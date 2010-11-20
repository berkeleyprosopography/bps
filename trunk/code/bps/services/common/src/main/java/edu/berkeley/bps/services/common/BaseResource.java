package edu.berkeley.bps.services.common;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.restlet.Context;  
import org.restlet.data.MediaType;
import org.restlet.data.Request;  
import org.restlet.data.Response;  
import org.restlet.data.Status;
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;  
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** 
 * Base resource class that supports common behaviors or attributes shared by 
 * all resources. Currently empty, but will centralize info on DB connections. 
 *  
 */  
public abstract class BaseResource extends Resource {  

	private String connectionUrl = null;
	
	public BaseResource(Context context, Request request, Response response) {  
		super(context, request, response);  
		
        String host = context.getParameters().getFirstValue("bps.db.host");
        String dbName = context.getParameters().getFirstValue("bps.db.dbname");
        String dbUser = context.getParameters().getFirstValue("bps.db.dbuser");
        String dbPass = context.getParameters().getFirstValue("bps.db.dbpass");
        connectionUrl = "jdbc:mysql://"+host+"/"+dbName+"?user="+dbUser+"&password="+dbPass;
	}  
	
	protected Connection openConnection() {
		return openConnection(true);
	}
	
	protected Connection openConnection(boolean verifyIsAvailable) {
		final String myName = "openConnection: ";
		if(connectionUrl == null) {
			String tmp = myName+"No connectionUrl set.";
			System.out.println(tmp);
			throw new RuntimeException( tmp );
		}
		Connection jdbcConnection = null;
		boolean isAvail = false;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			jdbcConnection = DriverManager.getConnection(connectionUrl);
			if(verifyIsAvailable) {
				Statement stmt = jdbcConnection.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT lockoutActive FROM DBInfo");
				if(rs.next()){
					isAvail = !rs.getBoolean("lockoutActive"); 
				}
				rs.close();
			}
		} catch ( ClassNotFoundException cnfe ) {
			String tmp = myName+"Cannot load the SQLServerDriver class.";
			System.out.println(tmp+"\n"+cnfe.getMessage());
			throw new RuntimeException(tmp);
		} catch (SQLException se) {
			String tmp = myName+"Problem connecting to DB. URL: "
				+"\n"+connectionUrl+"\n"+ se.getMessage();
			System.out.println(tmp);
			throw new RuntimeException( tmp );
		} catch (Exception e) {
			String tmp = myName+"\n"+ e.getMessage();
			System.out.println(tmp);
			throw new RuntimeException( tmp );
		}
		if(verifyIsAvailable && !isAvail) {
			closeConnection(jdbcConnection);
			String tmp = myName+"BPS Not Available (locked out).";
			System.out.println(tmp);
			throw new RuntimeException( tmp );
		}
		return jdbcConnection;
	}
	

	protected void closeConnection(Connection jdbcConnection) {
		if (jdbcConnection != null) try { jdbcConnection.close(); } catch(Exception e) {}
	}

    /**
     * Generate an XML representation of an error response.
     * 
     * @param errorMessage
     *            the error message.
     * @param errorCode
     *            the error code.
     */
    protected Representation generateErrorRepresentation(String errorMessage, Status status,
            Response response) {
        // Generate the output representation
        try {
        	DomRepresentation representation = new DomRepresentation(
                    MediaType.TEXT_XML);
            // Generate a DOM document representing the list of
            // items.
            Document d = representation.getDocument();

            Element eltError = d.createElement("error");

            Element eltMessage = d.createElement("message");
            eltMessage.appendChild(d.createTextNode(errorMessage));
            eltError.appendChild(eltMessage);

            if(response!=null) {
                // This is an error
                response.setStatus(status);
            	response.setEntity(representation);
            }
            return representation;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
