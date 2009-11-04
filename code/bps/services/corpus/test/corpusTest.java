package bps.services.corpus.test;

import java.util.HashMap;

import bps.services.corpus.main.Corpus;
import bps.services.corpus.main.SQLUtils;
import bps.services.corpus.main.XMLUtils;

public class corpusTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int i = 0;
		String arg;
		String corpusFile = null;

        while (i < args.length && args[i].startsWith("-")) {
            arg = args[i++];
            // Look for corpus arg
            if(arg.equals("-c")) {
                if (i < args.length)
                	corpusFile = args[i++];
                else
                    System.err.println("-c requires a filename");
            } else {
            	System.err.println("Unknown option: "+arg);
            }
        }
        if(corpusFile==null) {
        	System.err.println("Missing corpus file argument.");
        	System.exit(0);
        }
        String outFileBase = corpusFile.replace(".xml", "_");
        String corpusSQLfile = outFileBase+"corpus_load.txt";
        String docsSQLfile = outFileBase+"document_load.txt";
        String activitiesSQLfile = outFileBase+"activity_load.txt";
        String namesSQLfile = outFileBase+"name_load.txt";
        String activityRolesSQLfile = outFileBase+"activityRole_load.txt";
        String nameRoleActivitiesSQLfile = outFileBase+"nameRoleActivity_load.txt";
        HashMap<Integer, Corpus> corpora = new HashMap<Integer, Corpus>();
        try {
        	System.out.print("Opening corpus file...");
	        org.w3c.dom.Document doc = XMLUtils.OpenXMLFile("file:"+corpusFile);
        	System.out.println("Done.");
        	System.out.println("Creating corpus...");
			Corpus testCorpus = Corpus.CreateFromTEI(doc, true);
        	System.out.println("Done.");
			corpora.put(testCorpus.getId(), testCorpus);
        	System.out.print("Generating Corpus SQL...");
			SQLUtils.generateCorpusSQL(corpusSQLfile, corpora);
        	System.out.println("Done.");
        	System.out.println("Generating Dependent SQL...");
			testCorpus.generateDependentSQL(docsSQLfile, activitiesSQLfile,
					namesSQLfile, activityRolesSQLfile, nameRoleActivitiesSQLfile);
        	System.out.println("Corpus test completed.");
        } catch (Exception e) {
        	System.err.println(e);
            e.printStackTrace();
        }
	}

}
