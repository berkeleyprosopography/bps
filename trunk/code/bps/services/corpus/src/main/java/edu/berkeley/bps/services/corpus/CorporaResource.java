package edu.berkeley.bps.services.corpus;

import edu.berkeley.bps.services.common.BaseResource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

/**
 * Resource that manages a list of items.
 * 
 */
@Path("/corpora")
public class CorporaResource extends BaseResource {
	
	private static final String myClass = "CorporaResource";

	/**
     * Returns a listing of all corpora.
	 * @return Full (shallow) details of all corpora
	 */
	@GET
	@Produces("application/xml")
	@Wrapped(element="corpora")
	public List<Corpus> getAll() {
		// TODO Add pagination support to the BaseResource
		final String SELECT_ALL = "SELECT id, name, description, owner_id FROM corpus";
        // Generate the right representation according to its media type.
		ArrayList<Corpus> corpusList = new ArrayList<Corpus>();
        try {
	        Connection dbConn = openConnection(false);

			PreparedStatement stmt = dbConn.prepareStatement(SELECT_ALL);
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
				Corpus corpus = new Corpus(rs.getInt("id"), rs.getString("name"), 
						rs.getString("description"), rs.getInt("owner_id"), null);
				corpusList.add(corpus);
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+".getAll(): Problem querying DB.\n"+ se.getMessage();
			System.out.println(tmp);
        	throw new WebApplicationException( 
        			Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    "Problem creating corpus\n"+se.getLocalizedMessage()).build());
        }
        return corpusList;
    }

	/**
     * Returns details for a given corpus.
	 * @param id the id of the corpus of interest
	 * @return
	 */
	@GET
	@Produces("application/xml")
	@Path("{id}")
	public Corpus getCorpus(@PathParam("id")int id) {
        try {
	        Connection dbConn = openConnection(false);
			Corpus corpus = Corpus.FindByID(dbConn, id);
			if(corpus==null) {
            	throw new WebApplicationException( 
            			Response.status(
            				Response.Status.NOT_FOUND).entity("No corpus found with id: "+id).build());
				
			}
			return corpus;
		} catch(SQLException se) {
			String tmp = myClass+".getCorpus(): Problem querying DB.\n"+ se.getLocalizedMessage();
			System.out.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
        }
    }

    /**
     * Creates a new corpus.
     * @param corpus the representation of the new corpus
     * @return Response, with the path (and so id) of the newly created corpus
     */
    @POST
	@Consumes("application/xml")
    public Response createCorpus(Corpus corpus){
    	Connection dbConn = openConnection(false);

        // Check that the item is not already registered.
        if (Corpus.NameUsed(dbConn, corpus.getName())) {
            String tmp = "A corpus with the name '" + corpus.getName() + "' already exists.";
        	throw new WebApplicationException( 
        			Response.status(
        				Response.Status.BAD_REQUEST).entity(tmp).build());
        } 
        // Persist the new item, and get an id for it
    	corpus.CreateAndPersist(dbConn);
        UriBuilder path = UriBuilder.fromResource(Corpus.class);
        path.path("" + corpus.getId());
        Response response = Response.created(path.build()).build();
        return response;
	}

    /**
     * Creates a new corpus.
     * @param form the representation of the new corpus as form data
     * @return Response, with the path (and so id) of the newly created corpus
     */
    @POST
    @Consumes("application/x-www-form-urlencoded")
    public Response createCorpus(MultivaluedMap<String, String> form) {
        String corpusName = form.getFirst("name");
        String corpusDescription = form.getFirst("description");
        String corpusOwnerStr = form.getFirst("owner");
    	if(corpusName==null||corpusName.length()==0) {
        	throw new WebApplicationException( 
    			Response.status(Response.Status.BAD_REQUEST).entity(
    					"Missing value for corpus name").build());
    	}
    	if(corpusOwnerStr==null||corpusOwnerStr.length()==0) {
        	throw new WebApplicationException( 
	    			Response.status(Response.Status.BAD_REQUEST).entity(
	    					"Missing value for corpus owner id").build());
    	}
    	int corpusOwner;
    	try {
    		corpusOwner = Integer.parseInt(corpusOwnerStr);
    	} catch( NumberFormatException nfe ) {
        	throw new WebApplicationException( 
    			Response.status(Response.Status.BAD_REQUEST).entity(
					"Illegal (non integer) value for corpus owner id").build());
    	}
    	Connection dbConn = openConnection(false);

        // Check that the item is not already registered.
        if (Corpus.FindByName(dbConn, corpusName)!=null) {
            String tmp = "A corpus with the name '" + corpusName + "' already exists.";
        	throw new WebApplicationException( 
        			Response.status(
        				Response.Status.BAD_REQUEST).entity(tmp).build());
        }
        // Register the new item
    	Corpus corpus = Corpus.CreateAndPersist(dbConn, corpusName, corpusDescription, corpusOwner, null);
        // Set the response's status and entity
        UriBuilder path = UriBuilder.fromResource(Corpus.class);
        path.path("" + corpus.getId());
        Response response = Response.created(path.build()).build();
        return response;
    }

    /**
     * Updates an existing corpus
	 * @param id the id of the corpus of interest
     * @param corpus the representation of the new corpus
     * @return Response, with the path (and so id) of the newly created corpus
     */
    @PUT
	@Consumes("application/xml")
	@Path("{id}")
    public Response updateCorpus(
    		@PathParam("id") int id, Corpus corpus){
        try {
	        Connection dbConn = openConnection(false);
            if(!Corpus.Exists(dbConn, id)) {
            	throw new WebApplicationException( 
            			Response.status(
            				Response.Status.NOT_FOUND).entity("No corpus found with id: "+id).build());
            }
            corpus.persist(dbConn);
		} catch(RuntimeException re) {
			String tmp = myClass+".updateCorpus(): Problem updating DB.\n"+ re.getLocalizedMessage();
			System.out.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
        }
        // Set the response's status and entity
        UriBuilder path = UriBuilder.fromResource(Corpus.class);
        path.path("" + id);
        Response response = Response.ok(path.build().toString()).build();
        return response;
	}

	/**
     * Deletes a given corpus.
	 * @param id the id of the corpus to delete
	 * @return
	 */
	@DELETE
	@Produces("application/xml")
	@Path("{id}")
	public Response deleteCorpus(@PathParam("id") int id) {
        try {
	        Connection dbConn = openConnection(false);
            if(!Corpus.Exists(dbConn, id)) {
            	throw new WebApplicationException( 
            			Response.status(
            				Response.Status.NOT_FOUND).entity("No corpus found with id: "+id).build());
            }
            Corpus.DeletePersistence(dbConn,id);
		} catch(RuntimeException re) {
			String tmp = myClass+".deleteCorpus(): Problem querying DB.\n"+ re.getLocalizedMessage();
			System.out.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
        }
        return Response.ok().build();
    }

    
}
