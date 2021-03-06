package tribalj;
// Generated Oct 12, 2015 10:41:39 PM by Hibernate Tools 4.3.1


import java.util.Date;

/**
 * Data generated by hbm2java
 */
public class Data  implements java.io.Serializable {


     private Integer id;
     private Stations stations;
     private Date ts;
     private Date sendTime;
     private Double windSp;
     private String windDir;
     private Double windAng;
     private Double windGust;
     private Double windMax;
     private Double temp;
     private Double moist;
     private Double pressure;
     private Double clBase;
     private Double DPoint;
     private Double raindF;
     private Double heatIndex;

    public Data() {
    }

	
    public Data(Stations stations, Date ts) {
        this.stations = stations;
        this.ts = ts;
    }
    public Data(Stations stations, Date ts, Date sendTime, Double windSp, String windDir, Double windAng, Double windGust, Double windMax, Double temp, Double moist, Double pressure, Double clBase, Double DPoint, Double raindF, Double heatIndex) {
       this.stations = stations;
       this.ts = ts;
       this.sendTime = sendTime;
       this.windSp = windSp;
       this.windDir = windDir;
       this.windAng = windAng;
       this.windGust = windGust;
       this.windMax = windMax;
       this.temp = temp;
       this.moist = moist;
       this.pressure = pressure;
       this.clBase = clBase;
       this.DPoint = DPoint;
       this.raindF = raindF;
       this.heatIndex = heatIndex;
    }
   
    public Integer getId() {
        return this.id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    public Stations getStations() {
        return this.stations;
    }
    
    public void setStations(Stations stations) {
        this.stations = stations;
    }
    public Date getTs() {
        return this.ts;
    }
    
    public void setTs(Date ts) {
        this.ts = ts;
    }
    public Date getSendTime() {
        return this.sendTime;
    }
    
    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }
    public Double getWindSp() {
        return this.windSp;
    }
    
    public void setWindSp(Double windSp) {
        this.windSp = windSp;
    }
    public String getWindDir() {
        return this.windDir;
    }
    
    public void setWindDir(String windDir) {
        this.windDir = windDir;
    }
    public Double getWindAng() {
        return this.windAng;
    }
    
    public void setWindAng(Double windAng) {
        this.windAng = windAng;
    }
    public Double getWindGust() {
        return this.windGust;
    }
    
    public void setWindGust(Double windGust) {
        this.windGust = windGust;
    }
    public Double getWindMax() {
        return this.windMax;
    }
    
    public void setWindMax(Double windMax) {
        this.windMax = windMax;
    }
    public Double getTemp() {
        return this.temp;
    }
    
    public void setTemp(Double temp) {
        this.temp = temp;
    }
    public Double getMoist() {
        return this.moist;
    }
    
    public void setMoist(Double moist) {
        this.moist = moist;
    }
    public Double getPressure() {
        return this.pressure;
    }
    
    public void setPressure(Double pressure) {
        this.pressure = pressure;
    }
    public Double getClBase() {
        return this.clBase;
    }
    
    public void setClBase(Double clBase) {
        this.clBase = clBase;
    }
    public Double getDPoint() {
        return this.DPoint;
    }
    
    public void setDPoint(Double DPoint) {
        this.DPoint = DPoint;
    }
    public Double getRaindF() {
        return this.raindF;
    }
    
    public void setRaindF(Double raindF) {
        this.raindF = raindF;
    }
    public Double getHeatIndex() {
        return this.heatIndex;
    }
    
    public void setHeatIndex(Double heatIndex) {
        this.heatIndex = heatIndex;
    }




}


