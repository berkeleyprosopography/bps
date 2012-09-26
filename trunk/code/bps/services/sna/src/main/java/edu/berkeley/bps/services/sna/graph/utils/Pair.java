package edu.berkeley.bps.services.sna.graph.utils;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name="data")
public class Pair{
	   private String v;
	   private String k; 
	   
	   public Pair(){};
	   public Pair(String kk, String vv){k = kk; v= vv;}
	   public Pair(String kk, Number vv){k = kk; v= vv.toString();}
	   public Pair(Number kk, Number vv){k = kk.toString(); v= vv.toString();}
	   
	   @XmlAttribute(name="key")
	   public String getKey(){
		   return k;
	   }
	   
	   @XmlValue
	   public String getValue(){
		   return v;
	   }

}
