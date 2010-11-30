package edu.berkeley.bps.services.common.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.TestCase;

public abstract class TestDBBase extends TestCase {
	String host = "localhost";
	String dbName = "bpsdev";
	String dbUser = "bpsdev";
	String dbPass = "G0Names!";

	private String connectionUrl = null;
	private Connection jdbcConnection = null;
	
	public TestDBBase(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		connectionUrl = 
			"jdbc:mysql://"+host+"/"+dbName+"?user="+dbUser+"&password="+dbPass;
		openConnection();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		closeConnection();
	}

	
	private void openConnection() {
		final String myName = "openConnection: ";
		if(connectionUrl == null) {
			String tmp = myName+"No connectionUrl set.";
			System.out.println(tmp);
			throw new RuntimeException( tmp );
		}
		try {
			Class.forName("com.mysql.jdbc.Driver");
			jdbcConnection = DriverManager.getConnection(connectionUrl);
		} catch ( ClassNotFoundException cnfe ) {
			String tmp = myName+"Cannot load the SQL Driver class.";
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
	}
	
	protected void closeConnection() {
		if (jdbcConnection != null) try { jdbcConnection.close(); } catch(Exception e) {}
	}

	protected Connection getConnection() {
		return jdbcConnection;
	}

}

