package edu.berkeley.bps.services.corpus;

import edu.berkeley.bps.services.common.BaseResource;
import edu.berkeley.bps.services.common.ServiceContext;
import edu.berkeley.bps.services.corpus.sax.AssertionsParser;
import edu.berkeley.bps.services.corpus.sax.CorpusParser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
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
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
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
	public List<Corpus> getAll(@Context ServletContext srvc,
			@Context UriInfo ui) {
		try {
			MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
			String wkspParam = queryParams.getFirst("wksp");
			// Default to the "no workspace" value of 0 to get the unattached corpora
			int wksp_id = Corpus.NO_WKSP_ID;
			if(wkspParam != null) {
				try {
					wksp_id = Integer.parseInt(wkspParam);
				} catch( NumberFormatException nfe) {}
			}
			List<Corpus> corpusList = Corpus.ListAll(getServiceContext(srvc), wksp_id);
			return corpusList;
		} catch(WebApplicationException wae) {
			throw wae;
		} catch(Exception e) {
			String tmp = myClass+".getAll(): Problem getting Corpus List.\n"+ e.getLocalizedMessage();
			System.err.println(tmp);
			throw new WebApplicationException( 
					Response.status(
							Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
        }
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
			Corpus corpus = Corpus.FindByID(getServiceContext(srvc), id);
			if(corpus==null) {
            	throw new WebApplicationException( 
            			Response.status(
            				Response.Status.NOT_FOUND).entity("No corpus found with id: "+id).build());
				
			}
			return corpus;
		} catch(WebApplicationException wae) {
			throw wae;
		} catch(Exception e) {
			String tmp = myClass+".getCorpus(): Problem getting Corpus.\n"+ e.getLocalizedMessage();
			System.err.println(tmp);
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
    	try {
    		ServiceContext sc = getServiceContext(srvc);

    		// Check that the item is not already registered.
    		if (Corpus.NameUsed(sc, corpus.getName())) {
    			String tmp = "A corpus with the name '" + corpus.getName() + "' already exists.";
    			throw new WebApplicationException( 
    					Response.status(
    							Response.Status.BAD_REQUEST).entity(tmp).build());
    		} 
    		// Persist the new item, and get an id for it
    		// Ensure this is recognized as new
    		corpus.setId(CachedEntity.UNSET_ID_VALUE);
    		corpus.persist(sc.getConnection(), CachedEntity.SHALLOW_PERSIST);
    		Corpus.AddToMaps(sc, corpus);
    		UriBuilder path = UriBuilder.fromResource(CorporaResource.class);
    		path.path("" + corpus.getId());
    		Response response = Response.created(path.build()).build();
    		return response;
    	} catch(WebApplicationException wae) {
    		throw wae;
    	} catch(Exception e) {
    		String tmp = myClass+".createCorpus(): Problem creating Corpus.\n"+ e.getLocalizedMessage();
    		System.err.println(tmp);
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
    		ServiceContext sc = getServiceContext(srvc);

    		// Check that the item is not already registered.
    		if (Corpus.FindByName(sc, corpusName)!=null) {
    			String tmp = "A corpus with the name '" + corpusName + "' already exists.";
    			throw new WebApplicationException( 
    					Response.status(
    							Response.Status.BAD_REQUEST).entity(tmp).build());
    		}
    		// Register the new item
    		Corpus corpus = Corpus.CreateAndPersist(sc, corpusName, corpusDescription, corpusOwner, null);
    		// Set the response's status and entity
    		UriBuilder path = UriBuilder.fromResource(CorporaResource.class);
    		path.path("" + corpus.getId());
    		Response response = Response.created(path.build()).build();
    		return response;
    	} catch( WebApplicationException wae ) {
    		throw wae;
    	} catch( NumberFormatException nfe ) {
    		throw new WebApplicationException( 
    				Response.status(Response.Status.BAD_REQUEST).entity(
    						"Illegal (non integer) value for corpus owner id").build());
        } catch(Exception e) {
        	String tmp = myClass+".createCorpus(): Problem creating Corpus.\n"+ e.getLocalizedMessage();
        	System.err.println(tmp);
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
    		Connection dbConn = getServiceContext(srvc).getConnection();
            if(!Corpus.Exists(dbConn, id)) {
            	throw new WebApplicationException( 
            			Response.status(
            				Response.Status.NOT_FOUND).entity("No corpus found with id: "+id).build());
            }
            // Since all we're changing is the corpus fields, no need
            // to persist all the docs, etc.
			corpus.setId(id);		// Enforce payload and resource coherence
	    	corpus.persist(dbConn, CachedEntity.SHALLOW_PERSIST);
		} catch(RuntimeException re) {
			String tmp = myClass+".updateCorpus(): Problem updating DB.\n"+ re.getLocalizedMessage();
			System.err.println(tmp);
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
    		ServiceContext sc = getServiceContext(srvc);
			Corpus corpus = Corpus.FindByID(sc, id);
			if(corpus==null) {
            	throw new WebApplicationException( 
            			Response.status(
            				Response.Status.NOT_FOUND).entity("No corpus found with id: "+id).build());
            }
            corpus.deletePersistence(sc);
		} catch(RuntimeException re) {
			String tmp = myClass+".deleteCorpus(): Problem querying DB.\n"+ re.getLocalizedMessage();
			System.err.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
        }
        return Response.ok().build();
    }
	
	/**
     * Runs XSLT transform of uploaded corpus document to provide summary info.
	 * @param id the id of the corpus
	 * @return HTML output of XSLT summary transform
	 */
	@GET
	@Produces("application/xml")
	@Path("{id}/tei")
	public Response getTEI(
			@Context ServletContext srvc, 
			@PathParam("id") int id) {
		final String myName = ".getTEI(): ";
        try {
        	final InputStream xmls;
        	String teipath = "/var/bps/corpora/"+id+"/tei/corpus.xml";
			if (teipath != null && !teipath.isEmpty()) {
				xmls = new FileInputStream(teipath);
			} else {
				String tmp = myClass+myName+"No corpus file uploaded.";
				System.err.println(tmp);
	        	throw new WebApplicationException( 
	    			Response.status(
	    				Response.Status.BAD_REQUEST).entity(tmp).build());
			}
			Response response = Response.ok(xmls).build();
	        return response;
	    } catch(WebApplicationException wae) {
			throw wae;
        } catch (FileNotFoundException fnfe) {
			String tmp = myClass+myName+"Could not open corpus file: \n"+ fnfe.getLocalizedMessage();
			System.err.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		} catch(Exception e) {
			String tmp = myClass+myName+".Problem getting TEI.\n"+ e.getLocalizedMessage();
			System.err.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		}
    }

	/**
     * Runs XSLT transform of uploaded corpus document to provide summary info.
	 * @param id the id of the corpus
	 * @return HTML output of XSLT summary transform
	 */
	@GET
	@Produces("text/html")
	@Path("{id}/tei/summary")
	public StreamingOutput getTEISummary(
			@Context ServletContext srvc, 
			@PathParam("id") int id) {
		final String myName = ".getTEISummary(): ";
        try {
        	final InputStream xmls;
        	String teipath = "/var/bps/corpora/"+id+"/tei/corpus.xml";
			if (teipath != null && !teipath.isEmpty()) {
				xmls = new FileInputStream(teipath);
			} else {
				String tmp = myClass+myName+"No corpus file uploaded.";
				System.err.println(tmp);
	        	throw new WebApplicationException( 
	    			Response.status(
	    				Response.Status.BAD_REQUEST).entity(tmp).build());
			}
        	return transformTEI(srvc, xmls);
		} catch(WebApplicationException wae) {
			throw wae;
        } catch (FileNotFoundException fnfe) {
			String tmp = myClass+myName+"Could not open corpus file: \n"+ fnfe.getLocalizedMessage();
			System.err.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		} catch(Exception e) {
			String tmp = myClass+myName+".Problem getting TEI.\n"+ e.getLocalizedMessage();
			System.err.println(tmp);
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
            			System.err.println(tmp);
                    	throw new WebApplicationException( 
                			Response.status(
                				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
					}
                }
        	};
        } catch (TransformerConfigurationException tce) {
			String tmp = myClass+".getTEI(): Problem transforming TEI.\n"+ tce.getLocalizedMessage();
			System.err.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		} catch(Exception e) {
			String tmp = myClass+".getTEI(): Problem getting TEI.\n"+ e.getLocalizedMessage();
			System.err.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		}
    }

	/**
     * Runs XSLT transform of uploaded corpus document to provide summary info.
	 * @param id the id of the corpus
	 * @return HTML output of XSLT summary transform
	 */
	@PUT
	@Path("{id}/tei")
	public Response rebuildFromTEI(
			@Context ServletContext srvc, 
			@PathParam("id") int id) {
		final String myName = ".rebuildFromTEI(): ";
        try {
        	String teipath = "/var/bps/corpora/"+id+"/tei/corpus.xml";
        	ServiceContext sc = getServiceContext(srvc);
			Corpus corpus = Corpus.FindByID(sc, id);
			if(corpus==null) {
            	throw new WebApplicationException( 
            			Response.status(
            				Response.Status.NOT_FOUND).entity("No corpus found with id: "+id).build());
				
			}
			// Clear out the existing documents, Names, etc.
			Connection dbConn = sc.getConnection();
            corpus.deleteAttachedEntities(dbConn);
            CorpusParser.buildFromTEI(dbConn, corpus, teipath);
            // Persist updated corpus (description, etc.)
            corpus.persist(dbConn, CachedEntity.DEEP_PERSIST);
	        Response response = Response.ok().build();
	        return response;
		} catch(WebApplicationException wae) {
			throw wae;
		} catch(Exception e) {
			String tmp = myClass+myName+"Problem parsing TEI.\n"+ e.getLocalizedMessage();
			System.err.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		}
    }

	/**
     * Runs XSLT transform of uploaded corpus document to provide summary info.
	 * @param id the id of the corpus
	 * @return HTML output of XSLT summary transform
	 */
	@PUT
	@Path("{id}/dates")
	public Response handleDateAssertions(
			@Context ServletContext srvc, 
			@PathParam("id") int id) {
		final String myName = ".handleDateAssertions(): ";
        try {
        	String datespath = "/var/bps/corpora/"+id+"/assertions/dates.xml";
        	ServiceContext sc = getServiceContext(srvc);
			Corpus corpus = Corpus.FindByID(sc, id);
			if(corpus==null) {
            	throw new WebApplicationException( 
            			Response.status(
            				Response.Status.NOT_FOUND).entity("No corpus found with id: "+id).build());
				
			}
			// Clear out the existing documents, Names, etc. 
			AssertionsParser.updateCorpusDates(corpus, datespath);
            // Persist updated corpus (description, etc.)
            corpus.persist(sc.getConnection(), CachedEntity.DEEP_PERSIST);

	        Response response = Response.ok().build();
	        return response;
		} catch(WebApplicationException wae) {
			throw wae;
		} catch(Exception e) {
			String tmp = myClass+myName+"Problem parsing TEI.\n"+ e.getLocalizedMessage();
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
			ServiceContext sc = getServiceContext(srvc);
			Corpus corpus = Corpus.FindByID(sc, id);
			if(corpus==null) {
            	throw new WebApplicationException( 
            			Response.status(
            				Response.Status.NOT_FOUND).entity("No corpus found with id: "+id).build());
			}
			ActivityRole roleFilter = getRoleFromParams(queryParams, corpus);
			Name nameFilter = getNameFromParams(queryParams, corpus);
	        //docList = Document.ListAllInCorpus(dbConn, corpus);
	        docList = corpus.getDocuments(sc, nameFilter, roleFilter, orderBy);
		} catch(WebApplicationException wae) {
			throw wae;
		} catch(RuntimeException re) {
			String tmp = myClass+".getDocuments(): Problem querying DB.\n"+ re.getLocalizedMessage();
			System.err.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
        }
        return docList;
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
		} catch(RuntimeException re) {
			String tmp = myClass+".getDocument("+docspec
				+"): Problem querying DB.\n"+ re.getLocalizedMessage();
			System.err.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
        }
        return document;
    }

	/**
	 * @return Document for the given id
	 */
	protected Document getDocument(ServiceContext sc, 
			int corpusid, String docspec) {
		Document document = null;
        try {
        	Corpus corpus = Corpus.FindByID(sc, corpusid);
        	if(corpus==null) {
        		throw new WebApplicationException( 
    				Response.status(
   						Response.Status.NOT_FOUND).entity(
   								"No corpus found with id: "+corpusid).build());
        	}
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
		} catch(RuntimeException re) {
			String tmp = myClass+".getDocument(): Problem querying DB.\n"+ re.getLocalizedMessage();
			System.err.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
        }
        return document;
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
			@Context ServletContext srvc,  @Context UriInfo ui,
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
		} catch(RuntimeException re) {
			String tmp = myClass+".getDocumentNRADs(): Problem querying DB.\n"+ re.getLocalizedMessage();
			System.err.println(tmp);
        	throw new WebApplicationException( 
    			Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
        }
        return nradList;
    }
	

	/**
     * Deletes documents associated with a given corpus.
	 * @param id the id of the corpus
	 * @return
	 */
	@DELETE
	@Produces("application/xml")
	@Path("{id}/documents")
	public Response deleteDocuments(@Context ServletContext srvc, @PathParam("id") int id) {
        try {
    		ServiceContext sc = getServiceContext(srvc);
			Corpus corpus = Corpus.FindByID(sc, id);
			if(corpus==null) {
            	throw new WebApplicationException( 
            			Response.status(
            				Response.Status.NOT_FOUND).entity("No corpus found with id: "+id).build());
            }
            corpus.deleteDocuments(sc.getConnection());
		} catch(RuntimeException re) {
			String tmp = myClass+".deleteCorpusDocs(): Problem querying DB.\n"+ re.getLocalizedMessage();
			System.err.println(tmp);
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
			Corpus corpus = Corpus.FindByID(getServiceContext(srvc), id);
			if(corpus==null) {
            	throw new WebApplicationException( 
            			Response.status(
            				Response.Status.NOT_FOUND).entity("No corpus found with id: "+id).build());
				
			}
			//activityList = Activity.ListAllInCorpus(dbConn, corpus);
			activityList = corpus.getActivities();
		} catch(RuntimeException re) {
			String tmp = myClass+".getActivities(ServletContext, int)(): Problem querying DB.\n"+ re.getLocalizedMessage();
			System.err.println(tmp);
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
			Corpus corpus = Corpus.FindByID(getServiceContext(srvc), id);
			if(corpus==null) {
            	throw new WebApplicationException( 
            			Response.status(
            				Response.Status.NOT_FOUND).entity("No corpus found with id: "+id).build());
				
			}
			activity = corpus.findActivity(aid);
			if(activity==null) {
            	throw new WebApplicationException( 
            			Response.status(
            				Response.Status.NOT_FOUND).entity("No activity found with id: "+aid).build());
				
			}
		} catch(RuntimeException re) {
			String tmp = myClass+".getActivity(): Problem querying DB.\n"+ re.getLocalizedMessage();
			System.err.println(tmp);
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

        try {
        	ServiceContext sc = getServiceContext(srvc);
        	Corpus corpus = Corpus.FindByID(sc, id);
        	if(corpus==null) {
        		throw new WebApplicationException( 
        				Response.status(
        						Response.Status.NOT_FOUND).entity("No corpus found with id: "+id).build());

        	}
        	// Check that the item is not already registered.
    		Connection dbConn = sc.getConnection();
        	if (Activity.FindByName(dbConn, corpus, activity.getName())!=null) {
        		String tmp = "An activity with the name '" + activity.getName() + "' already exists.";
        		throw new WebApplicationException( 
        				Response.status(
        						Response.Status.BAD_REQUEST).entity(tmp).build());
        	} 
    		activity.setCorpus(corpus);
	        // Persist the new item, and get an id for it
        	activity.CreateAndPersist(dbConn);
        	corpus.addActivity(activity);
	        UriBuilder path = UriBuilder.fromResource(CorporaResource.class);
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
     */
    @PUT
	@Consumes("application/xml")
	@Path("{id}/activities/{aid}")
    public Response updateActivity(@Context ServletContext srvc, 
    		@PathParam("id") int id, @PathParam("aid") int aid, Activity activity){
        try {
        	ServiceContext sc = getServiceContext(srvc);
			Corpus corpus = Corpus.FindByID(sc, id);
			if(corpus==null) {
            	throw new WebApplicationException( 
            			Response.status(
            				Response.Status.NOT_FOUND).entity("No corpus found with id: "+id).build());
				
			}
    		Connection dbConn = sc.getConnection();
            if(!Activity.Exists(dbConn, corpus, aid)) {
            	throw new WebApplicationException( 
            			Response.status(
            				Response.Status.NOT_FOUND).entity("No activity found with id: "+id).build());
            }
            activity.setId(aid);		// Enforce payload and resource coherence
    		activity.setCorpus(corpus);	// Ensure we have proper linkage
            activity.persist(dbConn);
		} catch(RuntimeException re) {
			String tmp = myClass+".updateActivity(): Problem updating DB.\n"+ re.getLocalizedMessage();
			System.err.println(tmp);
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
        	ServiceContext sc = getServiceContext(srvc);
			Corpus corpus = Corpus.FindByID(sc, id);
			if(corpus==null) {
            	throw new WebApplicationException( 
            			Response.status(
            				Response.Status.NOT_FOUND).entity("No corpus found with id: "+id).build());
				
			}
    		Connection dbConn = sc.getConnection();
            if(!Activity.Exists(dbConn, corpus, aid)) {
            	throw new WebApplicationException( 
            			Response.status(
            				Response.Status.NOT_FOUND).entity("No activity found with id: "+id).build());
            }
            Activity.DeletePersistence(dbConn, corpus, aid);
		} catch(RuntimeException re) {
			String tmp = myClass+".deleteActivity(): Problem querying DB.\n"+ re.getLocalizedMessage();
			System.err.println(tmp);
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
		List<ActivityRole> activityRoleList = null;
		try {
			Corpus corpus = Corpus.FindByID(getServiceContext(srvc), id);
			if(corpus==null) {
				throw new WebApplicationException( 
						Response.status(
								Response.Status.NOT_FOUND).entity("No corpus found with id: "+id).build());

			}
			//activityList = ActivityRole.ListAllInCorpus(dbConn, corpus);
			activityRoleList = corpus.getActivityRoles();
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
	@Path("{id}/activityRoles/{arspec}")
	public ActivityRole getActivityRole(@Context ServletContext srvc, 
			@PathParam("id") int id, @PathParam("arspec") String arspec) {
		ActivityRole activityRole = null;
		try {
			Corpus corpus = Corpus.FindByID(getServiceContext(srvc), id);
			if(corpus==null) {
				throw new WebApplicationException( 
					Response.status(
						Response.Status.NOT_FOUND).entity(
								"No corpus found with id: "+id).build());
			}
			int arid = -1;
			try {
				arid = Integer.parseInt(arspec);
			} catch( NumberFormatException nfe) {}
			if(arid>0) {
				activityRole = corpus.findActivityRole(arid);
			} else {
				activityRole = corpus.findActivityRole(arspec);
			}
			if(activityRole==null) {
				throw new WebApplicationException( 
					Response.status(
						Response.Status.NOT_FOUND).entity(
								"No activityRole found with id: "+arid).build());

			}
		} catch(RuntimeException re) {
			String tmp = myClass+".getActivityRole(): Problem querying DB.\n"+ re.getLocalizedMessage();
			System.err.println(tmp);
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

		try {
			ServiceContext sc = getServiceContext(srvc);
			Corpus corpus = Corpus.FindByID(getServiceContext(srvc), id);
			if(corpus==null) {
				throw new WebApplicationException( 
						Response.status(
								Response.Status.NOT_FOUND).entity("No corpus found with id: "+id).build());

			}
			// Check that the item is not already registered.
			Connection dbConn = sc.getConnection();
			if (ActivityRole.FindByName(dbConn, corpus, activityRole.getName())!=null) {
				String tmp = "An activityRole with the name '" + activityRole.getName() + "' already exists.";
				throw new WebApplicationException( 
						Response.status(
								Response.Status.BAD_REQUEST).entity(tmp).build());
			} 
			activityRole.setCorpus(corpus);
			// Persist the new item, and get an id for it
			activityRole.CreateAndPersist(dbConn);
        	corpus.addActivityRole(activityRole);
			UriBuilder path = UriBuilder.fromResource(CorporaResource.class);
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
	 */
	@PUT
	@Consumes("application/xml")
	@Path("{id}/activityRoles/{aid}")
	public Response updateActivityRole(@Context ServletContext srvc, 
			@PathParam("id") int id, @PathParam("aid") int aid, ActivityRole activityRole){
		try {
			ServiceContext sc = getServiceContext(srvc);
			Corpus corpus = Corpus.FindByID(sc, id);
			if(corpus==null) {
				throw new WebApplicationException( 
						Response.status(
								Response.Status.NOT_FOUND).entity("No corpus found with id: "+id).build());

			}
			Connection dbConn = sc.getConnection();
			if(!ActivityRole.Exists(dbConn, corpus, aid)) {
				throw new WebApplicationException( 
						Response.status(
								Response.Status.NOT_FOUND).entity("No activityRole found with id: "+id).build());
			}
			activityRole.setId(aid);		// Enforce payload and resource coherence
			activityRole.setCorpus(corpus);	// Ensure we have proper linkage
			activityRole.persist(dbConn);
		} catch(RuntimeException re) {
			String tmp = myClass+".updateActivityRole(): Problem updating DB.\n"+ re.getLocalizedMessage();
			System.err.println(tmp);
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
			ServiceContext sc = getServiceContext(srvc);
			Corpus corpus = Corpus.FindByID(sc, id);
			if(corpus==null) {
				throw new WebApplicationException( 
						Response.status(
								Response.Status.NOT_FOUND).entity("No corpus found with id: "+id).build());

			}
			Connection dbConn = sc.getConnection();
			if(!ActivityRole.Exists(dbConn, corpus, aid)) {
				throw new WebApplicationException( 
						Response.status(
								Response.Status.NOT_FOUND).entity("No activityRole found with id: "+id).build());
			}
			ActivityRole.DeletePersistence(dbConn, corpus, aid);
		} catch(RuntimeException re) {
			String tmp = myClass+".deleteActivityRole(): Problem querying DB.\n"+ re.getLocalizedMessage();
			System.err.println(tmp);
			throw new WebApplicationException( 
					Response.status(
							Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		}
		return Response.ok().build();
	}
	
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
	public List<Name> getNames(@Context ServletContext srvc, @PathParam("id") int id,
			@Context UriInfo ui) {
		List<Name> nameList = null;
		try {
			Corpus corpus = Corpus.FindByID(getServiceContext(srvc), id);
			if(corpus==null) {
				throw new WebApplicationException( 
						Response.status(
								Response.Status.NOT_FOUND).entity("No corpus found with id: "+id).build());
			}
			MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
			String typeFilterParam = queryParams.getFirst("type");
			if(typeFilterParam!=null) {
				try {
					typeFilterParam = typeFilterParam.trim();
					int gender = Name.NameTypeStringToValue(typeFilterParam);
				} catch( IllegalArgumentException iae) {
					throw new WebApplicationException( 
						Response.status(
							Response.Status.NOT_FOUND).entity(
									"Unrecognized type filter: "+typeFilterParam).build());
				}
			}
			ActivityRole roleFilter = getRoleFromParams(queryParams, corpus);
			String genderFilterParam = queryParams.getFirst("gender");
			if(genderFilterParam!=null) {
				try {
					genderFilterParam = genderFilterParam.trim();
					int gender = Name.GenderStringToValue(genderFilterParam);
				} catch( IllegalArgumentException iae) {
					throw new WebApplicationException( 
						Response.status(
							Response.Status.NOT_FOUND).entity(
									"Unrecognized gender filter: "+genderFilterParam).build());
				}
			}
			String orderByParam = queryParams.getFirst("o");
			if(orderByParam!=null) {
				orderByParam = orderByParam.trim();
			}
			if(typeFilterParam==null && roleFilter==null && genderFilterParam == null
					 && orderByParam == null) {
				nameList = corpus.getNames();
			} else {
				nameList = corpus.getNames(typeFilterParam, roleFilter, genderFilterParam, 
										orderByParam, getServiceContext(srvc).getConnection());
			}
		} catch(RuntimeException re) {
			String tmp = myClass+".getNames(): Problem querying DB.\n"+ re.getLocalizedMessage();
			System.err.println(tmp);
			throw new WebApplicationException( 
					Response.status(
							Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		}
		return nameList;
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
			Corpus corpus = Corpus.FindByID(getServiceContext(srvc), id);
			if(corpus==null) {
				throw new WebApplicationException( 
						Response.status(
								Response.Status.NOT_FOUND).entity("No corpus found with id: "+id).build());

			}
			name = corpus.findName(nid);
			if(name==null) {
				throw new WebApplicationException( 
					Response.status(
						Response.Status.NOT_FOUND).entity("No name found with id: "+nid).build());

			}
		} catch(RuntimeException re) {
			String tmp = myClass+".getActivityRole(): Problem querying DB.\n"+ re.getLocalizedMessage();
			System.err.println(tmp);
			throw new WebApplicationException( 
					Response.status(
							Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		}
		return name;
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
			Corpus corpus = Corpus.FindByID(sc, id);
			if(corpus==null) {
				throw new WebApplicationException( 
						Response.status(
								Response.Status.NOT_FOUND).entity("No corpus found with id: "+id).build());

			}
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
			UriBuilder path = UriBuilder.fromResource(CorporaResource.class);
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
	 */
	@PUT
	@Consumes("application/xml")
	@Path("{id}/names/{nid}")
	public Response updateName(@Context ServletContext srvc, 
			@PathParam("id") int id, @PathParam("nid") int nid, Name name){
		try {
			ServiceContext sc = getServiceContext(srvc);
			Corpus corpus = Corpus.FindByID(sc, nid);
			if(corpus==null) {
				throw new WebApplicationException( 
						Response.status(
								Response.Status.NOT_FOUND).entity("No corpus found with id: "+id).build());

			}
			Connection dbConn = sc.getConnection();
			if(!Name.Exists(dbConn, corpus, id)) {
				throw new WebApplicationException( 
						Response.status(
								Response.Status.NOT_FOUND).entity("No activityRole found with id: "+id).build());
			}
			name.setId(nid);		// Enforce payload and resource coherence
			name.setCorpusId(id);	// Ensure we have proper linkage
			name.persist(dbConn);
		} catch(RuntimeException re) {
			String tmp = myClass+".updateActivityRole(): Problem updating DB.\n"+ re.getLocalizedMessage();
			System.err.println(tmp);
			throw new WebApplicationException( 
					Response.status(
							Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		}
		// Set the response's status and entity
		UriBuilder path = UriBuilder.fromResource(CorporaResource.class);
		path.path(id + "/names/" + nid);
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
	@Path("{id}/names/{nid}")
	public Response deleteName(@Context ServletContext srvc, 
			@PathParam("id") int id, @PathParam("nid") int nid) {
		try {
			ServiceContext sc = getServiceContext(srvc);
			Corpus corpus = Corpus.FindByID(sc, id);
			if(corpus==null) {
				throw new WebApplicationException( 
						Response.status(
								Response.Status.NOT_FOUND).entity("No corpus found with id: "+id).build());

			}
			Connection dbConn = sc.getConnection();
			if(!Name.Exists(dbConn, corpus, nid)) {
				throw new WebApplicationException( 
						Response.status(
								Response.Status.NOT_FOUND).entity("No activityRole found with id: "+id).build());
			}
			Name.DeletePersistence(dbConn, id, nid);
		} catch(RuntimeException re) {
			String tmp = myClass+".deleteName(): Problem querying DB.\n"+ re.getLocalizedMessage();
			System.err.println(tmp);
			throw new WebApplicationException( 
					Response.status(
							Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		}
		return Response.ok().build();
	}
	
	private ActivityRole getRoleFromParams(MultivaluedMap<String, String> queryParams,
			Corpus corpus) {
		String roleFilterParam = queryParams.getFirst("role");
		ActivityRole roleFilter = null;
		if(roleFilterParam!=null) {
			roleFilter = corpus.findActivityRole(roleFilterParam.trim());
			if(roleFilter==null) {
				throw new WebApplicationException( 
					Response.status(
						Response.Status.NOT_FOUND).entity(
								"Unrecognized role filter: "+roleFilterParam).build());
			}
			return roleFilter;
		}
		return null;
	}

	private Name getNameFromParams(MultivaluedMap<String, String> queryParams,
			Corpus corpus) {
		String nameFilterParam = queryParams.getFirst("name");
		Name name= null;
		if(nameFilterParam!=null) {
			int name_id = -1;
			try {
				name_id = Integer.parseInt(nameFilterParam);
			} catch( NumberFormatException nfe) {}
			if(name_id>0) {
				name = corpus.findName(name_id);
			} else {
				name = corpus.findName(nameFilterParam);
			}
			if(name==null) {
				throw new WebApplicationException( 
					Response.status(
						Response.Status.NOT_FOUND).entity(
								"Unrecognized name: "+nameFilterParam).build());
			}
			return name;
		}
		return null;
	}

}
