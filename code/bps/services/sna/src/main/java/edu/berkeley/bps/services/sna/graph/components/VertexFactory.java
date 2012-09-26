package edu.berkeley.bps.services.sna.graph.components;

import java.util.Map;
import org.apache.commons.collections15.Factory;
import edu.berkeley.bps.services.sna.graph.utils.Pair;


public class VertexFactory implements Factory<Vertex>
{
    private int n = 0;
    
	public Vertex create() {
		return (new Vertex(n++, null));
	}
	
	public Vertex create( Map<String, String> v) {

		return (new Vertex(n++, v));
	}
	public Vertex create( Pair p) {
		Vertex v = new Vertex(n++);
		v.addData((String)p.getKey(), (String)p.getValue());
		return v;
	}
	
	
	
	public Vertex create( Map<String, String> v, String id) {
		
		v.put("GMLid", id);
		return (new Vertex(n++, v));
	}
}