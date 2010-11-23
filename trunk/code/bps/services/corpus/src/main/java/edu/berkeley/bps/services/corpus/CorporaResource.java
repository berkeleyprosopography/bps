package edu.berkeley.bps.services.corpus;

import edu.berkeley.bps.services.common.BaseResource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
 * Resource that manages a list of items.
 * 
 */
public class CorporaResource extends BaseResource {
	
	private static final String myClass = "CorporaResource";

	public CorporaResource(Context context, Request request, Response response) {  
		super(context, request, response);  

        // Allow modifications of this resource via POST requests.
        setModifiable(true);

        // Declare the kind of representations supported by this resource.
        getVariants().add(new Variant(MediaType.TEXT_XML));
        System.out.println(myClass+" CTor called...");
    }

    /**
     * Handle POST requests: create a new item.
     */
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

    /**
     * Returns a listing of all registered items.
     */
    @Override
    public Representation represent(Variant variant) throws ResourceException {
		final String myName = ".represent (getList): ";
		// TODO Add pagination support to the BaseResource
		final String SELECT_ALL = "SELECT id, name, description, owner_id FROM corpus";
        // Generate the right representation according to its media type.
        if (MediaType.TEXT_XML.equals(variant.getMediaType())) {
            try {
                DomRepresentation representation = new DomRepresentation(
                        MediaType.TEXT_XML);
                // Generate a DOM document representing the list of
                // items.
                Document doc = representation.getDocument();
                Element root = doc.createElement("corpora");
                doc.appendChild(root);

        		try {
        	        Connection dbConn = openConnection(false);

        			PreparedStatement stmt = dbConn.prepareStatement(SELECT_ALL);
        			ResultSet rs = stmt.executeQuery();
        			while(rs.next()){
        				int id = rs.getInt("id");
        				String name = rs.getString("name");
        				String description = rs.getString("description"); 
        				String owner = rs.getString("owner_id"); 
        				
                        Element eltItem = 
                        	Corpus.toXMLPayload(doc, id, name, description, null );

                        root.appendChild(eltItem);
        			}
        			rs.close();
        			stmt.close();
        		} catch(SQLException se) {
        			String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
        			System.out.println(tmp);
                	return generateErrorRepresentation(
                            "Problem creating corpus\n"+se.getLocalizedMessage(), 
                            	Status.CONNECTOR_ERROR_INTERNAL, null );
        		}
                doc.normalizeDocument();

                // Returns the XML representation of this document.
                return representation;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

}
