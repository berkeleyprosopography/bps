package edu.berkeley.bps.services.sna.graph;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.berkeley.bps.services.sna.graph.utils.Pair;
import edu.berkeley.bps.services.sna.graph.components.Vertex;
import edu.berkeley.bps.services.sna.graph.components.VertexFactory;
import edu.berkeley.bps.services.sna.graph.components.EdgeFactory;
import edu.berkeley.bps.services.sna.graph.components.Edge;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.*;
@XmlRootElement (name="graph")
@XmlType (propOrder={"properties", "nodes", "edges"})
public class GraphWrapper {
	List<Pair> PropList = new ArrayList<Pair>();
    private final Graph<Vertex, Edge> graph;
    
    public GraphWrapper() {
        this.graph = new SparseMultigraph<Vertex, Edge>();
    }
    
    public GraphWrapper(Graph<Vertex, Edge> arg) {
        this.graph = arg;
    }

    public Graph<Vertex, Edge> getBaseGraph() {
        return this.graph;
    }
    
    public boolean setProperty(Pair p){
    	try{
	   		PropList.add(p);
	   	}catch (Exception e) {
	   		return false;
	   	}
	   	return true;

    }
    

    public Boolean addVertex(Vertex v){
    	return graph.addVertex(v);
    }
    
    public Boolean addEdge(Edge e, Vertex source, Vertex target){
    	return graph.addEdge(e, source, target);
    }
  
    @XmlElement(name="property")
    public List<Pair> getProperties(){return PropList;}
    
    //@XmlElementWrapper(name="nodes")
    @XmlElement(name="node")
    public List<Vertex> getNodes(){
        List<Vertex> myNodeList = new ArrayList<Vertex>(graph.getVertices());
        return myNodeList;
    }
    
    //@XmlElementWrapper(name="edges")
    @XmlElement(name="edge")
    public List<Edge> getEdges(){
        List<Edge> myEdgeList = new ArrayList<Edge>(graph.getEdges());
        return myEdgeList;
    }
    
    
}