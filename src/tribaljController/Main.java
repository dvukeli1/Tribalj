/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tribaljController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import tribaljModel.MeteoModel;
import tribaljModel.data;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 *
 * @author Mitja
 */
public class Main {

    /**
     * Variable declarations
     */
    private static double speed[];
    private static double angleWind[];
    private static final DecimalFormat df_2 = new DecimalFormat("###.##");
    private static int count = 0;
    private static final int stationID = 1;
    private static String data;
    private static double averageSpeed = 0.0;
    private static double maxSpeed = 0.0;
    private static double newAngle = 0.0;
    private static final int sampleNumber = 100;
    private static boolean isSend = false;
    private static final int schedule = 60000;
    private static boolean read = true;
    private static double EW_VectorArr[];
    private static double NS_VectorArr[];
    private static double tempw[];
    private static double temps[];
    private static LocalDateTime localDateTime;
    private static final Serial sensor = SerialFactory.createInstance();
    private static String command = "";
    private static final Timer sendData = new Timer();
    private static String direction = "N";
    private static String time = "";
    private static boolean isError = false;
    private static boolean errorSend = false;
    private static int readingsNumber = 0;
    private static int wrongCondNumber = 0;
    private static String tweetMessage = "";
    private static boolean isTweet = false;
    private static Logger logger;
    private static MeteoModel yunMeteo = new MeteoModel();
    private static String yunAddress = "http://elmasserver.dyndns.org:19871";
    private static String httpBase = "/arduino/";
    private static I2c i2c = new I2c();
    private static data meteoData;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        /* try {
            factory = NewHibernateUtil.getSessionFactory();
        } catch (Exception e) {
        }*/
        
        logger = Logger.getLogger(Main.class.getName());
        ConsoleHandler consoleHandler = new ConsoleHandler();
       

        localDateTime = LocalDateTime.now();
        meteoData = i2c.getMeteoData();
        // TODO code application logic here
        speed = new double[sampleNumber];
        angleWind = new double[sampleNumber];
        temps = new double[sampleNumber];
        tempw = new double[sampleNumber];
        EW_VectorArr = new double[sampleNumber];
        NS_VectorArr = new double[sampleNumber];
        for (int i = 0; i < sampleNumber; i++) {
            speed[i] = angleWind[i] = 0.0;

        }

        try {
            sensor.open("/dev/ttyUSB0", 19200);
        } catch (Exception e) {
            command = "echo \"Restarting Main:  USB PROBLEM !!   $(date)\" | mail -s \"Main stanica\" \"petimilan@gmail.com\"";
            TerminalCommand(command);
            command = "sudo reboot";
            TerminalCommand(command);
        }

        sensor.addListener((SerialDataListener) (SerialDataEvent event) -> {

            if (read) {
                data = event.getData();
             //   logger.log(Level.INFO, "count = {0}", count);
                //  System.out.println("count = " + count);

                try {

                    String received[] = data.split("\r\n");
                    String split[] = received[1].split(":");

                    angleWind[count] = Double.valueOf(split[1]);

                    String split1[] = received[0].split(":");
                    double speedTemp = Double.valueOf(split1[1]);
                    if (count > 0) {
                        if (speed[count - 1] / speed[count] < 1.2) {
                            speed[count] = speed[count - 1];
                        } else {
                            speed[count] = speedTemp;
                        }
                    } else {
                        speed[count] = speedTemp;
                    }

                    count++;

                } catch (Exception e) {
                }
                if (count == sampleNumber) {
                    read = false;

                    temps = speed;
                    tempw = angleWind;
                    for (int i = 0; i < sampleNumber; i++) {
                        temps[i] = speed[i];
                        tempw[i] = angleWind[i];

                    }
//                    System.out.println("Reset !!");
                }
                data = "";
            }
        });

        Watchdog();

    }

    /**
     * Watchdog for calculations
     */
    private static void Watchdog() {
        Timer watchdog = new Timer();
        watchdog.schedule(new TimerTask() {

            @Override
            public void run() {
                if (!isSend) {
                    isSend = true;
                    Calculations();
                }
                isSend = false;
            }
        }, schedule, schedule + 6000);
    }

    /**
     * Json to object
     */
    private static void jsonToObject(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        yunMeteo = mapper.readValue(json, MeteoModel.class);
     }

    private static String objectToJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(meteoData);
    }

    /**
     * HTTP get
     *
     * @param urlToRead
     */
    public static String getHTML(String urlToRead) throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
        }
        return result.toString();
    }

    /**
     * Calculate and send data every 60 seconds
     */
    private static void Calculations() {

        sendData.schedule(new TimerTask() {

            @Override
            public void run() {

                command = "empty";
                if (!sensor.isOpen() || sensor.isShutdown()) {
                    try {
                        sensor.open("/dev/ttyUSB0", 19200);
                    } catch (Exception e) {
                        command = "echo \"Restarting Main:  USB PROBLEM !!   $(date)\" | mail -s \"Main stanica\" \"petimilan@gmail.com\"";
                        TerminalCommand(command);
//                        System.exit(0);
                    }
                }
                
       
         /**
          *  
          * B = (ln(RH / 100) + ((17.27 * T) / (237.3 + T))) / 17.27 
          * 
          * D = (237.3 * B) / (1 - B)
          * where:
               T = Air Temperature (Dry Bulb) in Centigrade (C) degrees
              RH = Relative Humidity in percent (%)
               B = intermediate value (no units) 
               D = Dew point in Centigrade (C) degrees
           
          * Cloud base = ((temp - D)/4)*1000 ; 
          * HI= c1+c2T+c3R+c4TR+c5T2+c6R2+c7T2R+c8TR2+c9T2R2

            In this formula,
            HI = heat index in degrees Fahrenheit,
            R = Relative humidity,
            T = Temperature in ∘F, T(°F) = T(°C) × 1.8 + 32
            c1 = -42.379,
             c2= -2.04901523,
            c3 = -10.14333127,
            c4 = -0.22475541,
            c5 = -6.83783 x 10−3,
            c6 = -5.481717 x 10−2,
            c7 = -1.22874 x 10−3,
            c8 = 8.5282 x 10−4,
            c9 = -1.99 x 10−6
            T(°C) = (HI - 32) / 1.8
It is found that whenever HI values exceed above 40∘C during different temperature and humidity ranges, there is a threat of heatstroke.
          Celsius	Fahrenheit	Notes
27–32 °C	80–90 °F	Caution: fatigue is possible with prolonged exposure and activity. Continuing activity could result in heat cramps.
32–41 °C	90–105 °F	Extreme caution: heat cramps and heat exhaustion are possible. Continuing activity could result in heat stroke.
41–54 °C	105–130 °F	Danger: heat cramps and heat exhaustion are likely; heat stroke is probable with continued activity.
over 54 °C	over 130 °F	Extreme danger: heat stroke is imminent.
                 */
                try {
                    String resp = getHTML(yunAddress + httpBase + "/meteo/1").replace(":\"", "\"");
              //      System.out.println("Response = " + resp);
                    jsonToObject(resp);
                    meteoData.setRAIND_F(df_2.format(yunMeteo.getRainDrop()));
                } catch (Exception ex) {
                  logger.log(Level.SEVERE, null, ex);
                }
                try {
                    i2c.sht25();
                    i2c.vario();
                } catch (IOException | InterruptedException ex) {
                   logger.log(Level.SEVERE, null, ex);
                }
               
                double humidity = Double.valueOf(meteoData.getMOIST());
                double temperature = Double.valueOf(meteoData.getTEMP());
                
               
                double B = (Math.log(humidity / 100) + ((17.27 * temperature) / (237.3 + temperature))) / 17.27;
               double dewPoint = (237.3 * B) / (1 - B);
          
               // double cloudBase = ((temperature - dewPoint) / 4) * 1000;
                 double cloudBase = 125* ( temperature -(temperature - ((100-humidity) / 5)));
               
               

                isError = false;
                boolean isOk = false;
                double EW_Vector = 0.0;
                double NS_Vector = 0.0;
                double newMax = 0.0;
                maxSpeed = 0.01;
                averageSpeed = 0.01;
                newAngle = 0.01;
                double windGust = 0.01;
                double angle = 359.9;
                for (int i = 0; i < sampleNumber; i++) {
                    EW_VectorArr[i] = Math.sin(tempw[i] * (Math.PI / 180)) * temps[i];
                    NS_VectorArr[i] = Math.cos(tempw[i] * (Math.PI / 180)) * temps[i];
                    averageSpeed += temps[i];
                    EW_Vector += EW_VectorArr[i];
                    NS_Vector += NS_VectorArr[i];
                    newAngle += tempw[i];
                    if (i > 4) {
                        double ew = ((EW_VectorArr[i] + EW_VectorArr[i - 1] + EW_VectorArr[i - 2] + EW_VectorArr[i - 3] + EW_VectorArr[i - 4] + EW_VectorArr[i - 5]));
                        double ns = ((NS_VectorArr[i] + NS_VectorArr[i - 1] + NS_VectorArr[i - 2] + NS_VectorArr[i - 3] + NS_VectorArr[i - 4] + NS_VectorArr[i - 5]));
                        double avgSpeed = (temps[i] + temps[i - 1] + temps[i - 2] + temps[i - 3] + temps[i - 4] + temps[i - 5]) / 6;

                        double ew_aver = (ew / 6) * (-1);
                        double ns_aver = (ns / 6) * (-1);

                        /* if(ew != 0 || ns != 0){
                         newMax= Math.sqrt(Math.pow(ew_aver, 2) + Math.pow(ns_aver, 2));
                         }*/
                        newMax = avgSpeed;
                        if (maxSpeed < newMax) {
                            maxSpeed = newMax;
                        }

                    }

                }
                //             System.out.println(" EW_aver = " + EW_Vector + " NS_aver = " + NS_Vector);
                Double EW_Avg = (EW_Vector / count + 1) * (-1);
                Double NS_Avg = (NS_Vector / count + 1) * (-1);
                int ang = 0;
                if (EW_Avg > 0) {
                    ang = 180;
                }
                if (NS_Avg < 0 && EW_Avg < 0) {
                    ang = 0;
                }
                if (NS_Avg > 0 && EW_Avg < 0) {
                    ang = 360;
                }

                //angle = (180 / Math.PI) * Math.atan(NS_Avg / EW_Avg) + ang;
                angle = newAngle / count;
                if (EW_Vector != 0 || NS_Vector != 0) {
                    double averageSpeedGust = Math.sqrt(Math.pow(EW_Avg, 2) + Math.pow(NS_Avg, 2));
                    averageSpeed /= (count + 1);
                    windGust = averageSpeedGust * (1 + (3 / (Math.log(750 / 0.03))));
                   
                    try {
                        direction = headingToString(angle);
                    } catch (Exception e) {
                    }
                    isOk = true;

                }
                if (EW_Vector == 0 && NS_Vector == 0) {
                    averageSpeed = 0.001;
                    windGust = 0.001;
                    direction = headingToString(359.9);
                    maxSpeed = 0.001;
                    isOk = true;

                }
                if (count < 59 && !isError) {

                    isError = true;
                    isOk = false;
                  

                }

                if (isOk && !isError) {
                    if (errorSend) {
                        command = " echo 'Sensor on line after error '$(date) | mail -s 'Meteo stanica' petimilan@gmail.com";
                        SendData();
                        errorSend = false;
                    }
                    if (!errorSend) {
                        localDateTime = LocalDateTime.now();
                        time = "" + localDateTime.getYear() + "-" + localDateTime.getMonthValue() + "-" + localDateTime.getDayOfMonth()
                                + "/" + localDateTime.getHour() + ":" + localDateTime.getMinute() + ":" + localDateTime.getSecond();
                        String time2 = "" + localDateTime.getDayOfMonth() + "-" + localDateTime.getMonthValue() + "-" + localDateTime.getYear()
                                + " " + localDateTime.getHour() + ":" + localDateTime.getMinute();
                        command = "/home/pi/sendData.sh " + time + " " + stationID + " " + df_2.format(averageSpeed) + " " + direction + " "
                                + df_2.format(angle) + " " + df_2.format(windGust) + " " + df_2.format(maxSpeed); // + df_2.format(temperature) + df_2.format(humidity)
                               // + df_2.format(pressure) + df_2.format(cloudBase) + " 0 0 0";
                        tweetMessage = time2 + "\nWind direction - " + direction + "\nAverage wind speed - " + df_2.format(averageSpeed)
                                + " m/s \n" + "Max wind speed = " + df_2.format(maxSpeed) + " m/s \n" + "Wind gust = " + df_2.format(windGust) + " m/s";
                        //                 System.out.println(tweetMessage);
                        
                        
                        meteoData.setCL_BASE((int)(cloudBase - cloudBase%100) +"");
                        meteoData.setD_POINT(df_2.format(dewPoint));
                        meteoData.setST_ID(stationID+"");
                        meteoData.setSEND_TIME(time);
                        meteoData.setWIND_ANG(df_2.format(angle));
                        meteoData.setWIND_SP(df_2.format(averageSpeed));
                        meteoData.setWIND_GUST(df_2.format(windGust));
                        meteoData.setWIND_MAX(df_2.format(maxSpeed));
                        meteoData.setWIND_DIR(direction);
                     
                        
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd/HH:mm:ss");
                        Date date = Calendar.getInstance().getTime();
                        try {
                            date = formatter.parse(time);
                        } catch (ParseException ex) {
                          logger.log(Level.SEVERE, null, ex);
                        }
                    //    logger.info(tweetMessage);
                        try {
                            sendJson();
                            //    SendData();
                            // Tweet();
                        } catch (IOException ex) {
                           logger.log(Level.SEVERE, null, ex);
                        }
                    }
                }
                if (!isOk && isError) {
                    if (!errorSend) {
                        command = " echo 'NO DATA - Sensor problem '$(date) | mail -s 'Meteo stanica' petimilan@gmail.com";
                        try {
                           
                            sendJson();
                        } catch (IOException ex) {
                           logger.log(Level.SEVERE, null, ex);
                        }
                    }
                    errorSend = true;
                }
               
                count = 0;
                read = true;
                isSend = true;
                if ((localDateTime.getHour() > 9 && localDateTime.getHour() < 18)) {
                    
                    if (angle > 90 && angle < 270) {
                       
                        if (averageSpeed < 10) {
                                  
                            readingsNumber++;
                                   
                        } else {
                            wrongCondNumber++;

                        }
                    } else {
                        wrongCondNumber++;

                    }
                } else {
                    wrongCondNumber++;

                }
                
                if (wrongCondNumber == 5) {
                    wrongCondNumber = 0;
                    isTweet = true;
                    readingsNumber = 0;
                }
                if (isTweet && readingsNumber == 10) {
                    
                    isTweet = false;
                    readingsNumber = 0;
                    wrongCondNumber = 0;
                    Tweet();
                }
            }
        }, 100, schedule);
    }

    /**
     * Send command to RPi terminal
     *
     * @param command - command to send
     * @return terminal response
     */
    private static String TerminalCommand(String command) {

        String line = "";
        String[] cmd = {"/bin/sh", "-c", command};
        Process p;
        try {
            p = Runtime.getRuntime().exec(cmd);
            int i;
            char c;
            while ((i = (byte) p.getErrorStream().read()) != -1) {
                line += String.valueOf(i);
            }
            if (!line.isEmpty()) {
                // return line;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String read;
            line = "";
            while ((read = reader.readLine()) != null) {
                line += read;

            }
            p.waitFor();

        } catch (IOException | InterruptedException e) {
            return e.getMessage();
        }

        return line;

    }

    /**
     * Convert wind angle to wind heading
     *
     * @param x - double value of average wind angle from windDirection
     * @return String representing wind heading (8 side wind rose)
     */
    public static String headingToString(double x) {
        String heading[] = {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW", "N"};

        return heading[(int) Math.round((((double) x % 360) / 22.5))];
    }

    /**
     * Send data to database
     */
    private static void SendData() {
        Thread send = new Thread(new Runnable() {

            @Override
            public void run() {

                String response = "ID = " + TerminalCommand(command) + "\n";
                //     System.out.println(response);
            }
        });
        send.start();
    }

    /**
     * Send tweet
     */
    private static void Tweet() {
        Thread tweet = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    ConfigurationBuilder cb = new ConfigurationBuilder();
                    cb.setDebugEnabled(true)
                            .setOAuthConsumerKey("Y0cZwkCVyARjeADtAXnQ5n05D")
                            .setOAuthConsumerSecret("qLySVecQkuASIN2LXzpRHZZXbacKZQzRPF0EA5OknqDK6D9uoe")
                            .setOAuthAccessToken("3901923855-cGOp5RoqkkPQcIWlCoG65BXwxMFiw6twi8LLuxD")
                            .setOAuthAccessTokenSecret("Dy1yd0oTejBmkv88mytPFufO8CvZHv6gnZVxipmsdTY5L");
                    TwitterFactory tf = new TwitterFactory(cb.build());
                    Twitter twitter = tf.getInstance();
                    StatusUpdate statusUpdate = new StatusUpdate(tweetMessage);
                    //attach any media, if you want to
                    statusUpdate.setMedia(
                            //title of media
                            "", new URL("http://flumen.club/fotke/front.jpg?raw=1").openStream());
                    try {
                        Status status = twitter.updateStatus(statusUpdate);
                    } catch (TwitterException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }

                } catch (MalformedURLException ex) {
                    logger.log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }

            }
        });
        tweet.start();

    }

    private static boolean CountMinutes(boolean start) {
        if (start) {
            readingsNumber++;
        } else {
            readingsNumber = 0;
        }
        if (readingsNumber == 15) {
            readingsNumber = 1;
            return true;
        } else {
            return false;
        }
    }

    private static void sendJson() throws MalformedURLException, IOException {
        String json = objectToJson();
    //    System.out.println(" json = " + json);
        String url = "http://flumen.club/wp/data/rest.php/data";
        URL object = new URL(url);

        HttpURLConnection con = (HttpURLConnection) object.openConnection();
        con.setDoOutput(true);
        con.setDoInput(true);
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestMethod("POST");

        OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
        wr.write(json);
        wr.flush();

        StringBuilder sb = new StringBuilder();
        int HttpResult = con.getResponseCode();
        if (HttpResult == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "utf-8"))) {
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }
           logger.log(Level.INFO, "Last ID {0}", sb.toString());
        } else {
            logger.log(Level.WARNING,con.getResponseMessage());
        }
    }
}
