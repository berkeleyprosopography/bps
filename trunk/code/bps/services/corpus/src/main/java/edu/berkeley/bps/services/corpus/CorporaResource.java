package edu.berkeley.bps.services.corpus;

import edu.berkeley.bps.services.common.BaseResource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
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
     * Handle POST requests: create a new item.
    @Override
    public void acceptRepresentation(Representation entity)
            throws ResourceException {
        // Parse the given representation and retrieve pairs of
        // "name=value" tokens.
    	String mediaTypeName = entity.getMediaType().getName();
        Form form = new Form(entity);
        String corpusName = form.getFirstValue("name");
        String corpusDescription = form.getFirstValue("description");
        String corpusOwnerStr = form.getFirstValue("owner");
        Response response = getResponse();
        try {
        	if(corpusName==null||corpusName.length()==0)
        		throw new IllegalArgumentException("Missing value for corpus name");
        	if(corpusOwnerStr==null||corpusOwnerStr.length()==0)
        		throw new IllegalArgumentException("Missing value for corpus owner id");
        	int corpusOwner;
        	try {
        		corpusOwner = Integer.parseInt(corpusOwnerStr);
        	} catch( NumberFormatException nfe ) {
        		throw new IllegalArgumentException("Illegal (non integer) value for corpus owner id");
        	}
        	Connection dbConn = openConnection(false);

	        // Check that the item is not already registered.
	        if (Corpus.FindByName(dbConn, corpusName)!=null) {
	        	generateErrorRepresentation(
	                    "A corpus with the name '" + corpusName + "' already exists.", 
	                    	Status.CLIENT_ERROR_BAD_REQUEST, response );
	        } else {
	            // Register the new item
	        	Corpus corpus = Corpus.CreateAndPersist(dbConn, corpusName, corpusDescription, corpusOwner, null);
	            // Set the response's status and entity
	            response.setStatus(Status.SUCCESS_CREATED);
	            Representation rep = new StringRepresentation("Corpus created",
	                    MediaType.TEXT_PLAIN);
	            // Indicates where is located the new resource.
	            rep.setIdentifier(getRequest().getResourceRef().getIdentifier()
	                    + "/" + corpus.getId());
	            response.setEntity(rep);
	        }
        } catch(Exception e) {
        	generateErrorRepresentation(
                    "Problem creating corpus\n"+e.getLocalizedMessage(), 
                    	Status.CONNECTOR_ERROR_INTERNAL, response );
        }
    }
     */

    /**
     * Returns a listing of all corpora.
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
     * Returns a listing of a single corpus.
     */
	@GET
	@Produces("application/xml")
	@Path("{id}")
	public Corpus getCorpus(int id) {
        try {
	        Connection dbConn = openConnection(false);
			Corpus corpus = Corpus.FindByID(dbConn, id);
			return corpus;
		} catch(SQLException se) {
			String tmp = myClass+".getCorpus(): Problem querying DB.\n"+ se.getLocalizedMessage();
			System.out.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
        }
    }

    @POST
	@Consumes("application/xml")
    public Response createCorpus(Corpus corpus){
    	Connection dbConn = openConnection(false);

        // Check that the item is not already registered.
        if (Corpus.FindByName(dbConn, corpus.getName())!=null) {
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

    
}
