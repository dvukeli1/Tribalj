/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tribaljModel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author Mitja
 */
public class data {
     @JsonProperty("ID")
    public String getID() { 
         return this.iD;
    }
    public void setID(String iD) { 
         this.iD = iD ;
    }
    String iD = null;

    @JsonProperty("TS")
    public String getTS() { 
         return this.tS; 
    }
    public void setTS(String tS) { 
         this.tS = tS; 
    }
    String tS = null;

    @JsonProperty("SEND_TIME")
    public String getSEND_TIME() { 
         return this.sEND_TIME; 
    }
    public void setSEND_TIME(String sEND_TIME) { 
         this.sEND_TIME = sEND_TIME; 
    }
    String sEND_TIME = null;

    @JsonProperty("ST_ID")
    public String getST_ID() { 
         return this.sT_ID; 
    }
    public void setST_ID(String sT_ID) { 
         this.sT_ID = sT_ID; 
    }
    String sT_ID = null;

    @JsonProperty("WIND_SP")
    public String getWIND_SP() { 
         return this.wIND_SP; 
    }
    public void setWIND_SP(String wIND_SP) { 
         this.wIND_SP = wIND_SP; 
    }
    String wIND_SP = null;

    @JsonProperty("WIND_DIR")
    public String getWIND_DIR() { 
         return this.wIND_DIR; 
    }
    public void setWIND_DIR(String wIND_DIR) { 
         this.wIND_DIR = wIND_DIR; 
    }
    String wIND_DIR = null;

    @JsonProperty("WIND_ANG")
    public String getWIND_ANG() { 
         return this.wIND_ANG; 
    }
    public void setWIND_ANG(String wIND_ANG) { 
         this.wIND_ANG = wIND_ANG; 
    }
    String wIND_ANG = null;

    @JsonProperty("WIND_GUST")
    public String getWIND_GUST() { 
         return this.wIND_GUST; 
    }
    public void setWIND_GUST(String wIND_GUST) { 
         this.wIND_GUST = wIND_GUST; 
    }
    String wIND_GUST = null;

    @JsonProperty("WIND_MAX")
    public String getWIND_MAX() { 
         return this.wIND_MAX; 
    }
    public void setWIND_MAX(String wIND_MAX) { 
         this.wIND_MAX = wIND_MAX; 
    }
    String wIND_MAX = null;

    @JsonProperty("TEMP")
    public String getTEMP() { 
         return this.tEMP; 
    }
    public void setTEMP(String tEMP) { 
         this.tEMP = tEMP; 
    }
    String tEMP = null;

    @JsonProperty("MOIST")
    public String getMOIST() { 
         return this.mOIST; 
    }
    public void setMOIST(String mOIST) { 
         this.mOIST = mOIST; 
    }
    String mOIST = null;

    @JsonProperty("PRESSURE")
    public String getPRESSURE() { 
         return this.pRESSURE; 
    }
    public void setPRESSURE(String pRESSURE) { 
         this.pRESSURE = pRESSURE; 
    }
    String pRESSURE = null;

    @JsonProperty("CL_BASE")
    public String getCL_BASE() { 
         return this.cL_BASE ;
    }
    public void setCL_BASE(String cL_BASE) { 
         this.cL_BASE = cL_BASE; 
    }
    String cL_BASE = null;

    @JsonProperty("D_POINT")
    public String getD_POINT() { 
         return this.d_POINT; 
    }
    public void setD_POINT(String d_POINT) { 
         this.d_POINT = d_POINT ;
    }
    String d_POINT = null;

    @JsonProperty("RAIND_F")
    public String getRAIND_F() { 
         return this.rAIND_F ;
    }
    public void setRAIND_F(String rAIND_F) { 
         this.rAIND_F = rAIND_F ;
    }
    String rAIND_F = null;

    @JsonProperty("HEAT_INDEX")
    public String getHEAT_INDEX() { 
         return this.hEAT_INDEX ;
    }
    public void setHEAT_INDEX(String hEAT_INDEX) { 
         this.hEAT_INDEX = hEAT_INDEX ;
    }
    String hEAT_INDEX = null;

    
}
