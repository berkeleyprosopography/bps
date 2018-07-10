package edu.berkeley.bps.services.common.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TestDBBase extends TestCase {
	
	final Logger logger = LoggerFactory.getLogger(TestDBBase.class);
	
	String host = "localhost";
        // These should be pulled from properties files.
	String dbName = "name";
	String dbUser = "user";
	String dbPass = "password";

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
			logger.error(tmp);
			throw new RuntimeException( tmp );
		}
		try {
			Class.forName("com.mysql.jdbc.Driver");
			jdbcConnection = DriverManager.getConnection(connectionUrl);
		} catch ( ClassNotFoundException cnfe ) {
			String tmp = myName+"Cannot load the SQL Driver class.";
			logger.error(tmp);
			logger.error(cnfe.getMessage());
			throw new RuntimeException(tmp);
		} catch (SQLException se) {
			String tmp = myName+"Problem connecting to DB. URL: "
				+"\n"+connectionUrl+"\n"+ se.getMessage();
			logger.error(tmp);
			throw new RuntimeException( tmp );
		} catch (Exception e) {
			String tmp = myName+"\n"+ e.getMessage();
			logger.error(tmp);
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

