package edu.berkeley.bps.services.sna.math;

import java.util.concurrent.*;
import edu.berkeley.bps.services.sna.graph.components.Edge;
import edu.berkeley.bps.services.sna.graph.components.Vertex;
import edu.berkeley.bps.services.sna.graph.components.EdgeFactory;
import edu.berkeley.bps.services.sna.graph.components.VertexFactory;
import edu.berkeley.bps.services.sna.graph.utils.Pair;
import edu.berkeley.bps.services.sna.exceptions.NotFoundVertexException;
import edu.berkeley.bps.services.sna.exceptions.NotFoundEdgeException;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;
import org.jboss.resteasy.spi.ApplicationException;

import edu.uci.ics.jung.algorithms.importance.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.importance.Ranking;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.algorithms.cluster.BicomponentClusterer;
import edu.uci.ics.jung.algorithms.cluster.EdgeBetweennessClusterer;
import edu.uci.ics.jung.algorithms.cluster.VoltageClusterer;
import edu.uci.ics.jung.algorithms.filters.*;
import edu.uci.ics.jung.algorithms.scoring.EigenvectorCentrality;
import edu.uci.ics.jung.algorithms.scoring.HITS;
import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.algorithms.shortestpath.*;

public  class Math  {
	// Returns vertex info, wrapping the Vertex.getInfo() method

	public String GetVertexInfo(Graph<Vertex, Edge> graph, Pair p) throws NotFoundVertexException{
	return getVertexByProperty(graph,p).toString();
	
} 
	//
	public String GetEdgeInfo(Graph<Vertex, Edge> graph, Pair source, Pair target) 
		throws NotFoundEdgeException, NotFoundVertexException{
		Collection<Edge> list = this.getEdgeByEndpoints(graph, source, target);
		String ret = "";
		for (Iterator<Edge> i = list.iterator(); i.hasNext();){
			ret.concat(i.next().toString() + ";");
		}
		return null;
		
	} 
	
	public Boolean hasVertex(Graph<Vertex, Edge> graph, Pair p) throws NotFoundVertexException{
		return graph.containsVertex
			(getVertexByProperty
				(graph,p));
	}

public Integer ComputeDegree(Graph<Vertex, Edge> graph, Pair p) throws NotFoundVertexException{	
	Integer i =  graph.degree(getVertexByProperty(graph,p));
	return i;
}


public  Double ComputeNormalizedDegree(Graph<Vertex, Edge> graph, Pair p)throws NotFoundVertexException{
	double vval = (double)graph.degree(getVertexByProperty(graph,p));
	double max = (double)ComputeMaxDegree(graph);
	return vval/max;
}

public Integer ComputeMaxDegree(Graph<Vertex, Edge> graph){
	ArrayList<Integer> a= new ArrayList<Integer>();
	for (Iterator<Vertex> i = graph.getVertices().iterator(); i.hasNext();){
		a.add(graph.degree(i.next()));
	}
	return Collections.max(a);
}

public  Integer ComputeMinDegree(Graph<Vertex, Edge> graph){
	ArrayList<Integer> a= new ArrayList<Integer>();
	for (Iterator<Vertex> i = graph.getVertices().iterator(); i.hasNext();){
		a.add(graph.degree(i.next()));
	}
	return Collections.min(a);}

public  int ComputeInDegree(Graph<Vertex, Edge> graph, Pair p) throws NotFoundVertexException{
	return graph.inDegree(getVertexByProperty(graph,p));
}

public  int ComputeOutDegree(Graph<Vertex, Edge> graph, Pair p) throws NotFoundVertexException{
	return graph.outDegree(getVertexByProperty(graph,p));
}

public  int ComputeGraphSize(Graph<Vertex, Edge> graph){
	return graph.getEdgeCount();
}

public  Integer ComputeGraphOrder(Graph<Vertex, Edge> graph){
	return graph.getVertexCount();
}

public Collection<Vertex>  ComputeHood(Graph<Vertex, Edge> graph, Pair p) throws NotFoundVertexException{
	return graph.getNeighbors(getVertexByProperty(graph,p));
}

public Iterator<Vertex> getVertexIterator(Graph<Vertex, Edge> graph){
	return graph.getVertices().iterator();
}

public Iterator<Edge> getEdgeIterator(Graph<Vertex, Edge> graph){
	return graph.getEdges().iterator();
}

public  Collection<Set<Vertex>> ComputeKmeansClusterSet(Graph<Vertex, Edge> graph, int candidates){
	VoltageClusterer<Vertex, Edge> rbc = new VoltageClusterer<Vertex, Edge>(graph, candidates);
	Collection<Set<Vertex>> cluster = rbc.cluster(candidates);
	
	return cluster;
}

public  Collection<Set<Vertex>> ComputeKmeansCommunity(
		Graph<Vertex, Edge> graph, int candidates, Pair p)
		throws NotFoundVertexException{
	VoltageClusterer<Vertex, Edge> rbc = new VoltageClusterer<Vertex, Edge>(graph, candidates);
	Collection<Set<Vertex>> cluster = rbc.getCommunity(getVertexByProperty(graph,p));
	return cluster;
}

public  Collection<Set<Vertex>> ComputeBiconnectedComponents(Graph<Vertex, Edge> graph,
		Boolean forceConversion) throws ClassCastException{
	BicomponentClusterer<Vertex, Edge> rbc = new BicomponentClusterer<Vertex, Edge>();
	UndirectedSparseMultigraph<Vertex, Edge> undirectedArg = new UndirectedSparseMultigraph<Vertex, Edge>();
	
	try{
		for (Iterator<Edge> i = graph.getEdges().iterator(); i.hasNext();){
			Edge item = i.next();
			Vertex source = this.getVertexByProperty(graph, new Pair("GMLid", item.getSource()));
			Vertex target = this.getVertexByProperty(graph, new Pair("GMLid", item.getTarget()));
			//if we force the conversion, every node is added, regardless of the original setting
			//because forceConversion==true evaluates the whole expression to TRUE; otherwise
			//an edge is added only if it's not directed
			if (forceConversion || !item.getDirected()){
				undirectedArg.addVertex(source);
				undirectedArg.addVertex(target);
				undirectedArg.addEdge(item,source, target);} 
		}
	} catch (NotFoundVertexException e) {
		throw new ClassCastException(e.getLocalizedMessage());
	}
	
	Collection<Set<Vertex>> cluster = rbc.transform(undirectedArg);
	
	return cluster;
}


public  Set<Set<Vertex>> ComputeEBClusterSet(Graph<Vertex, Edge> graph, int n){
	EdgeBetweennessClusterer<Vertex, Edge> rbc = new EdgeBetweennessClusterer<Vertex, Edge>(n);
	Set<Set<Vertex>> cluster = rbc.transform(graph);
	return cluster;
}

public  Graph<Vertex, Edge> ComputeEBCluster(Graph<Vertex, Edge> graph, int n){
	EdgeBetweennessClusterer<Vertex, Edge> rbc = new EdgeBetweennessClusterer<Vertex, Edge>(n);
	rbc.transform(graph);
	List<Edge> edges= rbc.getEdgesRemoved();
	for (Iterator<Edge> i = edges.iterator(); i.hasNext();){
		graph.removeEdge(i.next());}
	return graph;
}

/*
public  Collection<Set<Vertex>> ComputeCliques(Graph<Vertex, Edge> graph){
	BronKerboschCliqueFinder g= new BronKerboschCliqueFinder(graph);
	Collection<Set<Vertex>> cluster=g.getAllMaximalCliques();
	return cluster;
}*/


public  int ComputeNumberEdgesByProperty(Graph<Vertex, Edge> graph, Pair p) throws Exception{
	Graph ret = FilterByEdgeProperty(graph, p);
	return ret.getEdgeCount();
	
}

//returns a list of all the properties in the graph
public  Set ComputePropertySet(Graph<Vertex, Edge> graph, String p){
	CopyOnWriteArrayList<Edge> edgeSet = new CopyOnWriteArrayList<Edge>();
	edgeSet.addAll(graph.getEdges());
	Set<String> ret = new HashSet<String>();
	Iterator<Edge> i = edgeSet.iterator();
	
	while (i.hasNext()){
		Edge item = i.next();
		ret.add(item.getProperty(p));
	} 
		
	return ret;
	
}

public  Double ComputeDiameter(Graph<Vertex, Edge> graph){
	return DistanceStatistics.diameter(graph, new DijkstraDistance<Vertex, Edge>(graph));
}


public  Double ComputeDensity(Graph<Vertex, Edge> graph, String type){
	//Do some tests before implementing the case of a directed graph
	//(do not divide by 2 in the final formula - pairs)
	Collection<Edge> e = graph.getEdges();
	int n = graph.getVertexCount();
	final Double alpha = 1.0;
	Double sigma= 0.0;
	
	for(Iterator<Edge> i = e.iterator(); i.hasNext(); ) {
		Edge item = i.next();
		
		if (type!= null) { 
			if (item.getType() == type){
				sigma = sigma + item.getWeight();}
		}
		else if (type == null){
		  sigma = sigma + item.getWeight();}
	}
		Double density = sigma / (alpha * n * ((n-1.0)/2.0));
	
	return density;
}

public  Double ComputeStrength(Graph<Vertex, Edge> graph, Pair p1, Pair p2) 
throws NotFoundVertexException, NotFoundEdgeException{
	Collection<Edge> e = graph.findEdgeSet(getVertexByProperty(graph,p1), getVertexByProperty(graph,p2));
	Double acc= 0.0;
	
	for(Iterator<Edge> i = e.iterator(); i.hasNext(); ) {
		  Edge item = i.next();
		  acc = acc + item.getWeight();}
	
	return acc;
}

public  Double ComputeDistance(Graph<Vertex, Edge> graph, Pair p1, Pair p2) throws Exception{
	DijkstraDistance scorer = new DijkstraDistance(graph);
	return (Double)scorer.getDistance(getVertexByProperty(graph, p1), getVertexByProperty(graph, p2));
	
}

public Graph<Vertex, Edge> ComputeShortestPath(Graph<Vertex, Edge> graph, Pair p1, Pair p2) 
throws IllegalArgumentException, NotFoundVertexException, NotFoundEdgeException{
	
	//obtain the list of edges
	Vertex EHsource =getVertexByProperty(graph, p1);
	Vertex EHtarget =getVertexByProperty(graph, p2);
	DijkstraShortestPath<Vertex, Edge> scorer = new DijkstraShortestPath<Vertex, Edge>(graph);
	List <Edge> path = scorer.getPath(EHsource, EHtarget);
	
	//if list is not null
	//Crates a new graph object
	Graph<Vertex, Edge> g = new SparseMultigraph<Vertex, Edge>();
	//For every Edge in the path, add the:
	for (Iterator<Edge> i = path.iterator() ; i.hasNext();){
		Edge e = i.next();
		//Vertices vertex
		Vertex source = getVertexByProperty(graph, new Pair("GMLid", e.getSource()));
		Vertex target = getVertexByProperty(graph, new Pair("GMLid", e.getTarget()));
		if (!g.containsVertex(source)) g.addVertex(source);
		g.addVertex(target);
		g.addEdge(e, source, target);
	}
	
	
	return g;

}


public  Integer ComputeMultiplexity(Graph<Vertex, Edge> graph,  Pair p1, Pair p2) throws Exception{
	if (!areNeighbors(graph, p1, p2)) throw new Exception("Nodes are not connected");
	Collection<Edge> e = graph.findEdgeSet(getVertexByProperty(graph,p1), getVertexByProperty(graph,p2));
	return e.size();
	}




//running time is: O(n^2 + nm).
public  Double ComputeVertexBetweennessCentrality(
		Graph<Vertex, Edge> graph, Pair p) throws NotFoundVertexException{
	BetweennessCentrality<Vertex, Edge> ranker = new BetweennessCentrality<Vertex, Edge>(graph);
	ranker.setRemoveRankScoresOnFinalize(false);
    ranker.setMaximumIterations(500);
    ranker.evaluate();
	return ranker.getVertexRankScore(getVertexByProperty(graph,p));
}


public  Map<Vertex, Double> ComputeBetweennessCentrality(Graph<Vertex, Edge> graph){
	BetweennessCentrality<Vertex, Edge> ranker = new BetweennessCentrality<Vertex, Edge>(graph);
	
	ranker.setRemoveRankScoresOnFinalize(false);
    ranker.setMaximumIterations(500);
    ranker.evaluate();
    Map<Object,Map <Vertex, Number>>  e_map = ranker.getVertexRankScores();
	    Map i_map = (Map) e_map.values().iterator().next();
	    Collection kset = i_map.keySet();
	    Collection vset = i_map.values();
	    Iterator kiterator = kset.iterator();
	    Iterator viterator = vset.iterator();
	    Map<Vertex, Double> retset = new HashMap<Vertex, Double>();
	    
	    //prints map
	   while(kiterator.hasNext()) {
		   Vertex v =  (Vertex)kiterator.next() ;
		   Double value = (Double)viterator.next();
		   retset.put(v, value);
		   //System.out.println(v.toString() + " --- Centrality= " + value.toString());
	   }
    return retset;
}


//running time is: O(n^2 + nm).
public  Double ComputeVertexEigenvectorCentrality(Graph<Vertex, Edge> graph, Pair p) throws Exception{
	EigenvectorCentrality<Vertex, Edge> ranker = new EigenvectorCentrality<Vertex, Edge>(graph);
    ranker.setMaxIterations(500);
    ranker.evaluate();
	return ranker.getVertexScore(getVertexByProperty(graph,p));
}

public  Pair ComputeHITS(Graph<Vertex, Edge> graph, Pair p) throws NotFoundVertexException{
	HITS<Vertex, Edge> ranker = new HITS<Vertex, Edge>(graph);
    ranker.setMaxIterations(500);
    ranker.evaluate();
    Double hub = ranker.getVertexScore(getVertexByProperty(graph,p)).hub;
    Double authority = ranker.getVertexScore(getVertexByProperty(graph,p)).authority;
    Pair ret = new Pair(hub, authority);
    return ret;
}

public  Double ComputeVertexPageRank(Graph<Vertex, Edge> graph, Pair p, Double alpha) throws Exception{
	PageRank<Vertex, Edge> ranker = new PageRank<Vertex, Edge>(graph, alpha);
    ranker.setMaxIterations(500);
    ranker.evaluate();
	return ranker.getVertexScore(getVertexByProperty(graph,p));
}


public  Graph<Vertex, Edge> FilterByEdgeProperty(Graph<Vertex, Edge> graph, final Pair p) 
	throws NotFoundVertexException, NotFoundEdgeException, Exception{
	//copies the edgeset in an array - Needs to be CopyOnWrite to avoid
	//concurrent access exception
	CopyOnWriteArrayList<Edge> edgeSet = new CopyOnWriteArrayList<Edge>();
	edgeSet.addAll(graph.getEdges());
	Set<Vertex> keep = new HashSet<Vertex>();
	Iterator<Edge> i = edgeSet.iterator();
	
	//iterates over the edgeset and decides what edges are to be kept
	while (i.hasNext()){
		Edge item = i.next();
		//add handling in case p.Value()==null
		//[means the user is looking for edges with a property, not a value]
		
		//Gets the value of the required property with respect to the current edge
		String nodeVal = "";
		if (p.getKey().equals("source")) nodeVal = item.getSource();
		else if (p.getKey().equals("target")) nodeVal = item.getTarget();
		else nodeVal = item.getProperty(p.getKey());
		
			if (nodeVal.equalsIgnoreCase(p.getValue())){
				System.out.println("Keeping edge " + item.getId());
				keep.add(getVertexByProperty(graph, new Pair("GMLid", item.getSource())));
				keep.add(getVertexByProperty(graph, new Pair("GMLid", item.getTarget())));
			}
		else{
			graph.removeEdge(item);
		}
		
	} 
	
	//checks that the property is present in the edgeset at all
	if (graph.getEdges().isEmpty()) 
		throw new NotFoundEdgeException("Combination" + p.getKey() + ":"+ p.getValue()+" not found in the edgeset");
	
	
	CopyOnWriteArrayList<Vertex> VSet = new CopyOnWriteArrayList<Vertex>();
	VSet.addAll(graph.getVertices());
	Iterator<Vertex> k = VSet.iterator();
	
	//iterates over the vertices
	while (k.hasNext()){
		Vertex item = k.next();
		if (keep.contains(item))
			System.out.println("keeping node" +item.getId());
		else
			graph.removeVertex(item);
	} 
	return graph;
}

public  Graph<Vertex, Edge> FilterByVertexProperty(
		Graph<Vertex, Edge> graph, final Pair p) 
		throws NotFoundVertexException{
	Graph<Vertex, Edge> ret = new SparseMultigraph<Vertex, Edge>();
	CopyOnWriteArrayList<Vertex> vertexSet = new CopyOnWriteArrayList<Vertex>();
	vertexSet.addAll(graph.getVertices());
	Iterator<Vertex> i = vertexSet.iterator();
	Set<Vertex> keep = new HashSet<Vertex>();
	
	//Goes through the vertices, and copies into an hashset
	//those that are to be kept. HashSet helps avoiding repetitions and is
	// fast to access for the next procedure. 
	while (i.hasNext()){
		Vertex item = i.next();
		//gets the value of property (key) in the current node
		String a = "-1";
		if (p.getKey().equals("id")) a = item.getId().toString();
		else a = item.getProperty(p.getKey());
		
		//COMMENTED BECAUSE WE TOLERATE SOME PROPERTIES EXISTING
		//ONLY IN SOME NODES
		//if (a == "") 
			//throw new NotFoundVertexException("Not found \n" + p.getKey() +":"+ p.getValue());
		//add handling in case p.Value()==null [means the user is looking for nodes with a property, not a value]
		
		if (a.equals(p.getValue())){
			keep.add(item);
			ret.addVertex(item);
		}
		
	} 
	
	if (ret.getVertexCount()==0) throw new NotFoundVertexException(
			"No vertex "+ p.getKey() +":"+ p.getValue() +" found in the current graph");
	
	//then, per every edge, it is checked if both the terminals belong to
	//vertices in the keep set, and if so, they are added to the new graph
	CopyOnWriteArrayList<Edge> ESet = new CopyOnWriteArrayList<Edge>();
	ESet.addAll(graph.getEdges());
	Iterator<Edge> k = ESet.iterator();
	
	while (k.hasNext()){
		Edge item = k.next();
		Vertex source = getVertexByProperty(graph, new Pair("GMLid", item.getSource()));
		Vertex target = getVertexByProperty(graph, new Pair("GMLid", item.getTarget()));
		if (keep.contains(source) && keep.contains(target)){
			System.out.println("adding edge" +item.getId());
			ret.addEdge(item, source, target);
			}
			
	} 
	return ret;
}

protected Collection<Edge> getEdgeByEndpoints(Graph<Vertex, Edge> graph, Pair source, Pair target) 
throws NotFoundVertexException, NotFoundEdgeException{
	
	Collection<Edge> set= graph.findEdgeSet(getVertexByProperty(graph,source), getVertexByProperty(graph,target));
	if (set.isEmpty()) 
		throw new NotFoundEdgeException("No edge(s) between " + 
				source.getKey() + ":" + source.getValue() +
				"and " + target.getKey() + ":" + target.getValue() );
	else return set;
	
}

protected Collection<Edge> getEdgeByEndpoints(Graph<Vertex, Edge> graph, Vertex source, Vertex target) 
throws NotFoundEdgeException{
	Collection<Edge> set= graph.findEdgeSet(source, target);
	if (set.isEmpty()) throw new NotFoundEdgeException("No edge(s) between " + 
				source.toString() +
				"and " + target.toString() );
	else return set;
}




//extend with lists
protected Vertex getVertexByProperty(Graph<Vertex, Edge> graph, Pair p) throws NotFoundVertexException{
	//if the user is asking for the id, return the internal int id
	//this is done in order to expose a uniform interface through pairs
	//of properties k, v - but at the same allowing the user to access
	//the two distinct values id and GraphMLid
	if (p.getKey().equals("id")){return getVertexById(graph, Integer.parseInt((String) p.getValue()));}
	
	//otherwise look in the properties map
	Collection<Vertex> set = graph.getVertices();
	boolean flag = true;
	Iterator<Vertex> it = set.iterator();
	
	while (flag == true && it.hasNext()){
		Vertex v = it.next();
			if (p.getValue().equals(v.getProperty((String) p.getKey())) ){
				flag=false;
				return v;
		} 
	}  
	return null;
}


private Vertex getVertexById(Graph<Vertex, Edge> graph, Integer arg){
	Collection<Vertex> set = graph.getVertices();
	boolean flag = true;
	Iterator<Vertex> it = set.iterator();
	
	while (flag == true && it.hasNext()){
		Vertex v = it.next();
			if (arg.equals(v.getId())){
				flag=false;
				return v;
		} 
	}  
	return null;
}

public Boolean areNeighbors (Graph<Vertex, Edge> graph,  Pair p1, Pair p2) throws NotFoundVertexException{
	return graph.isNeighbor(getVertexByProperty(graph,p1), getVertexByProperty(graph,p2));
}

}
