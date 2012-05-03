package edu.berkeley.bps.services.workspace;

import edu.berkeley.bps.services.common.BaseResource;
import edu.berkeley.bps.services.common.ServiceContext;
import edu.berkeley.bps.services.corpus.Activity;
import edu.berkeley.bps.services.corpus.ActivityRole;
import edu.berkeley.bps.services.corpus.CachedEntity;
import edu.berkeley.bps.services.corpus.Corpus;
import edu.berkeley.bps.services.corpus.Document;
import edu.berkeley.bps.services.corpus.Name;
import edu.berkeley.bps.services.corpus.NameRoleActivity;
import edu.berkeley.bps.services.workspace.collapser.CollapserBase;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 * Resource that manages a list of items.
 * 
 */
@Path("/workspaces")
public class WorkspaceResource extends BaseResource {
	
	private static final String myClass = "WorkspaceResource";
	
	private int getUserIdFromParams(UriInfo ui) {
		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
		String user = queryParams.getFirst("user");
		int user_id = -1;
		if(user != null) {
			try {
				user_id = Integer.parseInt(user);
			} catch( NumberFormatException nfe) {}
		}
		/* Nice to be able to list all, so let's do that
		if(user_id <=0 ) {
        	throw new WebApplicationException( 
        			Response.status(
        				Response.Status.NOT_FOUND).entity("Must specify user for workspaces!").build());
		}
		*/
		return user_id;
	}

	private Workspace getWorkspace(ServiceContext sc, int wkspId) {
		Workspace workspace = Workspace.FindByID(sc, wkspId);
		if(workspace==null) {
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.NOT_FOUND).entity(
    						"No workspace found with id: "+wkspId).build());
			
		}
		return workspace;
	}
	
	/**
     * Returns a listing of all workspaces.
	 * @return Full (shallow) details of all workspaces
	 */
	@GET
	@Produces({"application/xml", "application/json"})
	@Wrapped(element="workspaces")
	public List<Workspace> getAllForUser(
			@Context ServletContext srvc,
			@Context UriInfo ui) {
		try {
			int user_id = getUserIdFromParams(ui);
			List<Workspace> wkspList = 
				Workspace.ListAllForUser(getServiceContext(srvc), user_id);
			return wkspList;
		} catch(WebApplicationException wae) {
			throw wae;
		} catch(Exception e) {
			String tmp = myClass+".getAllForUser(): Problem getting Workspace List.\n"+ e.getLocalizedMessage();
			System.err.println(tmp);
			throw new WebApplicationException( 
					Response.status(
							Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
        }
    }

	/**
     * Returns details for a given workspace.
	 * @param id the id of the workspace of interest
	 * @return
	 */
	@GET
	@Produces({"application/xml", "application/json"})
	@Path("{id}")
	public Workspace getWorkspace(@Context ServletContext srvc,
			@PathParam("id")int id) {
        try {
			return getWorkspace(getServiceContext(srvc), id);
		} catch(WebApplicationException wae) {
			throw wae;
		} catch(Exception e) {
			String tmp = myClass+".getCorpus(): Problem getting Workspace.\n"+ e.getLocalizedMessage();
			System.err.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
        }
    }

    /**
     * Creates a new workspace.
     * @param workspace the representation of the new workspace
     * @return Response, with the path (and so id) of the newly created workspace
     */
    @POST
	@Consumes("application/xml")
	public Response createWorkspace(@Context ServletContext srvc, 
			@Context UriInfo ui, Workspace workspace){
    	try {
    		Connection dbConn = getServiceContext(srvc).getConnection();

    		// Persist the new item, and get an id for it
    		// Ensure this is recognized as new
    		workspace.setId(CachedEntity.UNSET_ID_VALUE);
    		workspace.persist(dbConn, CachedEntity.SHALLOW_PERSIST);
    		UriBuilder path = UriBuilder.fromResource(WorkspaceResource.class);
    		path.path("" + workspace.getId());
    		Response response = Response.created(path.build()).build();
    		return response;
    	} catch(WebApplicationException wae) {
    		throw wae;
    	} catch(Exception e) {
    		String tmp = myClass+".createWorkspace(): Problem creating Workspace.\n"+ e.getLocalizedMessage();
    		System.err.println(tmp);
    		throw new WebApplicationException( 
    				Response.status(
    						Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
    	}
    }

    /**
     * Updates an existing workspace
	 * @param id the id of the workspace of interest
     * @param workspace the representation of the new workspace
     * @return Response, with the path (and so id) of the newly created workspace
     */
    @PUT
	@Consumes("application/xml")
	@Path("{id}")
    public Response updateWorkspace(@Context ServletContext srvc, 
    		@PathParam("id") int id, Workspace workspace){
        try {
        	ServiceContext sc = getServiceContext(srvc);
            if(!Workspace.Exists(sc, id)) {
            	throw new WebApplicationException( 
            			Response.status(
            				Response.Status.NOT_FOUND).entity("No workspace found with id: "+id).build());
            }
            // Since all we're changing is the corpus fields, no need
            // to persist all the docs, etc.
            workspace.setId(id);	// ID must match
            workspace.persist(sc.getConnection(), CachedEntity.SHALLOW_PERSIST);
            // Set the response's status and entity
            UriBuilder path = UriBuilder.fromResource(WorkspaceResource.class);
            path.path("" + id);
            Response response = Response.ok(path.build().toString()).build();
            return response;
		} catch(RuntimeException re) {
			String tmp = myClass+".updateWorkspace(): Problem updating DB.\n"+ re.getLocalizedMessage();
			System.err.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
        }
	}

	/**
     * Deletes a given workspace.
	 * @param id the id of the workspace to delete
	 * @return
	 */
	@DELETE
	@Produces("application/xml")
	@Path("{id}")
	public Response deleteWorkspace(@Context ServletContext srvc, @PathParam("id") int id) {
        try {
        	ServiceContext sc = getServiceContext(srvc);
			Workspace workspace = getWorkspace(sc, id);
			workspace.deletePersistence(sc.getConnection());
	        return Response.ok().build();
		} catch(RuntimeException re) {
			String tmp = myClass+".deleteWorkspace(): Problem querying DB.\n"+ re.getLocalizedMessage();
			System.err.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
        }
    }
	
	/**
     * Returns details for a given workspace.
	 * @param id the id of the workspace of interest
	 * @return
	 */
	@GET
	@Produces({"application/xml", "application/json"})
	@Path("{id}/collapser")
	public CollapserBase getCollapser(@Context ServletContext srvc,
			@PathParam("id")int id) {
        try {
        	ServiceContext sc = getServiceContext(srvc);
			Workspace workspace = getWorkspace(sc, id);
			return workspace.getCollapser();
		} catch(WebApplicationException wae) {
			throw wae;
		} catch(Exception e) {
			String tmp = myClass+".getCollapser(): Problem getting Collapser info.\n"+ e.getLocalizedMessage();
			System.err.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
        }
    }

    /**
     * Updates an existing workspace
	 * @param id the id of the workspace of interest
     * @param workspace the representation of the new workspace
     * @return Response, with the path (and so id) of the newly created workspace
     */
    @PUT
	@Consumes("application/xml")
	@Path("{id}/collapser")
    public Response updateCollapser(@Context ServletContext srvc, 
    		@PathParam("id") int id, CollapserBase collapser){
        try {
        	ServiceContext sc = getServiceContext(srvc);
			Workspace workspace = getWorkspace(sc, id);
            // Since all we're changing is the corpus fields, no need
            // to persist all the docs, etc.
            workspace.updateCollapser(collapser);
            // Set the response's status and entity
            UriBuilder path = UriBuilder.fromResource(WorkspaceResource.class);
    		path.path(id + "/collapser");
            Response response = Response.ok(path.build().toString()).build();
            return response;
		} catch(RuntimeException re) {
			String tmp = myClass+".updateWorkspace(): Problem updating DB.\n"+ re.getLocalizedMessage();
			System.err.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
        }
	}
	
	private Corpus parsePayloadToGetCorpus(ServiceContext sc, String payload) {
    	int corpusID = Integer.parseInt(payload);
		Corpus corpus = Corpus.FindByID(sc, corpusID);
		if(corpus==null) {
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.NOT_FOUND).entity(
   						"No corpus found with id: "+corpusID).build());
		}
		return corpus;
	}
	
	private static final boolean MATCH_EXISTING_CORPUS = true;
	private static final boolean ADD_NEW_CORPUS = false;
	
	private Response addFromCorpusInt(ServletContext srvc, 
    		int wkspId, String payload, boolean mustMatchExisting) {
        try {
        	ServiceContext sc = getServiceContext(srvc);
    		// Do the import corpus into workspace.
    		// For now, if there is a corpus, blow it and all resources away.
    		// Also blow away all synthesized resources like Persons, Clans, etc. 
    		// Response should just be okay or failure
			Workspace workspace = getWorkspace(sc, wkspId);
			Corpus corpusToAdd = parsePayloadToGetCorpus(sc, payload);
			if(mustMatchExisting
					&& (corpusToAdd.getId()!=workspace.getBuiltFromCorpus())) {
				String tmp = myClass+".refreshFromCorpus(): Unknown Corpus specifier in payload: "
				+payload;
				System.err.println(tmp);
		    	throw new WebApplicationException( 
					Response.status(
						Response.Status.BAD_REQUEST).entity(tmp).build());

			}
			int wkspOwnerId = workspace.getOwner_id();
			Corpus newCorpus = corpusToAdd.cloneForWorkspace(sc, wkspId, wkspOwnerId);
			// setCorpus() will clear out all existing resources.
			workspace.setCorpus(sc, newCorpus);
			UriBuilder path = UriBuilder.fromResource(WorkspaceResource.class);
			path.path(wkspId + "/corpora/");
            Response response = Response.ok(path.build().toString()).build();
	        return response;
		} catch(WebApplicationException wae) {
			throw wae;
		} catch(NumberFormatException nfe) {
			String tmp = myClass+".addFromCorpus(): Illegal Corpus specifier in payload: "
			+payload+ nfe.getLocalizedMessage();
			System.err.println(tmp);
	    	throw new WebApplicationException( 
				Response.status(
					Response.Status.BAD_REQUEST).entity(tmp).build());
		} catch(Exception e) {
			String tmp = myClass+".addFromCorpus(): Problem adding Corpus\n"+ e.getLocalizedMessage();
			System.err.println(tmp);
	    	throw new WebApplicationException( 
				Response.status(
					Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
	    }
	}

    /**
     * Creates a new activity.
     * @param activity the representation of the new activity
     * @return Response, with the path (and so id) of the newly created activity
     */
    @POST
	@Consumes("text/plain")
	@Path("{id}/corpora")
    public Response addFromCorpus(@Context ServletContext srvc, 
    		@PathParam("id") int wkspId, String payload){
    	return addFromCorpusInt( srvc, wkspId, payload, ADD_NEW_CORPUS);
	}


    /**
     * Updates an existing activity
	 * @param id the id of the activity of interest
     * @param activity the representation of the new activity
     * @return Response, with the path (and so id) of the newly created activity
     */
    @PUT
	@Consumes("text/plain")
	@Path("{id}/corpora")
    public Response refreshFromCorpus(@Context ServletContext srvc, 
    		@PathParam("id") int wkspId, String payload) {
    	return addFromCorpusInt( srvc, wkspId, payload, MATCH_EXISTING_CORPUS);
	}
	
	private final static boolean FAIL_ON_NO_CORPUS = true;
	private final static boolean NO_CORPUS_OKAY = false;
	private Corpus getWorkspaceCorpus(ServiceContext sc, int wkspId,
			boolean fFailOnNoCorpus) {
		Workspace workspace = getWorkspace(sc, wkspId);
		// Need to get the corpus for this workspace, and fetch its documents
		Corpus corpus = workspace.getCorpus();
		if(fFailOnNoCorpus && corpus == null) {
        	throw new WebApplicationException( 
    			Response.status(
				Response.Status.NOT_FOUND).entity(
				"No corpus assigned to workspace - cannot get/modify sub-resources").build());
		}
		return corpus;
	}
	
    /**
     * HACK - rebuilds the maps of Persons and Clans from the corpus
	 * @param id the id of the activity of interest
     * @param activity the representation of the new activity
     * @return Response, with the path (and so id) of the newly created activity
     */
    @PUT
	@Consumes("text/plain")
	@Path("{id}/corpora/entities")
    public Response rebuildEntitiesFromCorpus(@Context ServletContext srvc, 
    		@PathParam("id") int wkspId, String payload) {
        try {
        	ServiceContext sc = getServiceContext(srvc);
        	Workspace workspace = getWorkspace(sc, wkspId);
        	workspace.rebuildEntitiesFromCorpus(sc);
        	UriBuilder path = UriBuilder.fromResource(WorkspaceResource.class);
        	path.path(wkspId + "/corpora/entities");
        	Response response = Response.ok(path.build().toString()).build();
        	return response;
        } catch(WebApplicationException wae) {
        	throw wae;
        } catch(Exception e) {
        	String tmp = myClass+".rebuildEntitiesFromCorpus(): Problem rebuilding\n"+ e.getLocalizedMessage();
        	System.err.println(tmp);
        	throw new WebApplicationException( 
        			Response.status(
        					Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
        }
	}
		
	/**
     * Gets documents associated with a given corpus.
	 * @param id the id of the corpus
	 * @return
	 */
	@GET
	@Produces({"application/xml", "application/json"})
	@Wrapped(element="documents")
	@Path("{id}/documents")
	public List<Document> getDocuments(
			@Context ServletContext srvc, @Context UriInfo ui,
			@PathParam("id") int id) {
		List<Document> docList = null;
        try {
			MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
			// look for order-by setting
			String orderByParam = queryParams.getFirst("o");
			// Default to the "no workspace" value of 0 to get the unattached corpora
			int orderBy;
			if(orderByParam==null 
					|| Corpus.ORDER_DOCS_BY_ALT_ID_PARAM.equalsIgnoreCase(orderByParam)) {
				orderBy = Corpus.ORDER_DOCS_BY_ALT_ID;
			} else if(Corpus.ORDER_DOCS_BY_DATE_PARAM.equalsIgnoreCase(orderByParam)) {
				orderBy = Corpus.ORDER_DOCS_BY_DATE;
			} else {
            	throw new WebApplicationException( 
        			Response.status(
        				Response.Status.BAD_REQUEST).entity(
        					"Unrecognized order spec: "+orderByParam).build());
			}
			// Need to get the corpus for this workspace, and fetch its documents
			Corpus corpus = getWorkspaceCorpus(getServiceContext(srvc), id, NO_CORPUS_OKAY);
			if(corpus!=null) {
				docList = corpus.getDocuments(orderBy);
			} else {
				docList = new ArrayList<Document>();
			}
            return docList;
		} catch(RuntimeException re) {
			String tmp = myClass+".getDocuments(): Problem querying DB.\n"+ re.getLocalizedMessage();
			System.err.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
        }
    }
	
	/**
	 * @return Document for the given id
	 */
	@GET
	@Produces({"application/xml", "application/json"})
	@Path("{id}/documents/{docspec}")
	public Document getDocument(@Context ServletContext srvc, 
			@PathParam("id") int id, @PathParam("docspec") String docspec) {
		Document document = null;
        try {
	        document = getDocument(getServiceContext(srvc), id, docspec);
	        return document;
		} catch(RuntimeException re) {
			String tmp = myClass+".getDocument(): Problem querying DB.\n"+ re.getLocalizedMessage();
			System.err.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
        }
    }

	/**
	 * @return Document for the given id
	 */
	protected Document getDocument(ServiceContext sc, 
			int wkspid, String docspec) {
		Document document = null;
        try {
			Corpus corpus = getWorkspaceCorpus(sc, wkspid, FAIL_ON_NO_CORPUS);
			int did;
			try {
				did = Integer.parseInt(docspec);
			} catch(NumberFormatException nfe) {
				did = -1;
			}
			if(did>0) {
				document = corpus.getDocument(did);
			} else {
				// TODO add a second map by alt_id to corpus
				document = Document.FindByAltID(sc.getConnection(), corpus, docspec);
			}
			if(document==null) {
            	throw new WebApplicationException( 
            			Response.status(
            				Response.Status.NOT_FOUND).entity("No document found with specifier: "+docspec).build());
				
			}
	        return document;
		} catch(RuntimeException re) {
			String tmp = myClass+".getDocument(): Problem querying DB.\n"+ re.getLocalizedMessage();
			System.err.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
        }
    }

	/**
     * Gets documents associated with a given corpus.
	 * @param id the id of the corpus
	 * @return
	 */
	@GET
	@Produces({"application/xml;charset=UTF-8", "application/json;charset=UTF-8"})
	@Wrapped(element="nrads")
	@Path("{id}/documents/{docspec}/nrads")
	public List<NameRoleActivity> getDocumentNRADs(
			@Context ServletContext srvc, @Context UriInfo ui,
			@PathParam("id") int id, @PathParam("docspec") String docspec) {
		List<NameRoleActivity> nradList = null;
        try {
	        Document document = getDocument(getServiceContext(srvc), id, docspec);
			MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
			String filter = queryParams.getFirst("filter");
			if(filter != null && "nofamily".equals(filter)) {
		        nradList = document.getNonFamilyNameRoleActivities();
			} else {
		        nradList = document.getNameRoleActivities(true);
			}
	        return nradList;
		} catch(RuntimeException re) {
			String tmp = myClass+".getDocumentNRADs(): Problem querying DB.\n"+ re.getLocalizedMessage();
			System.err.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
        }
    }
	
	/**
     * Gets documents associated with a given corpus.
	 * @param id the id of the corpus
	 * @return
	 */
	@GET
	@Produces({"application/xml;charset=UTF-8", "application/json;charset=UTF-8"})
	@Wrapped(element="nradToEntityLinks")
	@Path("{wid}/documents/{docId}/nrads/links")
	public List<NRADEntityLink> getEntityLinksForDoc(
			@Context ServletContext srvc, @Context UriInfo ui,
			@PathParam("wid") int wid, @PathParam("docId") int docId) {
        try {
        	Workspace workspace = getWorkspace(srvc, wid);
        	return workspace.getEntityLinksForDoc(docId);
		} catch(RuntimeException re) {
			String tmp = myClass+".getDocumentNRADs(): Problem querying DB.\n"+ re.getLocalizedMessage();
			System.err.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
        }
    }
	

	/**
     * We do not expose "Delete documents" associated with a given workspace.
     * We manage them indirectly through the corpus import and update
     * 	@DELETE
     * 	@Produces("application/xml")
     * 	@Path("{id}/documents")
	 */
	
/*********************************************************************************
 * Begin Activity Sub-resource declarations
 *********************************************************************************/

	/**
     * Returns a listing of all activities.
	 * @return Full (shallow) details of all activities
	 */
	@GET
	@Produces({"application/xml", "application/json"})
	@Path("{id}/activities")
	@Wrapped(element="activities")
	public List<Activity> getActivities(@Context ServletContext srvc, @PathParam("id") int id) {
		List<Activity> activityList = null;
        try {
			Corpus corpus = getWorkspaceCorpus(getServiceContext(srvc), id, NO_CORPUS_OKAY);
			if(corpus != null) {
				activityList = corpus.getActivities();
			} else {
				activityList = new ArrayList<Activity>();
			}
	        return activityList;
		} catch(RuntimeException re) {
			String tmp = myClass+".getActivities(ServletContext, int)(): Problem querying DB.\n"+ re.getLocalizedMessage();
			System.err.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
        }
    }

	/**
     * Returns an Activity SubResource for a given activity
	 * @return ActivityResource for the given id
	 */
	@GET
	@Produces({"application/xml", "application/json"})
	@Path("{id}/activities/{aid}")
	public Activity getActivity(@Context ServletContext srvc, 
			@PathParam("id") int id, @PathParam("aid") int aid) {
		Activity activity = null;
        try {
			Corpus corpus = getWorkspaceCorpus(getServiceContext(srvc), id, FAIL_ON_NO_CORPUS);
			activity = corpus.findActivity(aid);
			if(activity==null) {
            	throw new WebApplicationException( 
            			Response.status(
            				Response.Status.NOT_FOUND).entity("No activity found with id: "+aid).build());
				
			}
	        return activity;
		} catch(RuntimeException re) {
			String tmp = myClass+".getActivity(): Problem querying DB.\n"+ re.getLocalizedMessage();
			System.err.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
        }
    }

    /**
     * Creates a new activity.
     * @param activity the representation of the new activity
     * @return Response, with the path (and so id) of the newly created activity
     */
    @POST
	@Consumes("application/xml")
	@Path("{id}/activities")
    public Response createActivity(@Context ServletContext srvc, 
    		@PathParam("id") int id, Activity activity){

        try {
        	ServiceContext sc = getServiceContext(srvc);
			Corpus corpus = getWorkspaceCorpus(sc, id, FAIL_ON_NO_CORPUS);
        	// Check that the item is not already registered.
        	if (Activity.FindByName(sc.getConnection(), corpus, activity.getName())!=null) {
        		String tmp = "An activity with the name '" + activity.getName() + "' already exists.";
        		throw new WebApplicationException( 
        				Response.status(
        						Response.Status.BAD_REQUEST).entity(tmp).build());
        	} 
    		activity.setCorpus(corpus);
	        // Persist the new item, and get an id for it
        	activity.CreateAndPersist(sc.getConnection());
        	corpus.addActivity(activity);
	        UriBuilder path = UriBuilder.fromResource(WorkspaceResource.class);
  			path.path(id + "/activities/" + activity.getId());
	        Response response = Response.created(path.build()).build();
	        return response;
		} catch(WebApplicationException wae) {
			throw wae;
		} catch(Exception e) {
			String tmp = myClass+".createActivity(): Problem creating Activity.\n"+ e.getLocalizedMessage();
			System.err.println(tmp);
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
	 * WE SHOULD NOT EXPOSE THIS - IT SHOULD BE DONE WITH ASSERTIONS
    @PUT
	@Consumes("application/xml")
	@Path("{id}/activities/{aid}")
    public Response updateActivity(@Context ServletContext srvc, 
    		@PathParam("id") int id, @PathParam("aid") int aid, Activity activity){
        try {
    		Connection dbConn = getServiceContext(srvc).getConnection();
			Corpus corpus = getWorkspaceCorpus(dbConn, id, FAIL_ON_NO_CORPUS);
            if(!Activity.Exists(dbConn, corpus, aid)) {
            	throw new WebApplicationException( 
            			Response.status(
            				Response.Status.NOT_FOUND).entity("No activity found with id: "+aid).build());
            }
            activity.setId(aid);		// Enforce payload and resource coherence
    		activity.setCorpus(corpus);	// Ensure we have proper linkage
            activity.persist(dbConn);
	        // Set the response's status and entity
	        UriBuilder path = UriBuilder.fromResource(WorkspaceResource.class);
			path.path(id + "/activities/" + aid);
	        Response response = Response.ok(path.build().toString()).build();
	        return response;
		} catch(RuntimeException re) {
			String tmp = myClass+".updateActivity(): Problem updating DB.\n"+ re.getLocalizedMessage();
			System.err.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
        }
	}
     */

	/**
     * Deletes a given activity.
	 * @param id the id of the activity to delete
	 * @return
	 * WE SHOULD NOT EXPOSE THIS - IT SHOULD BE DONE WITH ASSERTIONS
	@DELETE
	@Produces("application/xml")
	@Path("{id}/activities/{aid}")
	public Response deleteActivity(@Context ServletContext srvc, 
			@PathParam("id") int id, @PathParam("aid") int aid) {
        try {
    		Connection dbConn = getServiceContext(srvc).getConnection();
			Corpus corpus = getWorkspaceCorpus(dbConn, id, FAIL_ON_NO_CORPUS);
            if(!Activity.Exists(dbConn, corpus, aid)) {
            	throw new WebApplicationException( 
            			Response.status(
            				Response.Status.NOT_FOUND).entity("No activity found with id: "+id).build());
            }
            Activity.DeletePersistence(dbConn, corpus, aid);
	        return Response.ok().build();
		} catch(RuntimeException re) {
			String tmp = myClass+".deleteActivity(): Problem querying DB.\n"+ re.getLocalizedMessage();
			System.err.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
        }
    }
	 */
	
/*********************************************************************************
 * Begin ActivityRole Sub-resource declarations
 *********************************************************************************/

	/**
	 * Returns a listing of all activityRoles.
	 * @return Full (shallow) details of all activityRoles
	 */
	@GET
	@Produces({"application/xml", "application/json"})
	@Path("{id}/activityRoles")
	@Wrapped(element="activityRoles")
	public List<ActivityRole> getActivityRoles(@Context ServletContext srvc, @PathParam("id") int id) {
		List<ActivityRole> activityRoleList = null;
		try {
			Corpus corpus = getWorkspaceCorpus(getServiceContext(srvc), id, NO_CORPUS_OKAY);
			if(corpus!=null) {
				activityRoleList = corpus.getActivityRoles();
			} else {
				activityRoleList = new ArrayList<ActivityRole>();
			}
			return activityRoleList;
		} catch(RuntimeException re) {
			String tmp = myClass+".getActivityRoles(): Problem querying DB.\n"+ re.getLocalizedMessage();
			System.err.println(tmp);
			throw new WebApplicationException( 
					Response.status(
							Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		}
	}

	/**
	 * Returns an ActivityRole SubResource for a given activityRole
	 * @return ActivityRoleResource for the given id
	 */
	@GET
	@Produces({"application/xml", "application/json"})
	@Path("{id}/activityRoles/{arid}")
	public ActivityRole getActivityRole(@Context ServletContext srvc, 
			@PathParam("id") int id, @PathParam("arid") int arid) {
		ActivityRole activityRole = null;
		try {
			Corpus corpus = getWorkspaceCorpus(getServiceContext(srvc), id, FAIL_ON_NO_CORPUS);
			activityRole = corpus.findActivityRole(arid);
			if(activityRole==null) {
				throw new WebApplicationException( 
					Response.status(
						Response.Status.NOT_FOUND).entity(
								"No activityRole found with id: "+arid).build());

			}
			return activityRole;
		} catch(RuntimeException re) {
			String tmp = myClass+".getActivityRole(): Problem querying DB.\n"+ re.getLocalizedMessage();
			System.err.println(tmp);
			throw new WebApplicationException( 
					Response.status(
							Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		}
	}

	/**
	 * Creates a new activityRole.
	 * @param activityRole the representation of the new activityRole
	 * @return Response, with the path (and so id) of the newly created activityRole
	 */
	@POST
	@Consumes("application/xml")
	@Path("{id}/activityRoles")
	public Response createActivityRole(@Context ServletContext srvc, 
			@PathParam("id") int id, ActivityRole activityRole){

		try {
			ServiceContext sc = getServiceContext(srvc);
			Corpus corpus = getWorkspaceCorpus(sc, id, FAIL_ON_NO_CORPUS);
			// Check that the item is not already registered.
			if (ActivityRole.FindByName(sc.getConnection(), corpus, activityRole.getName())!=null) {
				String tmp = "An activityRole with the name '" + activityRole.getName() + "' already exists.";
				throw new WebApplicationException( 
						Response.status(
								Response.Status.BAD_REQUEST).entity(tmp).build());
			} 
			activityRole.setCorpus(corpus);
			// Persist the new item, and get an id for it
			activityRole.CreateAndPersist(sc.getConnection());
        	corpus.addActivityRole(activityRole);
			UriBuilder path = UriBuilder.fromResource(WorkspaceResource.class);
			path.path(id + "/activityRoles/" + activityRole.getId());
			Response response = Response.created(path.build()).build();
			return response;
		} catch(WebApplicationException wae) {
			throw wae;
		} catch(Exception e) {
			String tmp = myClass+".createActivityRole(): Problem creating ActivityRole.\n"+ e.getLocalizedMessage();
			System.err.println(tmp);
			throw new WebApplicationException( 
					Response.status(
							Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		}
	}


	/**
	 * Updates an existing activityRole
	 * @param id the id of the activityRole of interest
	 * @param activityRole the representation of the new activityRole
	 * @return Response, with the path (and so id) of the newly created activityRole
	 * WE SHOULD NOT EXPOSE THIS - IT SHOULD BE DONE WITH ASSERTIONS
	@PUT
	@Consumes("application/xml")
	@Path("{id}/activityRoles/{aid}")
	public Response updateActivityRole(@Context ServletContext srvc, 
			@PathParam("id") int id, @PathParam("aid") int aid, ActivityRole activityRole){
		try {
			Connection dbConn = getServiceContext(srvc).getConnection();
			Corpus corpus = getWorkspaceCorpus(dbConn, id, FAIL_ON_NO_CORPUS);
			if(!ActivityRole.Exists(dbConn, corpus, aid)) {
				throw new WebApplicationException( 
						Response.status(
								Response.Status.NOT_FOUND).entity("No activityRole found with id: "+id).build());
			}
			activityRole.setId(aid);		// Enforce payload and resource coherence
			activityRole.setCorpus(corpus);	// Ensure we have proper linkage
			activityRole.persist(dbConn);
			// Set the response's status and entity
			UriBuilder path = UriBuilder.fromResource(WorkspaceResource.class);
			path.path(id + "/activityRoles/" + aid);
			Response response = Response.ok(path.build().toString()).build();
			return response;
		} catch(RuntimeException re) {
			String tmp = myClass+".updateActivityRole(): Problem updating DB.\n"+ re.getLocalizedMessage();
			System.err.println(tmp);
			throw new WebApplicationException( 
					Response.status(
							Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		}
	}
	 */

	/**
	 * Deletes a given activityRole.
	 * @param id the id of the activityRole to delete
	 * @return
	 * WE SHOULD NOT EXPOSE THIS - IT SHOULD BE DONE WITH ASSERTIONS
	@DELETE
	@Produces("application/xml")
	@Path("{id}/activityRoles/{aid}")
	public Response deleteActivityRole(@Context ServletContext srvc, 
			@PathParam("id") int id, @PathParam("aid") int aid) {
		try {
			Connection dbConn = getServiceContext(srvc).getConnection();
			Corpus corpus = getWorkspaceCorpus(dbConn, id, FAIL_ON_NO_CORPUS);
			if(!ActivityRole.Exists(dbConn, corpus, aid)) {
				throw new WebApplicationException( 
						Response.status(
								Response.Status.NOT_FOUND).entity("No activityRole found with id: "+id).build());
			}
			ActivityRole.DeletePersistence(dbConn, corpus, aid);
			return Response.ok().build();
		} catch(RuntimeException re) {
			String tmp = myClass+".deleteActivityRole(): Problem querying DB.\n"+ re.getLocalizedMessage();
			System.err.println(tmp);
			throw new WebApplicationException( 
					Response.status(
							Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		}
	}
	 */
	
/*********************************************************************************
 * Begin Name Sub-resource declarations
 *********************************************************************************/

	/**
	 * Returns a listing of all activityRoles.
	 * @return Full (shallow) details of all activityRoles
	 */
	@GET
	@Produces({"application/xml", "application/json"})
	@Path("{id}/names")
	@Wrapped(element="names")
	public List<Name> getNames(@Context ServletContext srvc, @PathParam("id") int id) {
		List<Name> nameList = null;
		try {
			Corpus corpus = getWorkspaceCorpus(getServiceContext(srvc), id, NO_CORPUS_OKAY);
			if(corpus!=null) {
				nameList = corpus.getNames();
			} else {
				nameList = new ArrayList<Name>();
			}
			return nameList;
		} catch(RuntimeException re) {
			String tmp = myClass+".getNames(): Problem querying DB.\n"+ re.getLocalizedMessage();
			System.err.println(tmp);
			throw new WebApplicationException( 
					Response.status(
							Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		}
	}

	/**
	 * Returns a Name SubResource for a given name
	 * @return Name for the given id
	 */
	@GET
	@Produces({"application/xml", "application/json"})
	@Path("{id}/names/{nid}")
	public Name getName(@Context ServletContext srvc, 
			@PathParam("id") int id, @PathParam("nid") int nid) {
		Name name = null;
		try {
			Corpus corpus = getWorkspaceCorpus(getServiceContext(srvc), id, FAIL_ON_NO_CORPUS);
			name = corpus.findName(nid);
			if(name==null) {
				throw new WebApplicationException( 
					Response.status(
						Response.Status.NOT_FOUND).entity("No name found with id: "+nid).build());

			}
			return name;
		} catch(RuntimeException re) {
			String tmp = myClass+".getActivityRole(): Problem querying DB.\n"+ re.getLocalizedMessage();
			System.err.println(tmp);
			throw new WebApplicationException( 
					Response.status(
							Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		}
	}

	/**
	 * Creates a new name.
	 * @param name the representation of the new Name
	 * @return Response, with the path (and so id) of the newly created Name
	 */
	@POST
	@Consumes("application/xml")
	@Path("{id}/names")
	public Response createName(@Context ServletContext srvc, 
			@PathParam("id") int id, Name name){

		try {
			ServiceContext sc = getServiceContext(srvc);
			Corpus corpus = getWorkspaceCorpus(sc, id, FAIL_ON_NO_CORPUS);
			// Check that the item is not already registered.
			if (corpus.findName(name.getName())!=null) {
				String tmp = "A name with the name '" + name.getName() + "' already exists.";
				throw new WebApplicationException( 
						Response.status(
								Response.Status.BAD_REQUEST).entity(tmp).build());
			} 
			name.setCorpusId(corpus.getId());
			// Persist the new item, and get an id for it
			name.persist(sc.getConnection());
			// Add to the corpus maps
			corpus.addName(name);
			UriBuilder path = UriBuilder.fromResource(WorkspaceResource.class);
			path.path(id + "/names/" + name.getId());
			Response response = Response.created(path.build()).build();
			return response;
		} catch(WebApplicationException wae) {
			throw wae;
		} catch(Exception e) {
			String tmp = myClass+".createName(): Problem creating Name.\n"+ e.getLocalizedMessage();
			System.err.println(tmp);
			throw new WebApplicationException( 
					Response.status(
							Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		}
	}


	/**
	 * Updates an existing Name
	 * @param id the id of the activityRole of interest
	 * @param name the representation of the new Name
	 * @return Response, with the path (and so id) of the updated name
	 * WE SHOULD NOT EXPOSE THIS - IT SHOULD BE DONE WITH ASSERTIONS
	@PUT
	@Consumes("application/xml")
	@Path("{id}/names/{nid}")
	public Response updateName(@Context ServletContext srvc, 
			@PathParam("id") int id, @PathParam("nid") int nid, Name name){
		try {
			Connection dbConn = getServiceContext(srvc).getConnection();
			Corpus corpus = getWorkspaceCorpus(dbConn, id, FAIL_ON_NO_CORPUS);
			if(!Name.Exists(dbConn, corpus, nid)) {
				throw new WebApplicationException( 
						Response.status(
								Response.Status.NOT_FOUND).entity("No activityRole found with id: "+id).build());
			}
			name.setId(nid);		// Enforce payload and resource coherence
			name.setCorpusId(id);	// Ensure we have proper linkage
			name.persist(dbConn);
			// Set the response's status and entity
			UriBuilder path = UriBuilder.fromResource(WorkspaceResource.class);
			path.path(id + "/names/" + nid);
			Response response = Response.ok(path.build().toString()).build();
			return response;
		} catch(RuntimeException re) {
			String tmp = myClass+".updateName(): Problem updating DB.\n"+ re.getLocalizedMessage();
			System.err.println(tmp);
			throw new WebApplicationException( 
					Response.status(
							Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		}
	}
	 */

	/**
	 * Deletes a given name.
	 * @param id the id of the name to delete
	 * @return
	 * WE SHOULD NOT EXPOSE THIS - IT SHOULD BE DONE WITH ASSERTIONS
	@DELETE
	@Produces("application/xml")
	@Path("{id}/names/{nid}")
	public Response deleteName(@Context ServletContext srvc, 
			@PathParam("id") int id, @PathParam("nid") int nid) {
		try {
			Connection dbConn = getServiceContext(srvc).getConnection();
			Corpus corpus = getWorkspaceCorpus(dbConn, id, FAIL_ON_NO_CORPUS);
			if(!Name.Exists(dbConn, corpus, nid)) {
				throw new WebApplicationException( 
						Response.status(
								Response.Status.NOT_FOUND).entity("No activityRole found with id: "+id).build());
			}
			Name.DeletePersistence(dbConn, id, nid);
			return Response.ok().build();
		} catch(RuntimeException re) {
			String tmp = myClass+".deleteName(): Problem querying DB.\n"+ re.getLocalizedMessage();
			System.err.println(tmp);
			throw new WebApplicationException( 
					Response.status(
							Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		}
	}
	 */
}
