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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author pschmitz
 *
 */
public class SQLUtils {

	private static final String sep = "|";
	private static final String newLn = "\n";
	private static final String nullStr = "\\N";

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
				writer.append(corpus.toXMLLoadString(sep, nullStr)+newLn);
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

	public static void generateDocumentsSQL(
			String filename, String nameRoleActivitiesFilename,
			 String nameFamilyLinksFilename,
			HashMap<Integer, Document> documents) {
		// Open the files
		BufferedWriter docsWriter = null;
		BufferedWriter nrasWriter = null;
		BufferedWriter nflsWriter = null;
		try {
			docsWriter = new BufferedWriter(
					  new OutputStreamWriter(
						new FileOutputStream(filename),"UTF8"));
			docsWriter.append("To load this file, use a sql command:\n");
			docsWriter.append("SET NAMES utf8;\n");
			docsWriter.append("LOAD DATA LOCAL INFILE '"+filename+"' INTO TABLE document CHARACTER SET utf8\n");
			docsWriter.append("FIELDS TERMINATED BY '"+sep+"' OPTIONALLY ENCLOSED BY '\"' IGNORE 6 LINES\n");
			docsWriter.append("(id, corpus_id, alt_id, sourcURL, xml_id, notes, date_str, date_norm)\n");
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
			nflsWriter = new BufferedWriter(
					  new OutputStreamWriter(
						new FileOutputStream(nameFamilyLinksFilename),"UTF8"));
			nflsWriter.append("To load this file, use a sql command:\n");
			nflsWriter.append("SET NAMES utf8;\n");
			nflsWriter.append("LOAD DATA LOCAL INFILE '"+filename+"' INTO TABLE familylink CHARACTER SET utf8\n");
			nflsWriter.append("FIELDS TERMINATED BY '"+sep+"' OPTIONALLY ENCLOSED BY '\"' IGNORE 6 LINES\n");
			nflsWriter.append("(id, nrad_id, name_id, link_type, xml_idref)\n");
			nflsWriter.append("SET creation_time=now();\n");
		} catch( IOException e ) {
			throw new RuntimeException("Could not create output file: " + nameRoleActivitiesFilename);
		}
		try {
			ArrayList<Integer> idsAsList = new ArrayList<Integer>(documents.keySet());
			Collections.sort(idsAsList);
			for( Integer id:idsAsList ) {
				Document doc = documents.get(id);
				// Generate another row in the documents table
				docsWriter.append(doc.toXMLLoadString(sep, nullStr)+newLn);
				generateNameRoleActivityDocumentsSQL(nrasWriter, nflsWriter,
						id, doc.getNameRoleActivities());
			}
		} catch( IOException e ) {
            //debugTrace(2, e);
			throw new RuntimeException("Problem writing SQL entry for document." );
		}
		try {
			docsWriter.flush();
			docsWriter.close();
			nrasWriter.flush();
			nrasWriter.close();
			nflsWriter.flush();
			nflsWriter.close();
		} catch( IOException e ) {
			throw new RuntimeException("Problem writing Document or NameRoleActivityDoc SQL.");
		}
	}

	public static void generateNameRoleActivityDocumentsSQL(
			Writer nraWriter, Writer nflWriter, int docID,
			ArrayList<NameRoleActivity> nameRoleActivities) {
		for( NameRoleActivity nra:nameRoleActivities ) {
			try {
				// Generate another row in the documents table
				nraWriter.append(nra.toXMLLoadString(docID, sep, nullStr)+newLn);
				generateNameFamilyLinksSQL( nflWriter, nra);
			} catch( IOException e ) {
	            //debugTrace(2, e);
				throw new RuntimeException("Problem writing SQL entry for NameRoleActivity:"+
												nra+" in doc: "+docID);
			}
		}
	}

	public static void generateNameFamilyLinksSQL(Writer writer,
			NameRoleActivity nameRoleActivity) {
		ArrayList<NameFamilyLink> nameFamilyLinks =
			nameRoleActivity.getNameFamilyLinks();
		if(nameFamilyLinks != null ) {
			int nraId = nameRoleActivity.getId();
			for( NameFamilyLink nfl:nameFamilyLinks ) {
				try {
					// Generate another row in the documents table
					writer.append(nfl.toXMLLoadString(nraId, sep, nullStr)+newLn);
				} catch( IOException e ) {
		            //debugTrace(2, e);
					throw new RuntimeException("Problem writing SQL entry for NameFamilyLink:"+
													nfl+" for nra: "+nameRoleActivity);
				}
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
				writer.append(activity.toXMLLoadString(sep, nullStr)+newLn);
			} catch( IOException e ) {
				throw new RuntimeException("Problem writing SQL entry for Activity:"+activity );
			}
		}
		try {
			writer.flush();
			writer.close();
		} catch( IOException e ) {
			throw new RuntimeException("Problem writing Activity SQL.");
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
				writer.append(ar.toXMLLoadString(sep, nullStr)+newLn);
			} catch( IOException e ) {
				throw new RuntimeException("Problem writing SQL entry for ActivityRole:"+ar );
			}
		}
		try {
			writer.flush();
			writer.close();
		} catch( IOException e ) {
			throw new RuntimeException("Problem writing ActivityRole SQL.");
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
				writer.append(name.toXMLLoadString(sep, nullStr)+newLn);
			} catch( IOException e ) {
				throw new RuntimeException("Problem writing SQL entry for Name:"+name );
			}
		}
		try {
			writer.flush();
			writer.close();
		} catch( IOException e ) {
			throw new RuntimeException("Problem writing Name SQL.");
		}
	}

}
