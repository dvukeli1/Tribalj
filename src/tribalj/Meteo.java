/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tribalj;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Mitja
 */
public class Meteo {
/**
 * Variable declarations
 */
    private static double speed[];
    private static double angleWind[];
    private static final DecimalFormat df2 = new DecimalFormat("###.##");
    private static int count = 0;
    private static final int stationID = 1;
    private static String data;
    private static Double averageSpeed = 0.0;
    private static Double maxSpeed = 0.0;
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
    private static  String command ="";
    private static final Timer sendData = new Timer();
    private static String direction = "N";
    private static String time ="";
    private static boolean isError = false;
    private static boolean errorSend = false;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
       
        localDateTime = LocalDateTime.now();
        // TODO code application logic here
         speed = new double[sampleNumber];
         angleWind = new double[sampleNumber];
         temps = new double[sampleNumber];
         tempw = new double[sampleNumber];
         EW_VectorArr = new double[sampleNumber];
         NS_VectorArr = new double[sampleNumber];
         for (int i = 0; i<sampleNumber; i++) {
                speed[i] = angleWind[i] = 0.0;
               
            }
         
        try {
              sensor.open("/dev/ttyUSB0", 19200);
        } catch (Exception e) {
            command = "echo \"Restarting Meteo:  USB PROBLEM !!   $(date)\" | mail -s \"Meteo stanica\" \"petimilan@gmail.com\"";
            TerminalCommand(command);
            command = "sudo reboot";
            TerminalCommand(command);
        }
        
        sensor.addListener((SerialDataListener) (SerialDataEvent event) -> {
           
            if(read){    
             data = event.getData();
                System.out.println("count = " + count);
               
            try {
                
                String received[] = data.split("\r\n");
                String split[] = received[1].split(":");
                 
                angleWind[count]= Double.valueOf(split[1]);
               
                String split1[] = received[0].split(":");

                speed [count]  = Double.valueOf(split1[1]);
              
                count++;
               
            }
            
                

             catch (Exception e) {
            }
            if (count == sampleNumber){
                read = false;
               
                
                temps = speed;
                tempw = angleWind;
                for (int i = 0; i<sampleNumber; i++) {
                temps[i] = speed[i];
                tempw[i] = angleWind[i];
               
            }
                System.out.println("Reset !!");
            }
            data = "";
            }
        });

       
    
        Watchdog();

    }

    /**
     * Watchdog for calculations
     */
    
    private static void Watchdog(){
        Timer watchdog = new Timer();
       watchdog.schedule(new TimerTask() {

            @Override
            public void run() {
               if (!isSend){
                   isSend = true;
                   Calculations();
               }
               isSend = false;
            }
        }, schedule, schedule+6000);
    }
    
    /**
     * Calculate and send data every 60 seconds
     */
    
    private static void Calculations(){
       
       
        sendData.schedule(new TimerTask() {

            @Override
            public void run() {
               command ="empty";
               if(!sensor.isOpen() || sensor.isShutdown()){
                try {
                    sensor.open("/dev/ttyUSB0", 19200);
                } catch (Exception e) {
                    command = "echo \"Restarting Meteo:  USB PROBLEM !!   $(date)\" | mail -s \"Meteo stanica\" \"petimilan@gmail.com\"";
                    TerminalCommand(command);
                   System.exit(0);
                }}
                
              isError = false;
               boolean isOk = false;
                double EW_Vector = 0.0;
                double NS_Vector = 0.0;
                Double newMax = 0.0;
                maxSpeed = 0.01;
                averageSpeed = 0.01 ;
                double windGust = 0.01;
                double angle = 359.9;
                for (int i = 0; i< sampleNumber;i++){
                    EW_VectorArr [i]= Math.sin(tempw[i]* (Math.PI / 180)) * temps[i];
                    NS_VectorArr [i]= Math.cos(tempw [i]* (Math.PI / 180)) * temps[i];
                     EW_Vector += EW_VectorArr[i];
                    NS_Vector += NS_VectorArr[i];
                    
                    if (i > 4) {
                          double  ew = ((EW_VectorArr[i] + EW_VectorArr[i-1] + EW_VectorArr[i-2] + EW_VectorArr[i-3] + EW_VectorArr[i-4] + EW_VectorArr[i-5]));
                          double  ns = ((NS_VectorArr[i] + NS_VectorArr[i-1] + NS_VectorArr[i-2]  + NS_VectorArr[i-3] + NS_VectorArr[i-4] + NS_VectorArr[i-5]));
                          double avgSpeed = (temps[i]+temps[i-1]+temps[i-2]+temps[i-3]+temps[i-4]+temps[i-5])/6;
                         
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
                System.out.println(" EW_aver = " + EW_Vector + " NS_aver = " + NS_Vector);
               Double EW_Avg = (EW_Vector / count+1)* (-1);
                Double NS_Avg = (NS_Vector / count+1)* (-1);
                int ang = 0;
                if (EW_Avg > 0 )  ang = 180;
               if (NS_Avg < 0 && EW_Avg < 0 )  ang = 0;
               if (NS_Avg > 0 && EW_Avg < 0) ang = 360;

                angle = (180 / Math.PI) * Math.atan(NS_Avg / EW_Avg) + ang;
                
                 if (EW_Vector != 0 || NS_Vector != 0){
                averageSpeed= Math.sqrt(Math.pow(EW_Avg, 2) + Math.pow(NS_Avg, 2));
              
                windGust = averageSpeed*(1+(3/(Math.log(750/0.03))));
               /*if(maxSpeed/windGust > 1.7){
                    maxSpeed = windGust;
                }*/
                /*if(maxSpeed < windGust){
                    maxSpeed = windGust;
                }*/
                     try {
                         direction = headingToString(angle);
                     } catch (Exception e) {
                     }
                     isOk = true;
                    
                    
                 }if(EW_Vector == 0 && NS_Vector == 0){
                     averageSpeed= 0.001;
                     windGust= 0.001;
                     direction= headingToString(359.9);
                     maxSpeed = 0.001;
                     isOk = true;
                     
                   
                 }if (count < 59 && !isError){
                    
                         isError = true;
                         isOk = false;
                         System.out.println("GRESKA!!");
                     
                 }
               

              
                if ( isOk && !isError) {
                    if(errorSend){
                         command = " echo 'Sensor on line after error '$(date) | mail -s 'Meteo stanica' petimilan@gmail.com";
                      SendData();
                      errorSend = false;
                    }
                    if(!errorSend){
                localDateTime = LocalDateTime.now();
                time= "" + localDateTime.getYear() + "-" + localDateTime.getMonthValue() + "-" + localDateTime.getDayOfMonth()
                        + "/" + localDateTime.getHour() + ":" + localDateTime.getMinute() + ":" + localDateTime.getSecond();

                command = "/home/pi/sendData.sh " + time + " " + stationID + " " + df2.format(averageSpeed) + " " + direction + " " 
                        + df2.format(angle) + " " + df2.format(windGust) + " " + df2.format(maxSpeed) + " 0 0";
                SendData();
                    }
                }
                if (!isOk && isError){
                    if(!errorSend){
                     command = " echo 'NO DATA - Sensor problem '$(date) | mail -s 'Meteo stanica' petimilan@gmail.com";
                      SendData();
                    }
                     errorSend = true; 
                }
                System.out.println(command);
                 count = 0;
                 read = true;
              isSend = true;
           
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
        String heading[] = {"N", "NNE","NE", "ENE","E", "ESE","SE","SSE", "S", "SSW","SW","WSW", "W","WNW","NW", "NNW","N"};
        
        return heading[(int) Math.round((((double) x % 360) / 22.5))];
    }

    private static void SendData(){
         Thread send = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        
                        String response = "ID = " + TerminalCommand(command)+ "\n";
                        System.out.println(response);
                    }
                });send.start();
    }
   
}
