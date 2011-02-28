package edu.berkeley.bps.services.corpus;

import edu.berkeley.bps.services.common.BaseResource;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import javax.ws.rs.QueryParam;

import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

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
	@Produces({"application/xml", "application/json"})
	@Wrapped(element="corpora")
	public List<Corpus> getAll(@Context ServletContext srvc) {
		Connection dbConn = getConnection(srvc);
		List<Corpus> corpusList = Corpus.ListAll(dbConn);
        return corpusList;
    }

	/**
     * Returns details for a given corpus.
	 * @param id the id of the corpus of interest
	 * @return
	 */
	@GET
	@Produces({"application/xml", "application/json"})
	@Path("{id}")
	public Corpus getCorpus(@Context ServletContext srvc, @PathParam("id")int id) {
        try {
	        Connection dbConn = getConnection(srvc);
			Corpus corpus = Corpus.FindByID(dbConn, id);
			if(corpus==null) {
            	throw new WebApplicationException( 
            			Response.status(
            				Response.Status.NOT_FOUND).entity("No corpus found with id: "+id).build());
				
			}
			return corpus;
		} catch(Exception e) {
			String tmp = myClass+".getCorpus(): Problem getting Corpus.\n"+ e.getLocalizedMessage();
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
    public Response createCorpus(@Context ServletContext srvc, Corpus corpus){
    	Connection dbConn = getConnection(srvc);

        // Check that the item is not already registered.
        if (Corpus.NameUsed(dbConn, corpus.getName())) {
            String tmp = "A corpus with the name '" + corpus.getName() + "' already exists.";
        	throw new WebApplicationException( 
        			Response.status(
        				Response.Status.BAD_REQUEST).entity(tmp).build());
        } 
        try {
	        // Persist the new item, and get an id for it
	    	corpus.CreateAndPersist(dbConn);
	        UriBuilder path = UriBuilder.fromResource(CorporaResource.class);
	        path.path("" + corpus.getId());
	        Response response = Response.created(path.build()).build();
	        return response;
		} catch(Exception e) {
			String tmp = myClass+".createCorpus(): Problem creating Corpus.\n"+ e.getLocalizedMessage();
			System.out.println(tmp);
	    	throw new WebApplicationException( 
				Response.status(
					Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
	    }
	}

    /**
     * Creates a new corpus.
     * @param form the representation of the new corpus as form data
     * @return Response, with the path (and so id) of the newly created corpus
     */
    @POST
    @Consumes("application/x-www-form-urlencoded")
    public Response createCorpus(@Context ServletContext srvc, MultivaluedMap<String, String> form) {
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
    	Connection dbConn = getConnection(srvc);

        // Check that the item is not already registered.
        if (Corpus.FindByName(dbConn, corpusName)!=null) {
            String tmp = "A corpus with the name '" + corpusName + "' already exists.";
        	throw new WebApplicationException( 
        			Response.status(
        				Response.Status.BAD_REQUEST).entity(tmp).build());
        }
        try {
        	// Register the new item
        	Corpus corpus = Corpus.CreateAndPersist(dbConn, corpusName, corpusDescription, corpusOwner, null);
        	// Set the response's status and entity
        	UriBuilder path = UriBuilder.fromResource(CorporaResource.class);
        	path.path("" + corpus.getId());
        	Response response = Response.created(path.build()).build();
        	return response;
        } catch(Exception e) {
        	String tmp = myClass+".createCorpus(): Problem creating Corpus.\n"+ e.getLocalizedMessage();
        	System.out.println(tmp);
        	throw new WebApplicationException( 
        			Response.status(
        					Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
        }
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
    public Response updateCorpus(@Context ServletContext srvc, 
    		@PathParam("id") int id, Corpus corpus){
        try {
	        Connection dbConn = getConnection(srvc);
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
        UriBuilder path = UriBuilder.fromResource(CorporaResource.class);
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
	public Response deleteCorpus(@Context ServletContext srvc, @PathParam("id") int id) {
        try {
	        Connection dbConn = getConnection(srvc);
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
	/**
     * Loads documents from a TEI file
	 * @param id the id of the corpus
	 * @return
	 */
	@GET
	@Produces("text/html")
	@Path("{id}/tei")
	public StreamingOutput getTEI(
			@Context ServletContext srvc, 
			@PathParam("id") int id,
			@QueryParam("file") String teipath) {
    	String defaultTEIPath = "/WEB-INF/resources/files/corpus.xml";
        try {
        	final InputStream xmls;
			if (teipath != null && !teipath.isEmpty()) {
				xmls = new FileInputStream(teipath);
			} else {
	        	xmls = srvc.getResourceAsStream(defaultTEIPath);
	        	if(xmls==null) {
	        		throw new RuntimeException("Cannot open resource: "+defaultTEIPath);
	        	}
			}
        	return transformTEI(srvc, xmls);
		} catch(WebApplicationException wae) {
			throw wae;
        } catch (FileNotFoundException fnfe) {
			String tmp = myClass+".loadTEI(): Bad file param: \n"+ fnfe.getLocalizedMessage();
			System.out.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		} catch(Exception e) {
			String tmp = myClass+".getTEI(): Problem getting TEI.\n"+ e.getLocalizedMessage();
			System.out.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		}
    }

	/**
     * Loads documents from a TEI file
	 * @param id the id of the corpus
	 * @return
	 */
	@PUT
	@Produces("application/xml")
	@Path("{id}/tei")
	public StreamingOutput loadTEI(
			@Context ServletContext srvc, 
			@PathParam("id") int id,
			@QueryParam("file") String teipath) {
        try {
        	final InputStream xmls = new FileInputStream(teipath);
        	return transformTEI(srvc, xmls);
		} catch(WebApplicationException wae) {
			throw wae;
        } catch (FileNotFoundException fnfe) {
			String tmp = myClass+".loadTEI(): Bad file param: \n"+ fnfe.getLocalizedMessage();
			System.out.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		}
    }

	public StreamingOutput transformTEI(
			ServletContext srvc, 
			InputStream teiStream) {
    	String xslpath = "/WEB-INF/resources/files/BPSTEINames.xsl";
        try {
        	final InputStream xmls = teiStream;
        	final InputStream xsls = srvc.getResourceAsStream(xslpath);
        	if(xsls==null) {
        		throw new RuntimeException("Cannot open resource: "+xslpath);
        	}
        	TransformerFactory tFactory = TransformerFactory.newInstance();
        	final Transformer transformer = 
        		tFactory.newTransformer(
        				new StreamSource(xsls));
        	return new StreamingOutput() {
        		public void write(OutputStream output) throws IOException, WebApplicationException {
                	try {
						transformer.transform(
						        new StreamSource(xmls), 
						        new StreamResult(output));
                    } catch (TransformerException tce) {
            			String tmp = myClass+".getTEI(): Problem transforming TEI.\n"+ tce.getLocalizedMessage();
            			System.out.println(tmp);
                    	throw new WebApplicationException( 
                			Response.status(
                				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
					}
                }
        	};
        } catch (TransformerConfigurationException tce) {
			String tmp = myClass+".getTEI(): Problem transforming TEI.\n"+ tce.getLocalizedMessage();
			System.out.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		} catch(Exception e) {
			String tmp = myClass+".getTEI(): Problem getting TEI.\n"+ e.getLocalizedMessage();
			System.out.println(tmp);
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
	public List<Document> getCorpusDocs(@Context ServletContext srvc, @PathParam("id") int id) {
		List<Document> docList = null;
        try {
	        Connection dbConn = getConnection(srvc);
			Corpus corpus = Corpus.FindByID(dbConn, id);
			if(corpus==null) {
            	throw new WebApplicationException( 
            			Response.status(
            				Response.Status.NOT_FOUND).entity("No corpus found with id: "+id).build());
				
			}
	        docList = Document.ListAllInCorpus(dbConn, corpus);
		} catch(RuntimeException re) {
			String tmp = myClass+".getCorpusDocs(): Problem querying DB.\n"+ re.getLocalizedMessage();
			System.out.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
        }
        return docList;
    }

	/**
     * Deletes documents associated with a given corpus.
	 * @param id the id of the corpus
	 * @return
	 */
	@DELETE
	@Produces("application/xml")
	@Path("{id}/documents")
	public Response deleteCorpusDocs(@Context ServletContext srvc, @PathParam("id") int id) {
        try {
	        Connection dbConn = getConnection(srvc);
			Corpus corpus = Corpus.FindByID(dbConn, id);
			if(corpus==null) {
            	throw new WebApplicationException( 
            			Response.status(
            				Response.Status.NOT_FOUND).entity("No corpus found with id: "+id).build());
            }
            corpus.deleteDocuments(dbConn);
		} catch(RuntimeException re) {
			String tmp = myClass+".deleteCorpusDocs(): Problem querying DB.\n"+ re.getLocalizedMessage();
			System.out.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
        }
        return Response.ok().build();
    }
	
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
	        Connection dbConn = getConnection(srvc);
			Corpus corpus = Corpus.FindByID(dbConn, id);
			if(corpus==null) {
            	throw new WebApplicationException( 
            			Response.status(
            				Response.Status.NOT_FOUND).entity("No corpus found with id: "+id).build());
				
			}
			activityList = Activity.ListAllInCorpus(dbConn, corpus);
		} catch(RuntimeException re) {
			String tmp = myClass+".getActivities(ServletContext, int)(): Problem querying DB.\n"+ re.getLocalizedMessage();
			System.out.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
        }
        return activityList;
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
	        Connection dbConn = getConnection(srvc);
			Corpus corpus = Corpus.FindByID(dbConn, id);
			if(corpus==null) {
            	throw new WebApplicationException( 
            			Response.status(
            				Response.Status.NOT_FOUND).entity("No corpus found with id: "+id).build());
				
			}
			activity = Activity.FindByID(dbConn, corpus, aid);
			if(activity==null) {
            	throw new WebApplicationException( 
            			Response.status(
            				Response.Status.NOT_FOUND).entity("No activity found with id: "+aid).build());
				
			}
		} catch(RuntimeException re) {
			String tmp = myClass+".getActivity(): Problem querying DB.\n"+ re.getLocalizedMessage();
			System.out.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
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
	@Path("{id}/activities")
    public Response createActivity(@Context ServletContext srvc, 
    		@PathParam("id") int id, Activity activity){
    	Connection dbConn = getConnection(srvc);

        try {
        	Corpus corpus = Corpus.FindByID(dbConn, id);
        	if(corpus==null) {
        		throw new WebApplicationException( 
        				Response.status(
        						Response.Status.NOT_FOUND).entity("No corpus found with id: "+id).build());

        	}
        	// Check that the item is not already registered.
        	if (Activity.FindByName(dbConn, corpus, activity.getName())!=null) {
        		String tmp = "An activity with the name '" + activity.getName() + "' already exists.";
        		throw new WebApplicationException( 
        				Response.status(
        						Response.Status.BAD_REQUEST).entity(tmp).build());
        	} 
    		activity.setCorpus(corpus);
	        // Persist the new item, and get an id for it
        	activity.CreateAndPersist(dbConn);
	        UriBuilder path = UriBuilder.fromResource(CorporaResource.class);
  			path.path(id + "/activities/" + activity.getId());
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
	@Path("{id}/activities/{aid}")
    public Response updateActivity(@Context ServletContext srvc, 
    		@PathParam("id") int id, @PathParam("id") int aid, Activity activity){
        try {
	        Connection dbConn = getConnection(srvc);
			Corpus corpus = Corpus.FindByID(dbConn, id);
			if(corpus==null) {
            	throw new WebApplicationException( 
            			Response.status(
            				Response.Status.NOT_FOUND).entity("No corpus found with id: "+id).build());
				
			}
            if(!Activity.Exists(dbConn, corpus, id)) {
            	throw new WebApplicationException( 
            			Response.status(
            				Response.Status.NOT_FOUND).entity("No activity found with id: "+id).build());
            }
    		activity.setCorpus(corpus);	// Ensure we have proper linkage
            activity.persist(dbConn);
		} catch(RuntimeException re) {
			String tmp = myClass+".updateActivity(): Problem updating DB.\n"+ re.getLocalizedMessage();
			System.out.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
        }
        // Set the response's status and entity
        UriBuilder path = UriBuilder.fromResource(CorporaResource.class);
		path.path(id + "/activities/" + aid);
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
	@Path("{id}/activities/{aid}")
	public Response deleteActivity(@Context ServletContext srvc, 
			@PathParam("id") int id, @PathParam("aid") int aid) {
        try {
	        Connection dbConn = getConnection(srvc);
			Corpus corpus = Corpus.FindByID(dbConn, id);
			if(corpus==null) {
            	throw new WebApplicationException( 
            			Response.status(
            				Response.Status.NOT_FOUND).entity("No corpus found with id: "+id).build());
				
			}
            if(!Activity.Exists(dbConn, corpus, id)) {
            	throw new WebApplicationException( 
            			Response.status(
            				Response.Status.NOT_FOUND).entity("No activity found with id: "+id).build());
            }
            Activity.DeletePersistence(dbConn, corpus, id);
		} catch(RuntimeException re) {
			String tmp = myClass+".deleteActivity(): Problem querying DB.\n"+ re.getLocalizedMessage();
			System.out.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
        }
        return Response.ok().build();
    }
	
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
		List<ActivityRole> activityList = null;
		try {
			Connection dbConn = getConnection(srvc);
			Corpus corpus = Corpus.FindByID(dbConn, id);
			if(corpus==null) {
				throw new WebApplicationException( 
						Response.status(
								Response.Status.NOT_FOUND).entity("No corpus found with id: "+id).build());

			}
			activityList = ActivityRole.ListAllInCorpus(dbConn, corpus);
		} catch(RuntimeException re) {
			String tmp = myClass+".getActivityRoles(): Problem querying DB.\n"+ re.getLocalizedMessage();
			System.out.println(tmp);
			throw new WebApplicationException( 
					Response.status(
							Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		}
		return activityList;
	}

	/**
	 * Returns an ActivityRole SubResource for a given activityRole
	 * @return ActivityRoleResource for the given id
	 */
	@GET
	@Produces({"application/xml", "application/json"})
	@Path("{id}/activityRoles/{aid}")
	public ActivityRole getActivityRole(@Context ServletContext srvc, 
			@PathParam("id") int id, @PathParam("aid") int aid) {
		ActivityRole activityRole = null;
		try {
			Connection dbConn = getConnection(srvc);
			Corpus corpus = Corpus.FindByID(dbConn, id);
			if(corpus==null) {
				throw new WebApplicationException( 
						Response.status(
								Response.Status.NOT_FOUND).entity("No corpus found with id: "+id).build());

			}
			activityRole = ActivityRole.FindByID(dbConn, corpus, aid);
			if(activityRole==null) {
				throw new WebApplicationException( 
						Response.status(
								Response.Status.NOT_FOUND).entity("No activityRole found with id: "+aid).build());

			}
		} catch(RuntimeException re) {
			String tmp = myClass+".getActivityRole(): Problem querying DB.\n"+ re.getLocalizedMessage();
			System.out.println(tmp);
			throw new WebApplicationException( 
					Response.status(
							Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		}
		return activityRole;
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
		Connection dbConn = getConnection(srvc);

		try {
			Corpus corpus = Corpus.FindByID(dbConn, id);
			if(corpus==null) {
				throw new WebApplicationException( 
						Response.status(
								Response.Status.NOT_FOUND).entity("No corpus found with id: "+id).build());

			}
			// Check that the item is not already registered.
			if (ActivityRole.FindByName(dbConn, corpus, activityRole.getName())!=null) {
				String tmp = "An activityRole with the name '" + activityRole.getName() + "' already exists.";
				throw new WebApplicationException( 
						Response.status(
								Response.Status.BAD_REQUEST).entity(tmp).build());
			} 
			activityRole.setCorpus(corpus);
			// Persist the new item, and get an id for it
			activityRole.CreateAndPersist(dbConn);
			UriBuilder path = UriBuilder.fromResource(CorporaResource.class);
			path.path(id + "/activityRoles/" + activityRole.getId());
			Response response = Response.created(path.build()).build();
			return response;
		} catch(Exception e) {
			String tmp = myClass+".createActivityRole(): Problem creating ActivityRole.\n"+ e.getLocalizedMessage();
			System.out.println(tmp);
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
	 */
	@PUT
	@Consumes("application/xml")
	@Path("{id}/activityRoles/{aid}")
	public Response updateActivityRole(@Context ServletContext srvc, 
			@PathParam("id") int id, @PathParam("id") int aid, ActivityRole activityRole){
		try {
			Connection dbConn = getConnection(srvc);
			Corpus corpus = Corpus.FindByID(dbConn, id);
			if(corpus==null) {
				throw new WebApplicationException( 
						Response.status(
								Response.Status.NOT_FOUND).entity("No corpus found with id: "+id).build());

			}
			if(!ActivityRole.Exists(dbConn, corpus, id)) {
				throw new WebApplicationException( 
						Response.status(
								Response.Status.NOT_FOUND).entity("No activityRole found with id: "+id).build());
			}
			activityRole.setCorpus(corpus);	// Ensure we have proper linkage
			activityRole.persist(dbConn);
		} catch(RuntimeException re) {
			String tmp = myClass+".updateActivityRole(): Problem updating DB.\n"+ re.getLocalizedMessage();
			System.out.println(tmp);
			throw new WebApplicationException( 
					Response.status(
							Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		}
		// Set the response's status and entity
		UriBuilder path = UriBuilder.fromResource(CorporaResource.class);
		path.path(id + "/activityRoles/" + aid);
		Response response = Response.ok(path.build().toString()).build();
		return response;
	}

	/**
	 * Deletes a given activityRole.
	 * @param id the id of the activityRole to delete
	 * @return
	 */
	@DELETE
	@Produces("application/xml")
	@Path("{id}/activityRoles/{aid}")
	public Response deleteActivityRole(@Context ServletContext srvc, 
			@PathParam("id") int id, @PathParam("aid") int aid) {
		try {
			Connection dbConn = getConnection(srvc);
			Corpus corpus = Corpus.FindByID(dbConn, id);
			if(corpus==null) {
				throw new WebApplicationException( 
						Response.status(
								Response.Status.NOT_FOUND).entity("No corpus found with id: "+id).build());

			}
			if(!ActivityRole.Exists(dbConn, corpus, id)) {
				throw new WebApplicationException( 
						Response.status(
								Response.Status.NOT_FOUND).entity("No activityRole found with id: "+id).build());
			}
			ActivityRole.DeletePersistence(dbConn, corpus, id);
		} catch(RuntimeException re) {
			String tmp = myClass+".deleteActivityRole(): Problem querying DB.\n"+ re.getLocalizedMessage();
			System.out.println(tmp);
			throw new WebApplicationException( 
					Response.status(
							Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		}
		return Response.ok().build();
	}
}
