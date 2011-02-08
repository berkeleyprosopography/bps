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
@Path("/activities")
public class ActivityResource extends BaseResource {
	
	private static final String myClass = "ActivityResource";

	/**
     * Returns a listing of all activities.
	 * @return Full (shallow) details of all activities
	 */
	@GET
	@Produces({"application/xml", "application/json"})
	@Wrapped(element="activities")
	public List<Activity> getAll() {
		Connection dbConn = openConnection(false);
		List<Activity> activityList = Activity.ListAll(dbConn);
        return activityList;
    }

	/**
     * Returns details for a given activity.
	 * @param id the id of the activity of interest
	 * @return
	 */
	@GET
	@Produces({"application/xml", "application/json"})
	@Path("{id}")
	public Activity getActivity(@PathParam("id")int id) {
		Activity activity = null;
        try {
	        Connection dbConn = openConnection(false);
	        activity = Activity.FindByID(dbConn, id);
		} catch(Exception e) {
			String tmp = myClass+".getActivity(): Problem getting Activity.\n"+ e.getLocalizedMessage();
			System.out.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
        }
		if(activity==null) {
        	throw new WebApplicationException( 
        			Response.status(
        				Response.Status.NOT_FOUND).entity("No activity found with id: "+id).build());
			
		}
		return activity;
    }

    /**
     * Creates a new activity.
     * @param activity the representation of the new activity
     * @return Response, with the path (and so id) of the newly created activity
     */
    @POST
	@Consumes("application/xml")
    public Response createActivity(Activity activity){
    	Connection dbConn = openConnection(false);

        // Check that the item is not already registered.
        if (Activity.FindByName(dbConn, activity.getName())!=null) {
            String tmp = "An activity with the name '" + activity.getName() + "' already exists.";
        	throw new WebApplicationException( 
        			Response.status(
        				Response.Status.BAD_REQUEST).entity(tmp).build());
        } 
        try {
	        // Persist the new item, and get an id for it
        	activity.CreateAndPersist(dbConn);
	        UriBuilder path = UriBuilder.fromResource(Activity.class);
	        path.path("" + activity.getId());
	        Response response = Response.created(path.build()).build();
	        return response;
		} catch(Exception e) {
			String tmp = myClass+".createActivity(): Problem creating Activity.\n"+ e.getLocalizedMessage();
			System.out.println(tmp);
	    	throw new WebApplicationException( 
				Response.status(
					Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
	    }
	}


    /**
     * Updates an existing activity
	 * @param id the id of the activity of interest
     * @param activity the representation of the new activity
     * @return Response, with the path (and so id) of the newly created activity
     */
    @PUT
	@Consumes("application/xml")
	@Path("{id}")
    public Response updateActivity(
    		@PathParam("id") int id, Activity activity){
        try {
	        Connection dbConn = openConnection(false);
            if(!Activity.Exists(dbConn, id)) {
            	throw new WebApplicationException( 
            			Response.status(
            				Response.Status.NOT_FOUND).entity("No activity found with id: "+id).build());
            }
            activity.persist(dbConn);
		} catch(RuntimeException re) {
			String tmp = myClass+".updateCorpus(): Problem updating DB.\n"+ re.getLocalizedMessage();
			System.out.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
        }
        // Set the response's status and entity
        UriBuilder path = UriBuilder.fromResource(Activity.class);
        path.path("" + id);
        Response response = Response.ok(path.build().toString()).build();
        return response;
	}

	/**
     * Deletes a given activity.
	 * @param id the id of the activity to delete
	 * @return
	 */
	@DELETE
	@Produces("application/xml")
	@Path("{id}")
	public Response deleteActivity(@PathParam("id") int id) {
        try {
	        Connection dbConn = openConnection(false);
            if(!Activity.Exists(dbConn, id)) {
            	throw new WebApplicationException( 
            			Response.status(
            				Response.Status.NOT_FOUND).entity("No activity found with id: "+id).build());
            }
            Activity.DeletePersistence(dbConn,id);
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
