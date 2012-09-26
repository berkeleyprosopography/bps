/* Social Network Analysis Module
 * GraphML Input/Output manager 
 * 
 * Written by Davide Semenzin 
 * 
 * */

package edu.berkeley.bps.services.sna.ioutils;

import edu.berkeley.bps.services.sna.context.GraphContext;
import edu.berkeley.bps.services.sna.graph.components.Vertex;
import edu.berkeley.bps.services.sna.graph.components.VertexFactory;
import edu.berkeley.bps.services.sna.graph.components.EdgeFactory;
import edu.berkeley.bps.services.sna.graph.components.Edge;
import edu.berkeley.bps.services.sna.exceptions.ParsingErrorException;

import edu.berkeley.bps.services.common.ServiceContext;
import edu.berkeley.bps.services.common.SystemProperties;

import java.awt.Color;
import java.awt.Paint;
import java.io.*;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.io.graphml.EdgeMetadata;
import edu.uci.ics.jung.io.graphml.GraphMLReader2;
import edu.uci.ics.jung.io.graphml.GraphMetadata;
import edu.uci.ics.jung.io.graphml.HyperEdgeMetadata;
import edu.uci.ics.jung.io.graphml.NodeMetadata;



public class IO {
	

	/* Create the Graph Transformer */
	Transformer<GraphMetadata, Graph<Vertex, Edge>>
	graphTransformer = new Transformer<GraphMetadata,
	                          Graph<Vertex, Edge>>() {
		
	  public Graph<Vertex, Edge>
	      transform(GraphMetadata metadata) {
	        if (metadata.getEdgeDefault().equals(
	        metadata.getEdgeDefault().DIRECTED)) {
	            return new
	            DirectedSparseMultigraph<Vertex, Edge>();
	        } else return new
	            SparseMultigraph<Vertex, Edge>();
	        
	
	      }
	};
	
	VertexFactory vfact= new VertexFactory();

	/* Create the Vertex Transformer */
	Transformer<NodeMetadata, Vertex> vertexTransformer
	= new Transformer<NodeMetadata, Vertex>() {
	    public Vertex transform(NodeMetadata metadata) {
	        Vertex v = vfact.create(metadata.getProperties(), metadata.getId()); 
	        System.out.println(metadata.toString());
	        return v;
	    }
	};
	
	EdgeFactory efact= new EdgeFactory();
	/* Create the Edge Transformer */
	 Transformer<EdgeMetadata, Edge> edgeTransformer =
	 new Transformer<EdgeMetadata, Edge>() {
	     public Edge transform(EdgeMetadata metadata) {
	         Edge e = efact.create(metadata.isDirected(),metadata.getSource(),
	        		 metadata.getTarget(), metadata.getProperties());
	         return e;
	     }
	 };
	
	 /* Create the Hyperedge Transformer */
	 Transformer<HyperEdgeMetadata, Edge> hyperEdgeTransformer
	 = new Transformer<HyperEdgeMetadata, Edge>() {
	      public Edge transform(HyperEdgeMetadata metadata) {
		         Edge e = efact.create();
	          return e;
	      }
	 };
	
	 
	public GraphMLReader2 createGraphReader(String src) throws Exception{
		/* Create the graphMLReader2 */
		StringReader fileReader = new StringReader(src);
		GraphMLReader2
		<Graph<Vertex, Edge>, 
		Vertex, 
		Edge>
			graphReader = 
				new
				GraphMLReader2<Graph<Vertex, Edge>, Vertex, Edge>
					(fileReader, graphTransformer, vertexTransformer,
							edgeTransformer, hyperEdgeTransformer);
		return graphReader;
	}
	
	public Graph<Vertex, Edge> parse (String arg) throws Exception{
		Graph<Vertex, Edge> graph = new SparseMultigraph<Vertex, Edge>();
			GraphMLReader2<Graph<Vertex, Edge>, Vertex, Edge> r = createGraphReader(arg);
			graph = (Graph<Vertex, Edge>) r.readGraph();
		return graph;
	}
	
}