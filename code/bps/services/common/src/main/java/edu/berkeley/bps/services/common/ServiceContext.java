package edu.berkeley.bps.services.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class ServiceContext {
	public static final String label = "ServiceContext";
	
	// TODO Really need to test and handle dropped connections
	private Connection dbConn = null;
	private String connectionUrl = null;
	private int SECONDS_TO_WAIT_FOR_VALID_CONNECTION = 3;
	
	private HashMap<String, Object> properties = new HashMap<String, Object>(); 
	
	public ServiceContext(String connectionUrl) {
		this.connectionUrl = connectionUrl;
	}
	
	protected void finalize() throws Throwable
	{
	  closeConnection();
	} 
	
	public Connection getConnection() {
		return getConnection(false);
	}
	
	public Connection getConnection(boolean fOnlyIfAvailable) {
		if(dbConn==null) {
			openConnection();
		}
		if(!checkConnection()) {
			closeConnection();
			openConnection();
		}
		if(fOnlyIfAvailable && (dbConn != null) && !isAvailable()) {
			return null;
		}
		return dbConn;
	}
	
	public boolean isAvailable() {
		final String myName = ".isAvailable: ";
		if(dbConn == null) {
			String tmp = label+myName+"No dbConnection set.";
			System.out.println(tmp);
			throw new RuntimeException( tmp );
		}
		try {
			Statement stmt = dbConn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT lockoutActive FROM DBInfo");
			if(rs.next()){
				return !rs.getBoolean("lockoutActive"); 
			} else {
				String tmp = label+myName+"DBInfo table not correctly initialized!";
				System.out.println(tmp);
			}
			rs.close();
		} catch (SQLException se) {
			String tmp = label+myName+"Problem connecting to DB. URL: "
				+"\n"+connectionUrl+"\n"+ se.getMessage();
			System.out.println(tmp);
			throw new RuntimeException( tmp );
		} catch (Exception e) {
			String tmp = label+myName+"\n"+ e.getMessage();
			System.out.println(tmp);
			throw new RuntimeException( tmp );
		}
		return false;
	}
	
	public void setProperty(String name, Object value) {
		properties.put(name, value);
	}
	
	public Object getProperty(String name) {
		return properties.get(name);
	}
	
	protected void openConnection() {
		final String myName = ".openConnection: ";
		if(connectionUrl == null) {
			String tmp = label+myName+"No connectionUrl set.";
			System.out.println(tmp);
			throw new RuntimeException( tmp );
		}
		boolean valid = false;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			dbConn = DriverManager.getConnection(connectionUrl);
			valid = dbConn.isValid(SECONDS_TO_WAIT_FOR_VALID_CONNECTION);
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
		if(!valid) {
			String tmp = myName+"Problem connecting to DB. URL: "
			+"\n"+connectionUrl+"\nConnection succeeded, but DB not valid";
			System.out.println(tmp);
			throw new RuntimeException( tmp );
		}
	}
	

	protected void closeConnection() {
		if (dbConn != null) try { dbConn.close(); dbConn=null;} catch(Exception e) {}
	}

	protected boolean checkConnection() {
		final String myName = ".checkConnection: ";
		try { 
			if (dbConn == null)
				return dbConn.isValid(3);
		} catch (SQLException se) {
			String tmp = myName+"Problem connecting to DB. \n"+ se.getMessage();
			System.out.println(tmp);
		}
		return false;
	}


}
