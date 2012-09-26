package edu.berkeley.bps.services.sna.math;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import edu.berkeley.bps.services.sna.exceptions.NotFoundEdgeException;
import edu.berkeley.bps.services.sna.exceptions.NotFoundVertexException;
import edu.berkeley.bps.services.sna.graph.components.Edge;
import edu.berkeley.bps.services.sna.graph.components.Vertex;
import edu.berkeley.bps.services.sna.graph.components.EdgeFactory;
import edu.berkeley.bps.services.sna.graph.components.VertexFactory;
import edu.berkeley.bps.services.sna.graph.utils.Pair;
import edu.berkeley.bps.services.sna.graph.GraphWrapper;


public interface SNAInterface {
	
	public String GetVertexInfo(GraphWrapper graph, Pair p) throws NotFoundVertexException;
	public String GetEdgeInfo(GraphWrapper graph, Pair source, Pair target) throws NotFoundEdgeException, NotFoundVertexException;
	public GraphWrapper ComputeDegree(GraphWrapper graph, Pair p, Boolean sparse) throws NotFoundVertexException;
	public GraphWrapper ComputeDegree(GraphWrapper graph)  throws NotFoundVertexException;
	public GraphWrapper ComputeNormalizedDegree(GraphWrapper graph, Pair p, Boolean sparse) throws NotFoundVertexException;
	public GraphWrapper ComputeNormalizedDegree(GraphWrapper graph)  throws NotFoundVertexException;
	public GraphWrapper ComputeMaxDegree(GraphWrapper graph, Boolean sparse);
	public GraphWrapper ComputeMinDegree(GraphWrapper graph, Boolean sparse);
	public  GraphWrapper ComputeInDegree(GraphWrapper graph, Pair p, Boolean sparse) throws NotFoundVertexException;
	public  GraphWrapper ComputeInDegree(GraphWrapper graph)  throws NotFoundVertexException;
	public  GraphWrapper ComputeOutDegree(GraphWrapper graph, Pair p, Boolean sparse) throws NotFoundVertexException;
	public  GraphWrapper ComputeOutDegree(GraphWrapper graph)  throws NotFoundVertexException;
	public  GraphWrapper ComputeGraphSize(GraphWrapper graph, Boolean sparse);
	public  GraphWrapper ComputeGraphOrder(GraphWrapper graph, Boolean sparse);
	public  GraphWrapper ComputeHood(GraphWrapper graph, Pair p, Boolean sparse, Boolean includeOrigin) throws NotFoundVertexException;
	public  GraphWrapper ComputeKmeansClusterSet(GraphWrapper graph, 
			Integer candidates);
	public  GraphWrapper ComputeKmeansCommunity(GraphWrapper graph, 
			Integer candidates, Pair p) throws NotFoundVertexException;
	public  GraphWrapper ComputeBiconnectedComponents(GraphWrapper graph, Boolean forceConversion) throws ClassCastException;
	public  GraphWrapper ComputeEBClusterSet(GraphWrapper graph, Integer n);
	public  GraphWrapper ComputeEBCluster(GraphWrapper graph, Integer n);
	//public  Collection<Set<Vertex>> ComputeCliques(GraphWrapper graph);
	public  GraphWrapper ComputeNumberEdgesByProperty(GraphWrapper graph, Pair p, Boolean sparse)throws Exception;
	public  GraphWrapper ComputeEdgePropertySet(GraphWrapper graph, Pair p)throws Exception;
	public  GraphWrapper ComputeVertexPropertySet(GraphWrapper graph, Pair p)throws Exception;
	public  GraphWrapper ComputeDiameter(GraphWrapper graph);
	public  GraphWrapper ComputeDensity(GraphWrapper graph, String type, Boolean sparse);
	public  GraphWrapper ComputeStrength(GraphWrapper graph, Pair p1, Pair p2, Boolean sparse) throws Exception;
	public  GraphWrapper ComputeDistance(GraphWrapper graph, Pair p1, Pair p2, Boolean sparse) throws Exception;
	public  GraphWrapper ComputeMultiplexity(GraphWrapper graph,  Pair p1, Pair p2, Boolean sparse) throws Exception;
	public  GraphWrapper ComputeVertexEigenvectorCentrality(GraphWrapper graph, Pair p, Boolean sparse)  throws NotFoundVertexException;
	public  GraphWrapper ComputeEigenvectorCentrality(GraphWrapper graph)  throws NotFoundVertexException;
	public  GraphWrapper ComputeBetweennessCentrality(GraphWrapper graph);
	public  GraphWrapper ComputeVertexBetweennessCentrality(GraphWrapper graph, Pair p, Boolean sparse)  throws NotFoundVertexException;
	public Boolean verifyVertex (GraphWrapper graph, Pair p)throws NotFoundVertexException;
}
