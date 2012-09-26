/* Social Network Analysis Module
 * Graph Context Datatype Wrapper 
 * 
 * Written by Davide Semenzin 
 * 
 * */

package edu.berkeley.bps.services.sna.context;
import edu.berkeley.bps.services.sna.graph.GraphWrapper;
import edu.berkeley.bps.services.sna.graph.utils.Pair;

import java.util.*;



public class GraphContext {
	private GraphWrapper graph;
	private HashMap params = new HashMap();
	
	public GraphContext(){
		graph = new GraphWrapper();
	}
	public GraphContext(GraphWrapper g){graph = g;}
	
	public GraphWrapper getGraph(){
		return graph;
	}
	public HashMap getParams(){
		return params;
	}
	
	public Boolean setParam(Pair p){
		try{
		params.put(p.getKey(), p.getValue());
		return true;
		} catch (Exception e){
			return false;
		}
	}
	
	
}