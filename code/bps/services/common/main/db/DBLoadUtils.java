package bps.services.common.main.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.log4j.Logger;


/**
 * @author pschmitz
 *
 */
public class DBLoadUtils {
	static Logger logger = Logger.getLogger(DBLoadUtils.class);

	private String connectionUrl = null;

	public DBLoadUtils(String protocol, String host, String dbName, String user, String password ) {
		final String myName = "DBLoadUtils.Ctor: ";
		if(!"mysql".equals(protocol))
			throw new RuntimeException(
				myName+"db currently only supports mysql protocol.");
	    if( host.length() <= 0 )
			throw new RuntimeException(myName+"No host specified.");
	    if( dbName.length() <= 0 )
			throw new RuntimeException(myName+"No db name specified.");
	    if( user.length() <= 0 )
			throw new RuntimeException(myName+"No user name specified.");
	    if( password.length() <= 0 )
			throw new RuntimeException(myName+"No password specified.");

		connectionUrl = "jdbc:mysql://"+host
								+"/"+dbName
								+"?user="+user
								+"&password="+password;
		}

	/**
	 * @param pathToLoadfile Full path on the local system to a UTF8 encoded data file
	 * @return true if load succeeds, else false.
	 * @throws RuntimeException for any SQL or other errors.
	 */
	protected boolean setAppLockout(boolean lockoutActive ) {
		final String myName = "DBLoadUtils.setAppLockout: ";
		String mainStatement =
			"UPDATE DBInfo SET lockoutActive="+(lockoutActive?"true":"false");
		boolean success = false;
		Statement sqlStatement = null;
		Connection jdbcConnection = openConnection();
		try {
			if(jdbcConnection != null) {
				sqlStatement = jdbcConnection.createStatement();
				logger.debug(myName+"Setting lockout to: "+(lockoutActive?"true":"false"));
				sqlStatement.execute(mainStatement);
				success = true;
			}
		} catch (Exception e) {
			String tmp = myName+"\n"+ e.getMessage();
			logger.debug(tmp);
			throw new RuntimeException( tmp );
		} finally {
			closeConnection(jdbcConnection);
		}
		return success;
	}

	/**
	 * @param pathToLoadfile Full path on the local system to a UTF8 encoded data file
	 * @return true if load succeeds, else false.
	 * @throws RuntimeException for any SQL or other errors.
	 */
	protected boolean uploadCorpusMetadata(String pathToLoadfile) {
		return uploadStandardMetadata("corpus",
				"(id, name, description, owner)", null, pathToLoadfile);
	}


	/**
	 * @param pathToLoadfile Full path on the local system to a UTF8 encoded data file
	 * @return true if load succeeds, else false.
	 * @throws RuntimeException for any SQL or other errors.
	 */
	protected boolean uploadDocumentMetadata(String pathToLoadfile) {
		return uploadStandardMetadata("document",
				"(id, corpus_id, alt_id, sourcURL, xml_id, notes, date_str, date_norm)",
				null, pathToLoadfile);
	}

	/**
	 * @param pathToLoadfile Full path on the local system to a UTF8 encoded data file
	 * @return true if load succeeds, else false.
	 * @throws RuntimeException for any SQL or other errors.
	 */
	protected boolean uploadNameMetadata(String pathToLoadfile) {
		return uploadStandardMetadata("name",
				"(id, name, notes, normal)", null, pathToLoadfile);
	}

	/**
	 * @param pathToLoadfile Full path on the local system to a UTF8 encoded data file
	 * @return true if load succeeds, else false.
	 * @throws RuntimeException for any SQL or other errors.
	 */
	protected boolean uploadFamilyLinkMetadata(String pathToLoadfile) {
		return uploadStandardMetadata("familylink",
				"(id, nrad_id, name_id, link_type, xml_idref)", null, pathToLoadfile);
	}

	/**
	 * @param pathToLoadfile Full path on the local system to a UTF8 encoded data file
	 * @return true if load succeeds, else false.
	 * @throws RuntimeException for any SQL or other errors.
	 */
	protected boolean uploadAllActivityMetadata(
			String pathToActivityLoadfile,
			String pathToActivityRoleLoadfile,
			String pathToNameRoleActivityLoadfile ) {
		return
			uploadStandardMetadata("activity", "(id, name, description, parent",
				null, pathToActivityLoadfile)
			&& uploadStandardMetadata("act_role", "(id, name, description",
				null, pathToActivityRoleLoadfile)
			&& uploadStandardMetadata("name_role_activity_doc",
				"(id, name, act_role, activity, document, xml_idref",
				null, pathToNameRoleActivityLoadfile);
	}

	/**
	 * @param pathToLoadfile Full path on the local system to a UTF8 encoded data file
	 * @return true if load succeeds, else false.
	 * @throws RuntimeException for any SQL or other errors.
	 */
	private boolean uploadStandardMetadata(String tableName, String colDesc,
			String deleteStmt, String pathToLoadfile) {
		// Map WinDoz paths to normal ones.
		pathToLoadfile = pathToLoadfile.replace('\\', '/');
		String mainStmt =
			"LOAD DATA LOCAL INFILE '"+pathToLoadfile+"' INTO TABLE "+tableName
			+ " CHARACTER SET utf8 FIELDS TERMINATED BY '|' OPTIONALLY ENCLOSED BY '\"' "
			+ colDesc;
		return uploadData( tableName, mainStmt, deleteStmt);
	}

	private boolean uploadData(String table, String mainStmt, String deleteStmt) {
		final String myName = "DBLoadUtils.uploadData["+table+"]: ";
		boolean success = false;
		Statement sqlStatement = null;
		Connection jdbcConnection = openConnection();
		String utf8SetupStatement = "SET NAMES utf8";
		String disableKeysStatement = "ALTER TABLE "+table+" DISABLE KEYS";
		String truncateStatement = "TRUNCATE "+table;
		String enableKeysStatement = "ALTER TABLE "+table+" ENABLE KEYS";
		String getCountStatement = "SELECT COUNT(*) FROM "+table;
		try {
			if(jdbcConnection != null) {
				sqlStatement = jdbcConnection.createStatement();
				if(deleteStmt!=null) {
					logger.debug(myName+"Deleting entries from "+table+" table...");
					sqlStatement.execute(deleteStmt);
				} else {
					logger.debug(myName+"Truncating "+table+" table...");
					sqlStatement.execute(truncateStatement);
				}
				if(mainStmt!=null){
					logger.debug(myName+"Setting UTF* for names...");
					sqlStatement.execute(utf8SetupStatement);
					logger.debug(myName+"Disabling keys...");
					sqlStatement.execute(disableKeysStatement);
					logger.debug(myName+"Executing Load statement...");
					sqlStatement.execute(mainStmt);
					logger.debug(myName+"(Re-)enabling keys...");
					sqlStatement.execute(enableKeysStatement);
					logger.debug(myName+"Getting "+table+" count...");
					ResultSet results = sqlStatement.executeQuery(getCountStatement);
					if(results.next()) {
						int count = results.getInt(1);
						logger.debug(myName+table+" table now reports: "+count+" total rows.");
					} else {
						logger.error("Problem querying for "+table+" count.");
					}
				}
				success = true;
			}
		} catch (Exception e) {
			String tmp = myName+"\n"+ e.getMessage();
			logger.debug(tmp);
			throw new RuntimeException( tmp );
		} finally {
			closeConnection(jdbcConnection);
		}
		return success;
	}

	private Connection openConnection() {
		final String myName = "DBLoadUtils.getConnection: ";
		Connection jdbcConnection = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			jdbcConnection = DriverManager.getConnection(connectionUrl);
		} catch ( ClassNotFoundException cnfe ) {
			String tmp = myName+"Cannot load the SQLServerDriver class.";
			logger.error(tmp+"\n"+cnfe.getMessage());
			throw new RuntimeException(tmp);
		} catch (SQLException se) {
			String tmp = myName+"Problem connecting to DB. URL: "
				+"\n"+connectionUrl+"\n"+ se.getMessage();
			logger.error(tmp);
			throw new RuntimeException( tmp );
		} catch (Exception e) {
			String tmp = myName+"\n"+ e.getMessage();
			logger.debug(tmp);
			throw new RuntimeException( tmp );
		}
		return jdbcConnection;
	}

	private void closeConnection(Connection jdbcConnection) {
		if (jdbcConnection != null) try { jdbcConnection.close(); } catch(Exception e) {}
	}

	/*
	public static void main(String[] args) {
		final String myName = "DBLoadUtils.main: ";
		try {
			DBLoadUtils testDB = new DBLoadUtils("mysql", "localhost", "delphi", "delphi_admin", "please");
			boolean success = testDB.setAppLockout(true);
			StringUtils.outputDebugStr( myName+"setAppLockout(true):"+(success?"succeeded":"failed"));
			String filename = "E:/PAHMA/Metadata dumps/DBLoad/DBDump_090503b_100_SQL_all.txt";
			success = testDB.uploadObjectsMetadata(filename);
			StringUtils.outputDebugStr( myName+"uploadObjectsMetadata("
										+filename+"):"+((true)?"succeeded":"failed"));
			String facetsFile = "E:/PAHMA/Metadata dumps/DBLoad/mainVocab_facets.txt";
			String catsFile = "E:/PAHMA/Metadata dumps/DBLoad/mainVocab_cats.txt";
			String hooksFile = "E:/PAHMA/Metadata dumps/DBLoad/mainVocabHooks.txt";
			String exclusionsFile = "E:/PAHMA/Metadata dumps/DBLoad/mainVocabExclusions.txt";
			success = testDB.uploadOntologyMetadata(facetsFile, catsFile, hooksFile, exclusionsFile);
			StringUtils.outputDebugStr( myName+"uploadOntologyMetadata():"
												+((true)?"succeeded":"failed"));
			filename = "E:/PAHMA/Metadata dumps/DBLoad/obj_cats_all_1.sql";
			success = testDB.uploadObjectCategoryAssociations(filename);
			StringUtils.outputDebugStr( myName+"uploadObjectCategoryAssociations("
										+filename+"):"+((true)?"succeeded":"failed"));
			success = testDB.updateOntologyCategoryCounts();
			StringUtils.outputDebugStr( myName+"updateOntologyCategoryCounts():"
												+((true)?"succeeded":"failed"));
		} catch ( Exception e ) {
			StringUtils.outputDebugStr( myName+"Failed:"+e.getMessage() );
		}
	}
	*/

}
