/* Social Network Analysis Module
 * RESTful resource class
 * 
 * Written by Davide Semenzin 
 * 
 * */

package edu.berkeley.bps.services.sna;

import edu.berkeley.bps.services.common.BaseResource;
import edu.berkeley.bps.services.common.ServiceContext;
import edu.berkeley.bps.services.common.SystemProperties;
import edu.berkeley.bps.services.sna.math.Sna;
import edu.berkeley.bps.services.sna.context.ContextManager;
import edu.berkeley.bps.services.sna.context.GraphContext;
import edu.berkeley.bps.services.sna.graph.GraphWrapper;
import edu.berkeley.bps.services.sna.graph.utils.Pair;
import edu.berkeley.bps.services.sna.exceptions.NotFoundEdgeException;
import edu.berkeley.bps.services.sna.exceptions.ParsingErrorException;
import edu.berkeley.bps.services.sna.exceptions.NotFoundVertexException;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.*;

import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;
import org.jboss.resteasy.spi.BadRequestException;
import org.jboss.resteasy.spi.*;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.bind.annotation.*;

import javax.ws.rs.core.Response.ResponseBuilder;

import java.lang.reflect.Method;
import java.util.*;


/**
 * SNA Resource 
 */
@Path("/sna")
public class SnaResource extends BaseResource {
	
	//Instance of the SNA module 
	private Sna SnaInstance = new Sna();	
	
	//Constructor
	
	public SnaResource() {}
	

	
	// Manages the exception handling in case a bad POST payload is received
	private GraphContext processInput(String payload) throws ParsingErrorException{
		try{
			GraphContext gc=ContextManager.getContext(payload);
			return gc;
			}
		catch (Exception e){
			throw new ParsingErrorException(e.getMessage());
		}
		
	}
	
	
	
	//Root - returns a list of the methods available
	@GET
	@Produces({"application/xml", "application/json"})
	@Wrapped(element="sna")
	public Object ListMethods() {
		try{
			Method[] methods =this.getClass().getDeclaredMethods();
			ListWrapper wrapper = new ListWrapper();
			for (Method method : methods) {
	            wrapper.add(method.getName());
	        }
			return wrapper;
			}catch(Exception e) {
				String tmp = "Error in ListMethods()\n"+ e.getLocalizedMessage();
				System.err.println(tmp);
				throw new WebApplicationException( 
						Response.status(
								Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		    }

    }
	
	//Utility class to marshal Lists as XML elements
	@XmlRootElement(name="Resources")
	private static class ListWrapper {
	       @XmlElement(name="Resource")
	       List<String> list=new ArrayList<String>();
	       public void MyResourceWrapper (){}
	       public void add(String s){ list.add(s);}
	 }
	
	/* -----------------------------------------------------------------------------------
	 *                                        GRAPH 
	 *  ----------------------------------------------------------------------------------
	 * */
	
	@POST
	@Consumes({"application/xml", "application/json"})
	@Produces({"application/xml", "application/json"})
	@Path("graph")
	public GraphWrapper RetrieveGraph(@Context ServletContext srvc, String payload) throws ParsingErrorException {
			return processInput(payload).getGraph();
	}
	
	
	@POST
	@Consumes({"application/xml", "application/json"})
	@Produces({"application/xml", "application/json"})
	@Path("graph/size")
	public GraphWrapper ProduceSize(@DefaultValue("true") @QueryParam("sparse") final boolean sparse,
			@Context ServletContext srvc,					
			String payload) throws ParsingErrorException{
			return SnaInstance.ComputeGraphSize(processInput(payload).getGraph(), sparse);
}
	
	
	@POST
	@Consumes({"application/xml", "application/json"})
	@Produces({"application/xml", "application/json"})
	@Path("graph/order")
	public GraphWrapper ProduceOrder(@DefaultValue("true") @QueryParam("sparse") final boolean sparse,
			@Context ServletContext srvc, String payload)throws ParsingErrorException {
		return SnaInstance.ComputeGraphOrder(processInput(payload).getGraph(), sparse);
    }
	
	/* -----------------------------------------------------------------------------------
	 *                               CENTRALITY MEASURES
	 *  ----------------------------------------------------------------------------------
	 * */
	
	@POST
	@Consumes({"application/xml", "application/json"})
	@Produces({"application/xml", "application/json"})
	@Path("centrality")
	public GraphWrapper ProduceCentrality(@Context ServletContext srvc, String payload) throws ParsingErrorException{
		return SnaInstance.ComputeBetweennessCentrality(processInput(payload).getGraph());
			
    }

	
	@POST
	@Consumes({"application/xml", "application/json"})
	@Produces({"application/xml", "application/json"})
	@Path("centrality/eigenvector")
	public GraphWrapper ProduceEigenvectorCentrality(
			@Context ServletContext srvc, String payload) throws ParsingErrorException, NotFoundVertexException{
			return SnaInstance.ComputeEigenvectorCentrality(processInput(payload).getGraph());
    }
	
	@POST
	@Consumes({"application/xml", "application/json"})
	@Produces({"application/xml", "application/json"})
	@Path("centrality/HITS")
	public GraphWrapper ProduceHITSCentrality(
			@Context ServletContext srvc, String payload) throws ParsingErrorException, NotFoundVertexException{
		return SnaInstance.ComputeHITS(processInput(payload).getGraph());
    }
	
	
	@POST
	@Consumes({"application/xml", "application/json"})
	@Produces({"application/xml", "application/json"})
	@Path("centrality/PageRank")
	public GraphWrapper ProducePageRankCentrality(
			@DefaultValue("0.5") @QueryParam("alpha") final Double alpha,
			@Context ServletContext srvc, String payload) throws ParsingErrorException, NotFoundVertexException{
		return SnaInstance.ComputePageRank(processInput(payload).getGraph(), alpha);
		
    }

	
	@POST
	@Consumes({"application/xml", "application/json"})
	@Produces({"application/xml", "application/json", "text/plain"})
	@Path("centrality/{key}")
	public GraphWrapper ProduceCentralityNode(
			@DefaultValue("false") @QueryParam("sparse") final boolean sparse,
			@DefaultValue("id") @QueryParam("property")final String property,
			@Context ServletContext srvc, 
		String payload, @PathParam("key") final String key) throws ParsingErrorException , NotFoundVertexException{
			return  SnaInstance.ComputeVertexBetweennessCentrality(processInput(payload).getGraph(), 
					new Pair(property,key), 
					sparse);
    }
	
	@POST
	@Consumes({"application/xml", "application/json"})
	@Produces({"application/xml", "application/json", "text/plain"})
	@Path("centrality/eigenvector/{key}")
	public GraphWrapper ProduceEigenvectorCentralityNode(
			@DefaultValue("false") @QueryParam("sparse") final boolean sparse,
			@DefaultValue("id") @QueryParam("property")final String property,
			@Context ServletContext srvc, 
		String payload, @PathParam("key") final String key)throws ParsingErrorException, NotFoundVertexException {
			return  SnaInstance.ComputeVertexEigenvectorCentrality(processInput(payload).getGraph(),
					new Pair(property,key), sparse);
		
    }
	
	@POST
	@Consumes({"application/xml", "application/json"})
	@Produces({"application/xml", "application/json", "text/plain"})
	@Path("centrality/HITS/{key}")
	public GraphWrapper ProduceHITSCentralityNode(
			@DefaultValue("false") @QueryParam("sparse") final boolean sparse,
			@DefaultValue("id") @QueryParam("property")final String property,
			@Context ServletContext srvc, 
		String payload, @PathParam("key") final String key)throws ParsingErrorException, NotFoundVertexException {
			return  SnaInstance.ComputeVertexHITS(processInput(payload).getGraph(), new Pair(property,key), sparse);
    }
	
	@POST
	@Consumes({"application/xml", "application/json"})
	@Produces({"application/xml", "application/json", "text/plain"})
	@Path("centrality/PageRank/{key}")
	public GraphWrapper ProducePageRankCentralityNode(
			@DefaultValue("false") @QueryParam("sparse") final boolean sparse,
			@DefaultValue("id") @QueryParam("property")final String property,
			@DefaultValue("0.5") @QueryParam("alpha") final Double alpha,
			@Context ServletContext srvc, 
		String payload, @PathParam("key") final String key) throws ParsingErrorException, NotFoundVertexException{
			return  SnaInstance.ComputeVertexPageRank(processInput(payload).getGraph(), new Pair(property,key), alpha, sparse);
		
    }
	
	/* -----------------------------------------------------------------------------------
	 *                        DEGREE MEASURES
	 *  ----------------------------------------------------------------------------------
	 * */
	
	@POST
	@Consumes({"application/xml", "application/json"})
	@Produces({"application/xml", "application/json"})
	@Path("degree")
	public GraphWrapper ProduceDegreeGraph(
			@DefaultValue("false") @QueryParam("normalized") final boolean normalized,
			@Context ServletContext srvc, String payload) throws ParsingErrorException, NotFoundVertexException{
				if (normalized) return SnaInstance.ComputeNormalizedDegree(processInput(payload).getGraph());
				else return SnaInstance.ComputeDegree(processInput(payload).getGraph());
    }
	
	
	 
	@POST
	@Consumes({"application/xml", "application/json"})
	@Produces({"application/xml", "application/json"})
	@Path("degree/in")
	public GraphWrapper ProduceInDegreeGraph(
			@Context ServletContext srvc, String payload)throws ParsingErrorException, NotFoundVertexException{
			return SnaInstance.ComputeInDegree(processInput(payload).getGraph());

    }
	
	@POST
	@Consumes({"application/xml", "application/json"})
	@Produces({"application/xml", "application/json"})
	@Path("degree/out")
	public GraphWrapper ProduceOutDegreeGraph(
			@Context ServletContext srvc, String payload) throws ParsingErrorException, NotFoundVertexException{
				return SnaInstance.ComputeOutDegree(processInput(payload).getGraph());
			
    }
	
		
	@POST
	@Consumes({"application/xml", "application/json"})
	@Produces({"application/xml", "application/json"})
	@Path("degree/{key}")
	public GraphWrapper ProduceDegree(
			@DefaultValue("false") @QueryParam("sparse") final boolean sparse,
			@DefaultValue("id") @QueryParam("property") final String property,
			@DefaultValue("false") @QueryParam("normalized") final boolean normalized,
			@Context ServletContext srvc, String payload, @PathParam("key")final String key) throws ParsingErrorException, NotFoundVertexException{
					if (normalized) return SnaInstance.ComputeNormalizedDegree(processInput(payload).getGraph(), new Pair(property,key), sparse);
					else
						return SnaInstance.ComputeDegree(processInput(payload).getGraph(), new Pair(property,key), sparse);
    }
	

	@POST
	@Consumes({"application/xml", "application/json"})
	@Produces({"application/xml", "application/json"})
	@Path("degree/in/{key}")
	public GraphWrapper ProduceInDegree(@DefaultValue("false") @QueryParam("sparse") final boolean sparse,
			@DefaultValue("id") @QueryParam("property") final String property,
			@Context ServletContext srvc, 
			String payload, @PathParam("key")final String key)throws ParsingErrorException, NotFoundVertexException{
					return  SnaInstance.ComputeOutDegree(processInput(payload).getGraph(), 
							new Pair(property,key), sparse);
    }
	
	@POST
	@Consumes({"application/xml", "application/json"})
	@Produces({"application/xml", "application/json"})
	@Path("degree/out/{key}")
	public GraphWrapper ProduceOutDegree(
			@DefaultValue("false") @QueryParam("sparse") final boolean sparse,
			@DefaultValue("id") @QueryParam("property") final String property,
			@Context ServletContext srvc, 
			String payload, @PathParam("key")final String key) throws ParsingErrorException, NotFoundVertexException{
					return  SnaInstance.ComputeOutDegree(processInput(payload).getGraph(), 
							new Pair(property,key), sparse);
    }
	
	/* -----------------------------------------------------------------------------------
	 *                              DENSITY MEASURES
	 *  ----------------------------------------------------------------------------------
	 * */
	
	@POST
	@Consumes({"application/xml", "application/json"})
	@Produces({"application/xml", "application/json"})
	@Path("density")
	public GraphWrapper ProduceDensity(
			@DefaultValue("false") @QueryParam("sparse") final boolean sparse,
			//@DefaultValue("null") @QueryParam("type") final String type,
			@Context ServletContext srvc, String payload)throws ParsingErrorException {
				return SnaInstance.ComputeDensity(processInput(payload).getGraph(), null, sparse);
		
    }

	/* -----------------------------------------------------------------------------------
	 *                        CLUSTER MEASURES
	 *  ----------------------------------------------------------------------------------
	 * */
	@POST
	@Consumes({"application/xml", "application/json"})
	@Produces({"application/xml", "application/json"})
	@Path("cluster/Kmeans")
	public GraphWrapper ProduceKmeansClusterSet(
			@DefaultValue("1") @QueryParam("candidates") final Integer candidates,
			@Context ServletContext srvc, String payload)throws ParsingErrorException{
		return SnaInstance.ComputeKmeansClusterSet(processInput(payload).getGraph(), candidates);
    }
	
	@POST
	@Consumes({"application/xml", "application/json"})
	@Produces({"application/xml", "application/json"})
	@Path("cluster/Kmeans/{key}")
	public GraphWrapper ProduceKmeansClusterSet(
			@DefaultValue("1") @QueryParam("candidates") final Integer candidates,
			@DefaultValue("id") @QueryParam("property") final String property,
			@PathParam("key")final String key,
			@Context ServletContext srvc, String payload) throws ParsingErrorException, NotFoundVertexException{
		return SnaInstance.ComputeKmeansCommunity(processInput(payload).getGraph(), candidates, new Pair(property,key));
    }

	@POST
	@Consumes({"application/xml", "application/json"})
	@Produces({"application/xml", "application/json"})
	@Path("cluster/edgeBetweenness")
	public GraphWrapper ProduceEBClusterSet(
			@DefaultValue("1") @QueryParam("threshold") final Integer threshold,
			@Context ServletContext srvc, String payload) throws ParsingErrorException{
		return SnaInstance.ComputeEBClusterSet(processInput(payload).getGraph(), threshold);
	
    }
	
	@POST
	@Consumes({"application/xml", "application/json"})
	@Produces({"application/xml", "application/json"})
	@Path("cluster/edgeBetweenness/pure")
	public GraphWrapper ProduceEBCluster(
			@DefaultValue("1") @QueryParam("threshold") final Integer threshold,
			@Context ServletContext srvc, String payload) throws ParsingErrorException{
		return SnaInstance.ComputeEBCluster(processInput(payload).getGraph(), threshold);
	
    }
	
	
	@POST
	@Consumes({"application/xml", "application/json"})
	@Produces({"application/xml", "application/json"})
	@Path("cluster/biconnectedComponents")
	public GraphWrapper ProduceBiconnectedComponents(
			@DefaultValue("false") @QueryParam("forceConversion") final Boolean forceConversion,
			@Context ServletContext srvc, String payload)throws ParsingErrorException, ClassCastException {
		return SnaInstance.ComputeBiconnectedComponents(processInput(payload).getGraph(), forceConversion);

    }
	
	@POST
	@Consumes({"application/xml", "application/json"})
	@Produces({"application/xml", "application/json"})
	@Path("cluster/neighborhood/{key}")
	public GraphWrapper ProduceNeighborhood(
			@DefaultValue("true") @QueryParam("sparse") final boolean sparse,
			@DefaultValue("true") @QueryParam("origin") final boolean origin,
			@DefaultValue("id") @QueryParam("property") final String property,
			@Context ServletContext srvc, String payload, 
			@PathParam("key")final String key) throws ParsingErrorException, NotFoundVertexException{
		return SnaInstance.ComputeHood(processInput(payload).getGraph(), 
				new Pair(property,key), 
				sparse,
				origin);

    }
	
	@POST
	@Consumes({"application/xml", "application/json"})
	@Produces({"application/xml", "application/json"})
	@Path("cluster/subset/edge/{value}")
	public GraphWrapper ProduceEdgeSubset(
			@DefaultValue("type") @QueryParam("key") final String key,
			@Context ServletContext srvc, String payload, 
			@PathParam("value")final String value) 
		throws ParsingErrorException, NotFoundVertexException,NotFoundEdgeException, Exception{
		return SnaInstance.ComputeEdgePropertySet(processInput(payload).getGraph(), 
				new Pair(key,value));
    }
	
	@POST
	@Consumes({"application/xml", "application/json"})
	@Produces({"application/xml", "application/json"})
	@Path("cluster/subset/edge/{v1},{v2}")
	public GraphWrapper ProduceEdgeSubsetFromEndpoints(
			@DefaultValue("id") @QueryParam("key") final String key,
			@Context ServletContext srvc, String payload, 
			@PathParam("v1")final String v1, 
			@PathParam("v2")final String v2) 
		throws ParsingErrorException, NotFoundVertexException,NotFoundEdgeException, Exception{
		return SnaInstance.ComputeEdgeSetFromEndpoints(processInput(payload).getGraph(), 
				new Pair(key,v1),
				new Pair(key,v2)
		);
    }
	
	@POST
	@Consumes({"application/xml", "application/json"})
	@Produces({"application/xml", "application/json"})
	@Path("cluster/subset/vertex/{value}")
	public GraphWrapper ProduceVertexSubset(
			@DefaultValue("id") @QueryParam("key") final String key,
			@Context ServletContext srvc, String payload,
			@PathParam("value")final String value) 
			throws ParsingErrorException, NotFoundVertexException{
		return SnaInstance.ComputeVertexPropertySet(processInput(payload).getGraph(), 
				new Pair(key,value));
    }
	

	
	@POST
	@Consumes({"application/xml", "application/json"})
	@Produces({"application/xml", "application/json"})
	@Path("path/strength/{v1},{v2}")
	public GraphWrapper ProduceStrength(@Context ServletContext srvc, 
			@DefaultValue("true") @QueryParam("sparse") final boolean sparse,
			@DefaultValue("id") @QueryParam("property") final String property,
			String payload, @PathParam("v1")final String v1, @PathParam("v2")final String v2) 
	throws ParsingErrorException, NotFoundEdgeException, NotFoundVertexException
			{
		try {return SnaInstance.ComputeStrength(processInput(payload).getGraph(), 
				new Pair(property,v1), 
				new Pair(property,v2),
				sparse);}
		catch (Exception e){
			throw new BadRequestException("Error in ProduceStrength\n" + e.getLocalizedMessage());	
		}

	}	
	
	@POST
	@Consumes({"application/xml", "application/json"})
	@Produces({"application/xml", "application/json"})
	@Path("path/shortest/{v1},{v2}")
	public GraphWrapper ProduceShortestPath(@Context ServletContext srvc, 
			@DefaultValue("true") @QueryParam("sparse") final boolean sparse,
			@DefaultValue("id") @QueryParam("property") final String property,
			String payload, @PathParam("v1")final String v1, @PathParam("v2")final String v2) 
	throws ParsingErrorException, IllegalArgumentException, NotFoundEdgeException, NotFoundVertexException {
		return SnaInstance.ComputeShortestPath(processInput(payload).getGraph(), 
				new Pair(property,v1), 
				new Pair(property,v2),
				sparse);
	}
	


}


