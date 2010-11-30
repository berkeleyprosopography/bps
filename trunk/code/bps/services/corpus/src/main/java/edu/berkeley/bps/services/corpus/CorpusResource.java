package edu.berkeley.bps.services.corpus;

import edu.berkeley.bps.services.common.BaseResource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.restlet.Context;  
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;  
import org.restlet.data.Response;  
import org.restlet.data.Status;
import org.restlet.resource.Resource;  
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Resource that manages an individual Corpus item.
 * 
 */
public class CorpusResource extends BaseResource {

	private static final String myClass = "CorpusResource";

	private Corpus corpus = null;

	public CorpusResource(Context context, Request request, Response response) {  
		super(context, request, response);  

		// Get the "id" attribute value taken from the URI template
		// /corpora/{id}.
		String idStr = (String) getRequest().getAttributes().get("corpusid");
		int id = Integer.parseInt(idStr);
		Connection dbConn = openConnection(false);

		// Get the item directly from the "persistence layer".
		this.corpus = Corpus.FindByID(dbConn, id);

		if (this.corpus != null) {
			// Declare the kind of representations supported by this resource.
			getVariants().add(new Variant(MediaType.TEXT_XML));
			// By default a resource cannot be updated.
			setModifiable(true);
		} else {
			// This resource is not available.
			setAvailable(false);
		}

		//System.out.println(myClass+" CTor called...");
	}

    /**
     * Handle DELETE requests.
     */
    @Override
    public void removeRepresentations() throws ResourceException {
        if (corpus == null) {
    		System.out.println(myClass+" DELETE called for non-existent corpus");
        } else {
    		final String DELETE_STMT = "DELETE FROM corpus WHERE id=?";
        	Connection dbConn = openConnection(false);
        	corpus.deletePersistence(dbConn);
			corpus = null;
        }
        // Tells the client that the request has been successfully fulfilled.
        getResponse().setStatus(Status.SUCCESS_OK);
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        // Generate the right representation according to its media type.
        if (MediaType.TEXT_XML.equals(variant.getMediaType())) {
            try {
                DomRepresentation representation = 
                	new DomRepresentation(MediaType.TEXT_XML);
                // Generate a DOM document representing the corpus.
                Document doc = representation.getDocument();
                Element corpusEl = corpus.toXMLPayload(doc, false );
                doc.appendChild(corpusEl);
                doc.normalizeDocument();

                // Returns the XML representation of this document.
                return representation;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Handle PUT requests.
     */
    @Override
    public void storeRepresentation(Representation entity)
            throws ResourceException {

        // Fetch the name and description.
        Form form = new Form(entity);
    	String name = form.getFirstValue("name");
    	String description = form.getFirstValue("description");
    	Connection dbConn = openConnection(false);
        // The PUT request updates or creates the resource.
    	if(corpus!=null) {
    		if(name!=null&&name.length()>2)
    			corpus.setName(name);
    		if(description!=null&&description.length()>0)
    			corpus.setDescription(description);
    		corpus.persist(dbConn);
            getResponse().setStatus(Status.SUCCESS_OK);
    	} else {
    		corpus = Corpus.CreateAndPersist(dbConn, name, description, 0, null);
            getResponse().setStatus(Status.SUCCESS_CREATED);
    	}
    }

}
