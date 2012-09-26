package edu.berkeley.bps.services.sna.graph.components;

import javax.xml.bind.annotation.*;
import edu.berkeley.bps.services.sna.graph.utils.Pair;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@XmlRootElement(name="node")
public class Vertex{
	   private int id;
	   private Map<String, String> data;
		   
	   
		public Vertex(){}
		
		public Vertex(int i){
			id=i;
		}
		
	   public Vertex( int i, Map<String, String> v){
	   	id =i;
	   	data =v;
	   }
	   
	   
	   public String getProperty(String k){
		   return data.get(k);
	   }
	   
	   public Boolean hasProperty(String k){
		   return data.containsKey(k);
	   }
	   
	   @XmlElement
	   public List<Pair> getData(){
		   List<Pair> mylist = new ArrayList<Pair>();
		   for (Map.Entry<String, String> entry : data.entrySet()){
			   Pair p = new Pair(entry.getKey(), entry.getValue());
			   mylist.add(p);
		   }
		   //removes the last element, which is the number of entries
		   //mylist.remove(mylist.size()-1);
		   return mylist;
	   }
	   
	   	   
	   @XmlAttribute
	   public Integer getId(){
		   return id;
	   }
	   
	   public Boolean addData(String k, String v){ 
	   	try{
	   		data.put(k, v);
	   	}catch (Exception e) {
	   		return false;
	   	}
	   	return true;
	   }
	   
  	   //generalise!!!
	   public String toString(){
		   String ret= data.get("name");
		   if (data.containsKey("father"))  
			   ret = ret + '/'+ data.get("father");
		   if (data.containsKey("grandfather"))  
			   ret = ret + '/'+ data.get("grandfather");
		   if (data.containsKey("clan"))  
			   ret = ret + "//"+ data.get("clan");
		   
		   
	   	return  ret  ;
	   }
	   
	   public boolean Equals(String s){ 
		   if (s!=null){
			   for (Map.Entry<String, String> entry : data.entrySet()){
				   if (entry.getValue()==s){
					   return true;
				   }
			   }
			   
		   }
		   return false;}
	   
	   

	}