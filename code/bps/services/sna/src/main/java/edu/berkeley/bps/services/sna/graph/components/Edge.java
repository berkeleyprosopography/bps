package edu.berkeley.bps.services.sna.graph.components;

import java.util.ArrayList;
import edu.berkeley.bps.services.sna.graph.utils.Pair;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.*;

@XmlRootElement(name="edge")
public class Edge{
	   
	   private int id;
	   //we assume edges are undirected unless otherwise specified
	   private Boolean directed=false;
	   private String source;
	   private String target;
	   
	   private Map<String, String> data;
	   
	   public Edge(){}
	   
	   public Edge( int i, String sor, String tar, Map<String, String> v){
	   	id = i;
	   	setSource(sor);
	   	setTarget(tar);
	   	data =v;
	   }
	   
	   public Edge( int i, Boolean directed, String sor, String tar, Map<String, String> v){
		   	id = i;
		   	setDirected(directed);
		   	setSource(sor);
		   	setTarget(tar);
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
	   
	   public Boolean setDirected(Boolean arg){ 
		   	try{
		   	directed = arg;}
		   	catch (Exception e) {
		   		return false;
		   	}
		   	return true;
		   }
		   
	   
	   public Boolean setSource(String arg){ 
	   	try{
	   	source = arg;}
	   	catch (Exception e) {
	   		return false;
	   	}
	   	return true;
	   }
	   
	   public Boolean setTarget(String arg){ 
		   	try{
		   	target = arg;}
		   	catch (Exception e) {
		   		return false;
		   	}
		   	return true;
		   }
		   
	   
	   public String toString(){
	   	return data.get("type") ;//+ "(T: " + type + ", V= " + value+ ") ";
	   }
	
	public String getType() {
			return data.get("type");
		}
	
	public Double getWeight() {
		try {
	         double d = Double.valueOf(data.get("weight").trim()).doubleValue();
	         return d;
	      } catch (NumberFormatException nfe) {
	         System.out.println("NumberFormatException: " + nfe.getMessage());
	      }
	      return null;
	}
	   
	public int getId() {
		return id;
	}
	
	@XmlAttribute
	public Boolean getDirected() {
		return directed;
	}
	
	@XmlAttribute
	public String getSource() {
		return source;
	}
	
	@XmlAttribute
	public String getTarget() {
		return target;
	}
	
	}