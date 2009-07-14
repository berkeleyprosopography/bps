/**
 *
 */
package bps.services.corpus.main;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author pschmitz
 *
 */
public class SQLUtils {

	private static final String sep = "|";
	private static final String newLn = "\n";

	// TODO Consider allowing the Docs, Activities, etc. writes to append.

	public static void generateCorpusSQL(String filename, HashMap<Integer, Corpus> corpora) {
		// Open the files
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(
					  new OutputStreamWriter(
						new FileOutputStream(filename),"UTF8"));
			writer.append("To load this file, use a sql command:\n");
			writer.append("SET NAMES utf8;\n");
			writer.append("LOAD DATA LOCAL INFILE '"+filename+"' INTO TABLE corpus CHARACTER SET utf8\n");
			writer.append("FIELDS TERMINATED BY '"+sep+"' OPTIONALLY ENCLOSED BY '\"' IGNORE 6 LINES\n");
			writer.append("(id, name, description, owner)\n");
			writer.append("SET creation_time=now();\n");
		} catch( IOException e ) {
			throw new RuntimeException("Could not create output file: " + filename);
		}
		for( Corpus corpus:corpora.values() ) {
			try {
				// Generate another row in the documents table
				writer.append(corpus.toXMLLoadString(sep)+newLn);
			} catch( IOException e ) {
				throw new RuntimeException("Problem writing SQL entry for Corpus:"+corpus );
			}
		}
		try {
			writer.flush();
			writer.close();
		} catch( IOException e ) {
			throw new RuntimeException("Problem writing Corpus SQL.");
		}
	}

	public static void generateDocumentsSQL(String filename, String nameRoleActivitiesFilename,
			HashMap<Integer, Document> documents) {
		// Open the files
		BufferedWriter docsWriter = null;
		BufferedWriter nrasWriter = null;
		try {
			docsWriter = new BufferedWriter(
					  new OutputStreamWriter(
						new FileOutputStream(filename),"UTF8"));
			docsWriter.append("To load this file, use a sql command:\n");
			docsWriter.append("SET NAMES utf8;\n");
			docsWriter.append("LOAD DATA LOCAL INFILE '"+filename+"' INTO TABLE document CHARACTER SET utf8\n");
			docsWriter.append("FIELDS TERMINATED BY '"+sep+"' OPTIONALLY ENCLOSED BY '\"' IGNORE 6 LINES\n");
			docsWriter.append("(id, alt_id, sourcURL, xml_id, notes, date_str, date_norm)\n");
			docsWriter.append("SET creation_time=now();\n");
		} catch( IOException e ) {
			throw new RuntimeException("Could not create output file: " + filename);
		}
		try {
			nrasWriter = new BufferedWriter(
					  new OutputStreamWriter(
						new FileOutputStream(nameRoleActivitiesFilename),"UTF8"));
			nrasWriter.append("To load this file, use a sql command:\n");
			nrasWriter.append("SET NAMES utf8;\n");
			nrasWriter.append("LOAD DATA LOCAL INFILE '"+filename+"' INTO TABLE name_role_activity_doc CHARACTER SET utf8\n");
			nrasWriter.append("FIELDS TERMINATED BY '"+sep+"' OPTIONALLY ENCLOSED BY '\"' IGNORE 6 LINES\n");
			nrasWriter.append("(id, name, act_role, activity, document, xml_idref)\n");
			nrasWriter.append("SET creation_time=now();\n");
		} catch( IOException e ) {
			throw new RuntimeException("Could not create output file: " + nameRoleActivitiesFilename);
		}
		try {
			for( Document doc:documents.values() ) {
				// Generate another row in the documents table
				docsWriter.append(doc.toXMLLoadString(sep)+newLn);
				generateNameRoleActivityDocumentsSQL(nrasWriter, doc.getId(), doc.getNameRoleActivities());
			}
		} catch( IOException e ) {
            //debugTrace(2, e);
			throw new RuntimeException("Problem writing SQL entry for document." );
		}
	}

	public static void generateNameRoleActivityDocumentsSQL(Writer writer,
			int docID, ArrayList<NameRoleActivity> nameRoleActivities) {
		for( NameRoleActivity nra:nameRoleActivities ) {
			try {
				// Generate another row in the documents table
				writer.append(nra.toXMLLoadString(sep, docID)+newLn);
			} catch( IOException e ) {
	            //debugTrace(2, e);
				throw new RuntimeException("Problem writing SQL entry for NameRoleActivity:"+
												nra+" in doc: "+docID);
			}
		}
	}

	public static void generateActivitiesSQL(String filename, HashMap<String, Activity> activities) {
		// Open the files
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(
					  new OutputStreamWriter(
						new FileOutputStream(filename),"UTF8"));
			writer.append("To load this file, use a sql command:\n");
			writer.append("SET NAMES utf8;\n");
			writer.append("LOAD DATA LOCAL INFILE '"+filename+"' INTO TABLE activity CHARACTER SET utf8\n");
			writer.append("FIELDS TERMINATED BY '"+sep+"' OPTIONALLY ENCLOSED BY '\"' IGNORE 6 LINES\n");
			writer.append("(id, name, description, parent)\n");
			writer.append("SET creation_time=now();\n");
		} catch( IOException e ) {
			throw new RuntimeException("Could not create output file: " + filename);
		}
		for( Activity activity:activities.values() ) {
			try {
				// Generate another row in the documents table
				writer.append(activity.toXMLLoadString(sep)+newLn);
			} catch( IOException e ) {
				throw new RuntimeException("Problem writing SQL entry for Activity:"+activity );
			}
		}
	}

	public static void generateActivityRolesSQL(String filename, HashMap<String, ActivityRole> activityRoles) {
		// Open the files
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(
					  new OutputStreamWriter(
						new FileOutputStream(filename),"UTF8"));
			writer.append("To load this file, use a sql command:\n");
			writer.append("SET NAMES utf8;\n");
			writer.append("LOAD DATA LOCAL INFILE '"+filename+"' INTO TABLE act_role CHARACTER SET utf8\n");
			writer.append("FIELDS TERMINATED BY '"+sep+"' OPTIONALLY ENCLOSED BY '\"' IGNORE 6 LINES\n");
			writer.append("(id, name, description)\n");
			writer.append("SET creation_time=now();\n");
		} catch( IOException e ) {
			throw new RuntimeException("Could not create output file: " + filename);
		}
		for( ActivityRole ar:activityRoles.values() ) {
			try {
				// Generate another row in the documents table
				writer.append(ar.toXMLLoadString(sep)+newLn);
			} catch( IOException e ) {
				throw new RuntimeException("Problem writing SQL entry for ActivityRole:"+ar );
			}
		}
	}

	public static void generateNamesSQL(String filename, HashMap<String, Name> names) {
		// Open the files
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(
					  new OutputStreamWriter(
						new FileOutputStream(filename),"UTF8"));
			writer.append("To load this file, use a sql command:\n");
			writer.append("SET NAMES utf8;\n");
			writer.append("LOAD DATA LOCAL INFILE '"+filename+"' INTO TABLE name CHARACTER SET utf8\n");
			writer.append("FIELDS TERMINATED BY '"+sep+"' OPTIONALLY ENCLOSED BY '\"' IGNORE 6 LINES\n");
			writer.append("(id, name, notes, normal)\n");
			writer.append("SET creation_time=now();\n");
		} catch( IOException e ) {
			throw new RuntimeException("Could not create output file: " + filename);
		}
		for( Name name:names.values() ) {
			try {
				// Generate another row in the documents table
				writer.append(name.toXMLLoadString(sep)+newLn);
			} catch( IOException e ) {
				throw new RuntimeException("Problem writing SQL entry for Name:"+name );
			}
		}
	}

}
