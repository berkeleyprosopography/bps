/* Social Network Analysis Module
 * Wrapper for JUNG libraries behaviour
 * 
 * Written by Davide Semenzin 
 * 
 * */


package edu.berkeley.bps.services.sna.math;

import edu.berkeley.bps.services.common.BaseResource;
import edu.berkeley.bps.services.common.ServiceContext;
import edu.berkeley.bps.services.common.SystemProperties;

import edu.berkeley.bps.services.sna.exceptions.NotFoundEdgeException;
import edu.berkeley.bps.services.sna.exceptions.NotFoundVertexException;
import edu.berkeley.bps.services.sna.graph.components.Vertex;
import edu.berkeley.bps.services.sna.graph.components.VertexFactory;
import edu.berkeley.bps.services.sna.graph.components.EdgeFactory;
import edu.berkeley.bps.services.sna.graph.components.Edge;
import edu.berkeley.bps.services.sna.graph.utils.Pair;
import edu.berkeley.bps.services.sna.graph.GraphWrapper;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;


/*Developer notes:
 * - Consider local caching of graph context due to how JUNG works
 * 
 * */

public class Sna implements SNAInterface {
	//Instance of the JUNG-dependent SNA module
	private Math math =  new Math();
		
	
//	public abstract class ExceptionHandler {
//	
//	public ExceptionHandler() {
//		//context = ContextManager.getContext(payload);
//	}
//	
//	public GraphWrapper build(Pair p) throws Throwable {
//		try {
//			return null;
//		}catch (IllegalArgumentException e){	
//				throw new NotFoundVertexException(
//						p.getKey() +": "+ p.getValue() + " is not a vertex of this graph.");
//		}
//	}
//	
//	public abstract String execute();
//}
	
	
	/* -----------------------------------------------------------------------------------
	 *                        NOT RETURNING GraphWrapper
	 *  ----------------------------------------------------------------------------------
	 * */
	
	/*
	 * Wraps around the toString() function in the Vertex type
	 * that is not visibile in the scope of SnaResource
	 * */
	public String GetVertexInfo(GraphWrapper graph, Pair p)  throws NotFoundVertexException{
		return math.GetVertexInfo(graph.getBaseGraph(), p);
		};

	/*
	 * Wraps around the toString() function in the Edge type
	 * that is not visibile in the scope of SnaResource
	 * */
	public String GetEdgeInfo(GraphWrapper graph, Pair source, Pair target) throws NotFoundEdgeException, NotFoundVertexException{
			return math.GetEdgeInfo(graph.getBaseGraph(), source, target);
	};
	
	/* -----------------------------------------------------------------------------------
	 *                        GRAPH MEASURES
	 *  ----------------------------------------------------------------------------------
	 * */
		
	/*
	 * Computes the number of edges in the graph.
	*/
	public  GraphWrapper ComputeGraphSize(GraphWrapper graph, Boolean sparse){
		Pair p = new Pair("size", math.ComputeGraphSize(graph.getBaseGraph()));
		if (sparse){
			GraphWrapper ret = new GraphWrapper();
			ret.setProperty(p);
			return ret;
		}
		else{
			graph.setProperty(p);
			return graph;
		}
	};
	
	/*
	 * Computes the number of vertices in the graph.
	*/
	public  GraphWrapper ComputeGraphOrder(GraphWrapper graph, Boolean sparse){
		Pair p = new Pair("order", math.ComputeGraphOrder(graph.getBaseGraph()));
		if (sparse){
			GraphWrapper ret = new GraphWrapper();
			ret.setProperty(p);
			return ret;
		}
		else{
			graph.setProperty(p);
			return graph;
		}

		};

	/* -----------------------------------------------------------------------------------
	 *                        DEGREE MEASURES
	 *  ----------------------------------------------------------------------------------
	 * */
		
	/*
	 * Computes the degree of a given vertex, identified by a Pair(k, v)
	 * where k = Property key    (i.e. name)
	 * and   v = Property value  (i.e. Nana-iddin)
	*/
	public GraphWrapper ComputeDegree(GraphWrapper graph, Pair p, Boolean sparse) throws NotFoundVertexException {
		try {
			Vertex v = math.getVertexByProperty(graph.getBaseGraph(), p);
			Integer degree = math.ComputeDegree(graph.getBaseGraph(), p);
			return Wrap(graph, degree, "degree", v, p, sparse);
		}
			catch (IllegalArgumentException e){	
				throw new NotFoundVertexException(
						p.getKey() +": "+ p.getValue() + " is not a vertex of this graph.");
		}
		
		};
	
		
	/*
	 * computes the degree of each vertex and pushes it as a property in the graph
	 * Faster than calling the single-node function on every node.
	*/
	public GraphWrapper ComputeDegree(GraphWrapper graph) throws NotFoundVertexException{
		for (Integer i = 0; i< math.ComputeGraphOrder(graph.getBaseGraph()); i++){	
			Pair p = new Pair ("id", i.toString());
			Vertex v = math.getVertexByProperty(graph.getBaseGraph(), p);
			Integer degree = math.ComputeDegree(graph.getBaseGraph(), p);
			v.addData("degree", degree.toString());
			}
		
			return graph;
		};
	
	/*
	 * Computes the normalized degree [0-1] of a vertex.
	*/
	public  GraphWrapper ComputeNormalizedDegree(GraphWrapper graph, Pair p, Boolean sparse) throws NotFoundVertexException{
		Vertex v = math.getVertexByProperty(graph.getBaseGraph(), p);
		Double degree = math.ComputeNormalizedDegree(graph.getBaseGraph(), p);
		return Wrap(graph, degree, "normalized_degree", v, p, sparse);
	};
	
	
	/*
	 * computes the normalized degree [0-1] of each vertex and pushes it as a property in the graph
	 * Faster than calling the single-node function on every node.
	*/
	public  GraphWrapper ComputeNormalizedDegree(GraphWrapper graph) throws NotFoundVertexException{
		for (Integer i = 0; i< math.ComputeGraphOrder(graph.getBaseGraph()); i++){
			Pair p = new Pair ("id", i.toString());
			Vertex v = math.getVertexByProperty(graph.getBaseGraph(), p);
			Double degree = math.ComputeNormalizedDegree(graph.getBaseGraph(), p);
			v.addData("normalized_degree", degree.toString());
		}
		return graph;
	};

	/*
	 * Computes the maximum degree in the graph and adds the value ad an
	 * <max_degree> entry in the graph root.
	*/
	public GraphWrapper ComputeMaxDegree(GraphWrapper graph, Boolean sparse){
		Integer degree = math.ComputeMaxDegree(graph.getBaseGraph());
		if (sparse) graph = new GraphWrapper();
		graph.setProperty(new Pair("max_degree", degree.toString()));
		return graph;
	};

	/*
	 * Computes the minimum degree in the graph and adds the value ad an
	 * <min_degree> entry in the graph root.
	*/
	public GraphWrapper ComputeMinDegree(GraphWrapper graph, Boolean sparse){
		Integer degree = math.ComputeMinDegree(graph.getBaseGraph());
		if (sparse) graph = new GraphWrapper();
		graph.setProperty(new Pair("min_degree", degree.toString()));
		return graph;
};
	
	/*
	 * Computes the in-degree of a vertex.
	*/
	public  GraphWrapper ComputeInDegree(GraphWrapper graph, Pair p, Boolean sparse) throws NotFoundVertexException{
		Vertex v = math.getVertexByProperty(graph.getBaseGraph(), p);
		Integer degree = math.ComputeInDegree(graph.getBaseGraph(), p);
		return Wrap(graph, degree, "in_degree", v, p, sparse);
	};
	
	/*
	 * Computes the in-degree per each vertex in the graph.
	*/
	public  GraphWrapper ComputeInDegree(GraphWrapper graph) throws NotFoundVertexException{
		for (Integer i = 0; i< math.ComputeGraphOrder(graph.getBaseGraph()); i++) {
			Pair p = new Pair ("id", i);
			Vertex v = math.getVertexByProperty(graph.getBaseGraph(), p);
			Integer degree = math.ComputeInDegree(graph.getBaseGraph(), p);
			v.addData("in_degree", degree.toString());
		}
		return graph;
	};

	/*
	 * Computes the out-degree of a vertex.
	*/
	public  GraphWrapper ComputeOutDegree(GraphWrapper graph, Pair p, Boolean sparse)throws NotFoundVertexException{
		Vertex v = math.getVertexByProperty(graph.getBaseGraph(), p);
		Integer degree = math.ComputeOutDegree(graph.getBaseGraph(), p);
		return Wrap(graph, degree, "out_degree", v, p, sparse);
		};
		
	/*
	 * Computes the out-degree per each vertex in the graph.
	*/
	public  GraphWrapper ComputeOutDegree(GraphWrapper graph) throws NotFoundVertexException{
		for (Integer i = 0; i< math.ComputeGraphOrder(graph.getBaseGraph()); i++){
			Pair p = new Pair ("id", i);
			Vertex v = math.getVertexByProperty(graph.getBaseGraph(), p);
			Integer degree = math.ComputeOutDegree(graph.getBaseGraph(), p);
			v.addData("out_degree", degree.toString());
		}
		return graph;
		};

		
		

		/* -----------------------------------------------------------------------------------
		 *                        CENTRALITY MEASURES
		 *  ----------------------------------------------------------------------------------
		 * */

		/*
		 * Computes the betweenness centrality of a specified node (via Pair( k, v))
		*/
		public  GraphWrapper ComputeVertexBetweennessCentrality(
				GraphWrapper graph, Pair p, Boolean sparse)
			throws NotFoundVertexException {
			Vertex v = math.getVertexByProperty(graph.getBaseGraph(), p);
			Double centrality = math.ComputeVertexBetweennessCentrality(graph.getBaseGraph(), p);
			return Wrap(graph, centrality, "BetweennessCentrality", v, p, sparse);};

		public  GraphWrapper ComputeBetweennessCentrality(GraphWrapper graph){
			Map<Vertex, Double> set = math.ComputeBetweennessCentrality(graph.getBaseGraph());
			Iterator<Vertex> ci = math.getVertexIterator(graph.getBaseGraph());
			
			while (ci.hasNext()){
				Vertex v = ci.next();
				v.addData("BetweennessCentrality", set.get(v).toString());
				}
			
			return graph;
			};

		public  GraphWrapper ComputeEigenvectorCentrality(GraphWrapper graph)
			throws NotFoundVertexException{
			for (Integer i = 0; i< math.ComputeGraphOrder(graph.getBaseGraph()); i++){
				Pair p = new Pair ("id", i.toString());
				Vertex v = math.getVertexByProperty(graph.getBaseGraph(), p);
				try {
					v.addData("EigenvectorCentrality", math.ComputeVertexEigenvectorCentrality
							(graph.getBaseGraph(), p).toString());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					throw new RuntimeException("Some node's outdegree is not > 0. " +
							"More info: " +e.getMessage());
				}
				}
			return graph;
			};
		
		public  GraphWrapper ComputeVertexEigenvectorCentrality(GraphWrapper graph, Pair p, 
				Boolean sparse) throws NotFoundVertexException{
			Vertex v = math.getVertexByProperty(graph.getBaseGraph(), p);
			Double centrality = null;
			try {
				centrality = math.ComputeVertexEigenvectorCentrality(graph.getBaseGraph(), p);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.getMessage();
			}
			return Wrap(graph, centrality, "EigenvectorCentrality", v, p, sparse);
		};
		
		public GraphWrapper ComputeVertexHITS(GraphWrapper graph, Pair p, Boolean sparse) 
		throws NotFoundVertexException{
			Pair result = math.ComputeHITS(graph.getBaseGraph(), p);
			Vertex v = math.getVertexByProperty(graph.getBaseGraph(), p);
			v.addData("HITS.hub", result.getKey());
			v.addData("HITS.authority", result.getValue());
			return graph;
		}
		
		public GraphWrapper ComputeHITS(GraphWrapper graph) throws NotFoundVertexException{
			for (Integer i = 0; i< math.ComputeGraphOrder(graph.getBaseGraph()); i++){
				Pair p = new Pair ("id", i.toString());
				Pair result = math.ComputeHITS(graph.getBaseGraph(), p);
				Vertex v = math.getVertexByProperty(graph.getBaseGraph(), p);
				v.addData("HITS.hub", result.getKey());
				v.addData("HITS.authority", result.getValue());
			}
			return graph;
		}
		
		public GraphWrapper ComputeVertexPageRank(GraphWrapper graph, Pair p, Double alpha,
				Boolean sparse)throws NotFoundVertexException{
			Double result = null;
			try {
				result = math.ComputeVertexPageRank(graph.getBaseGraph(), p, alpha);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Vertex v = math.getVertexByProperty(graph.getBaseGraph(), p);
			v.addData("PageRank", result.toString());
			return graph;
		}
		
		public GraphWrapper ComputePageRank(GraphWrapper graph, Double alpha)
		throws NotFoundVertexException{
			for (Integer i = 0; i< math.ComputeGraphOrder(graph.getBaseGraph()); i++){
				Pair p = new Pair ("id", i.toString());
				Double result = null;
				try {
					result = math.ComputeVertexPageRank(graph.getBaseGraph(), p, alpha);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Vertex v = math.getVertexByProperty(graph.getBaseGraph(), p);
				v.addData("PageRank", result.toString());
			}
			return graph;
		}
		
		
	/* -----------------------------------------------------------------------------------
	 *                        		CLUSTERS, SETS AND COMMUNITIES
	 *  ----------------------------------------------------------------------------------
	 * */
		
	/*
	 * Computes the K-means clusters given a number of candidates. The method
	 * returns the original graph, with the addition of a <KmeansClusterGroup>
	 * item to every node.
	*/
	public  GraphWrapper ComputeKmeansClusterSet(GraphWrapper graph, 
			Integer candidates){
		Collection<Set<Vertex>> cluster = math.ComputeKmeansClusterSet
			(graph.getBaseGraph(), candidates);
		Iterator<Set<Vertex>> ci =cluster.iterator();
		Integer group = 0;
		
		while (ci.hasNext()){
			Iterator<Vertex> si = ci.next().iterator();
			while(si.hasNext()){
				si.next().addData("KmeansClusterGroup", group.toString());
			}
			group++;
		}
		
		return graph;};

	/*
	 * Computes the K-means clusters given a number of candidates and returns a graph
	 * of the community centered around the vertex identified by a Pair(k,v) p.
	*/
	public  GraphWrapper ComputeKmeansCommunity(GraphWrapper graph, 
			Integer candidates, Pair p)  throws NotFoundVertexException{
		Collection<Set<Vertex>> cluster = math.ComputeKmeansCommunity
			(graph.getBaseGraph(), candidates, p);
		GraphWrapper ret = TrimGraphFromVertexSet(graph, cluster, "KMeansCommunityGroup");
		return ret;
		};

	public  GraphWrapper ComputeBiconnectedComponents(
			GraphWrapper graph, Boolean forceConversion) throws ClassCastException{		
		Collection<Set<Vertex>> cluster = math.ComputeBiconnectedComponents(
				graph.getBaseGraph(), forceConversion);
		Iterator<Set<Vertex>> ci =cluster.iterator();
		Integer group = 0;
	
		while (ci.hasNext()){
			Iterator<Vertex> si = ci.next().iterator();
			while(si.hasNext()){
				si.next().addData("BiconnectedComponentGroup", group.toString());
			}
			group++;
		}
	
	return graph;};
	
	/*
	 * Computes the Edge Betweenness cluster sets, consisting of all the node with betweenness
	 * greater or equal a threshold n. The method returns the original graph, with the addition 
	 * of an <EdgeBetweennessClusterGroup>item to every node that belongs to the cluster.
	*/
	public  GraphWrapper ComputeEBClusterSet(GraphWrapper graph, Integer n){		
		Collection<Set<Vertex>> cluster = math.ComputeEBClusterSet(graph.getBaseGraph(), n);
		Iterator<Set<Vertex>> ci =cluster.iterator();
		Integer group = 0;
		
		while (ci.hasNext()){
			Iterator<Vertex> si = ci.next().iterator();
			while(si.hasNext()){
				si.next().addData("EdgeBetweennessClusterGroup", group.toString());
			}
			group++;
		}
		return graph;
		};

	/*
	 * Computes the Edge Betweenness cluster sets, consisting of all the node with betweenness
	 * greater or equal a threshold n. The method returns a sparse graph comprising all the nodes
	 * whose betweenness is greater or equal a threshold n.
	*/	
	public  GraphWrapper ComputeEBCluster(GraphWrapper graph, Integer n){
		GraphWrapper cluster = new GraphWrapper(math.ComputeEBCluster(graph.getBaseGraph(), n));
		return cluster;
	};
	
	//public  Collection<Set<Vertex>> ComputeCliques(GraphWrapper graph){return null;};
	
	
	/*
	 * Computes the diameter of the graph (greatest distance between two vertices.
	 * NOT WORKIN AS IT REQUIRES THE DEFINITION OF A MEASUREMENT METRIC. INQUIRE.
	*/	
	public  GraphWrapper ComputeDiameter(GraphWrapper graph){
		Pair p = new Pair("diameter",math.ComputeDiameter(graph.getBaseGraph()) );
		graph.setProperty(p);
		return graph;
	};

	/*
	 * Computes the density of a graph, defined as the number of connections among nodes
	 * divided by the number of all possible connections. The String type parameter 
	 * allows for measuring the density of the sub-network of edges of type "type".
	*/
	public  GraphWrapper ComputeDensity(GraphWrapper graph, String type, Boolean sparse){
		Pair p = new Pair("density",math.ComputeDensity(graph.getBaseGraph(), type) );
		if (sparse){
			GraphWrapper ret = new GraphWrapper();
			ret.setProperty(p);
			return ret;
		}
		else{
			graph.setProperty(p);
			return graph;
		}
		};
		
	/*
	 * Computes the 1-neighbourhood of a given vertex identified by a Pair(k, v). The 
	 * parameter Boolean includeOrigin controls whether the node should be included
	 * in the result. 
	*/
	public  GraphWrapper ComputeHood(GraphWrapper graph, Pair p, Boolean sparse, 
			Boolean includeOrigin) throws NotFoundVertexException{
		Set<Vertex> nhood = new HashSet<Vertex>();
		Collection<Vertex> result = math.ComputeHood(graph.getBaseGraph(), p);
		if (result==null) return new GraphWrapper(); //OR ERROR CODE?
		else nhood.addAll(result);
		
		if (includeOrigin){
			nhood.add(math.getVertexByProperty(graph.getBaseGraph(), p));
			}
		Iterator<Vertex> ci = nhood.iterator(); 
		if (sparse){
			return TrimGraphFromVertexSet(graph, nhood);
		}
		else {
			while (ci.hasNext()){
				Vertex v = ci.next();
				v.addData("NeighborhoodOf", p.getValue());
			}
			return graph;
		}
		
	};

	/*
	 * Computes the number of edges that have a certain value of a specified property.
	*/
	public  GraphWrapper ComputeNumberEdgesByProperty(GraphWrapper graph, Pair p, Boolean sparse) throws Exception{
		GraphWrapper data = new GraphWrapper(graph.getBaseGraph());
		Integer value = math.ComputeNumberEdgesByProperty(graph.getBaseGraph(), p);
		if (sparse){
			graph.setProperty(new Pair("NumberEdges", value));
			graph.setProperty(new Pair("Property", p.getKey()));
			graph.setProperty(new Pair("Value", p.getValue()));
			return graph;
		}
		else{
			data.setProperty(new Pair("NumberEdges", value));
			data.setProperty(new Pair("Property", p.getKey()));
			data.setProperty(new Pair("Value", p.getValue()));
			return data;
		}
	};

	/*
	 * Computes the set of edges that have a certain value of a specified property.
	*/
	public GraphWrapper ComputeEdgePropertySet(GraphWrapper graph, Pair p) 
	throws NotFoundEdgeException, NotFoundVertexException, Exception{
		GraphWrapper filtered = new GraphWrapper(math.FilterByEdgeProperty(graph.getBaseGraph(), p));
		return filtered;
		};
	
		
	public GraphWrapper ComputeEdgeSetFromEndpoints(GraphWrapper graph, Pair p1, Pair p2) 
	throws NotFoundEdgeException, NotFoundVertexException, Exception{
		Collection<Edge> eSet = math.getEdgeByEndpoints(graph.getBaseGraph(), p1, p2);
		GraphWrapper ret = new GraphWrapper();
		
		Vertex source = math.getVertexByProperty(graph.getBaseGraph(), p1);
		Vertex target = math.getVertexByProperty(graph.getBaseGraph(), p2);
		ret.addVertex(source); ret.addVertex(target);
		
		for (Iterator<Edge> i = eSet.iterator(); i.hasNext();){
			ret.addEdge(i.next(), source, target);
		}
		
		return ret;
		};
		
		
	/*
	 * Computes the set of vertices that have a certain value of a specified property.
	*/
	public GraphWrapper ComputeVertexPropertySet(GraphWrapper graph, Pair p)
	throws NotFoundVertexException{
		GraphWrapper filtered = new GraphWrapper(math.FilterByVertexProperty(graph.getBaseGraph(), p));
		return filtered;
		};
		
		
		
		
	/* -----------------------------------------------------------------------------------
	 *                        		PATHS
	 *  ----------------------------------------------------------------------------------
	 * */
		public  GraphWrapper ComputeShortestPath(GraphWrapper graph, Pair p1, Pair p2, Boolean sparse) 
		throws IllegalArgumentException, NotFoundEdgeException, NotFoundVertexException{
			return new GraphWrapper(math.ComputeShortestPath(graph.getBaseGraph(), p1, p2));
			
		};
		
		
	public  GraphWrapper ComputeStrength(GraphWrapper graph, Pair p1, Pair p2, Boolean sparse) 
	throws Exception{
		GraphWrapper g = new GraphWrapper();
		g.setProperty(new Pair("Strength", math.ComputeStrength(graph.getBaseGraph(), p1, p2)));
		if (sparse) {
			g.setProperty(new Pair("Origin",p1.getValue()));
			g.setProperty(new Pair("Source",p2.getValue()));
			} else{
			g.addVertex(math.getVertexByProperty(graph.getBaseGraph(), p1));
			g.addVertex(math.getVertexByProperty(graph.getBaseGraph(), p2));
			} 
		return g;
		};

		public  GraphWrapper ComputeDistance(GraphWrapper graph, Pair p1, Pair p2, Boolean sparse) 
		throws Exception{
			GraphWrapper g = new GraphWrapper();
			g.setProperty(new Pair("Distance", math.ComputeDistance(graph.getBaseGraph(), p1, p2)));
			
			// The idea here is that the best way to represent the distance between two nodes
			// as a return type is to isolate them and either return pointers to their names
			// in case sparse==true (thus assuming there is persistency client=side), or either
			// by providing the nodes themselves in case the data is required. I can't think
			// of another meaningful/consistent way of doing this whilst returning the whole thing
			if (sparse) {
				g.setProperty(new Pair("Origin",p1.getValue()));
				g.setProperty(new Pair("Source",p2.getValue()));
				
			}
			else {
				g.addVertex(math.getVertexByProperty(graph.getBaseGraph(), p1));
				g.addVertex(math.getVertexByProperty(graph.getBaseGraph(), p2));
				} 
			return g;
			};


		public  GraphWrapper ComputeMultiplexity(GraphWrapper graph,  Pair p1, Pair p2, Boolean sparse) 
		throws Exception{
			GraphWrapper g = new GraphWrapper();
			g.setProperty(new Pair("Multiplexity", math.ComputeMultiplexity(graph.getBaseGraph(), p1, p2)));
			if (sparse) {
				g.setProperty(new Pair("Origin",p1.getValue()));
				g.setProperty(new Pair("Source",p2.getValue()));
			}
			else {
				g.addVertex(math.getVertexByProperty(graph.getBaseGraph(), p1));
				g.addVertex(math.getVertexByProperty(graph.getBaseGraph(), p2));
			} 
			return g;
		};
	
	
	/* -----------------------------------------------------------------------------------
	 *                        UTILITIES
	 *  ----------------------------------------------------------------------------------
	 */
	//Manages the exception handling in case a NotFoundVertexException is raised
	public Boolean verifyVertex(GraphWrapper c, Pair p) throws NotFoundVertexException {
			if (math.hasVertex(c.getBaseGraph(), new Pair(p.getKey(), p.getValue()))){
				return true;
			}
			else{
				throw new NotFoundVertexException("The specified vertex doesn't exist. " +
						"Try specifying the desired property via queryparam.");
			}
		}
	
	/*
	 * Builds and returns the response packages for degree and centrality methods
	 * that operate on a single vertex. 
	 * Horrible and temporary, reimplement with a Strategy pattern
	*/
	private GraphWrapper Wrap(GraphWrapper graph, Object metric, String metName, 
			Vertex v, Pair p, Boolean sparse)  throws NotFoundVertexException{
		if (sparse){
			//Calculates the metric and builds the response package
			//This will be the new graph it will return (empty)
			GraphWrapper ret = new GraphWrapper();
			//The vertex 
			ret.addVertex(v);
			
			Vertex v2 = new Vertex();
			try {
				v2 = math.getVertexByProperty(ret.getBaseGraph(), p);
			} catch (NotFoundVertexException e) {
				throw new NotFoundVertexException("Error in Wrap() - Vertex not found." 
						+ e.getLocalizedMessage());
			}
			v2.addData(metName, metric.toString());
			return ret;
		}
		//in this case, we return the entire graph, 
		//just adding the property to the requested vertex
		else {
			//Calculates the metric and builds the response package
			v.addData(metName, metric.toString());
			return graph;
			}
	}
	
	/*
	 * Builds and returns the response packages for degree and centrality methods
	 * that operate on a single vertex. 
	 * Horrible and temporary, reimplement with a Strategy pattern
	*/
	private GraphWrapper TrimGraphFromVertexSet (GraphWrapper g, Set<Vertex> subset){
		CopyOnWriteArrayList<Edge> edgeSet = new CopyOnWriteArrayList<Edge>();
		edgeSet.addAll(g.getBaseGraph().getEdges());
		Iterator<Edge> i = edgeSet.iterator();
		Iterator<Vertex> ci = subset.iterator();
		
		HashMap<String, Vertex> vset = new HashMap<String, Vertex>();
		//Creates a new empty GraphWrapper object
		GraphWrapper ret= new GraphWrapper();
		
		// Adds the vertices to the new graph
		while (ci.hasNext()){
				Vertex v = ci.next();
				ret.addVertex(v);
				vset.put(v.getProperty("GMLid"), v);
			}
		
		
		//Analyzes the edges in g and import those that are still valid
		while (i.hasNext()){
			Edge item = i.next();		
			if (vset.containsKey(item.getSource())){
				if(vset.containsKey(item.getTarget())){
					ret.addEdge(item, 
							vset.get(item.getSource()), 
							vset.get(item.getTarget())
							);
					}
			}
		} 
		return ret;

	}
	
	private GraphWrapper TrimGraphFromVertexSet (GraphWrapper g, 
			Collection<Set<Vertex>> subset, String metric) {
		CopyOnWriteArrayList<Edge> edgeSet = new CopyOnWriteArrayList<Edge>();
		edgeSet.addAll(g.getBaseGraph().getEdges());
		Iterator<Edge> i = edgeSet.iterator();
		
	
		Iterator<Set<Vertex>> ci =subset.iterator();
		HashMap<String, Vertex> vset = new HashMap<String, Vertex>();
		//Creates a new empty GraphWrapper object
		GraphWrapper graph= new GraphWrapper();
		Integer group = 0;
		// Adds the vertices to the new graph
		while (ci.hasNext()){
			Iterator<Vertex> si = ci.next().iterator();
			
			while(si.hasNext()){
				Vertex v = si.next();
				v.addData(metric, group.toString());
				graph.addVertex(v);
				vset.put(v.getProperty("GMLid"), v);
			}
			group++;
		}
		
		//Analyzes the edges in g and import those that are still valid
		while (i.hasNext()){
			Edge item = i.next();		
			if (vset.containsKey(item.getSource())){
				if(vset.containsKey(item.getTarget())){
					graph.addEdge(item, 
							vset.get(item.getSource()), 
							vset.get(item.getTarget())
							);
					}
			}
		} 
		
		return graph;
	}
	
	
}