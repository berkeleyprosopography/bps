package edu.berkeley.bps.services.corpus.test;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import edu.berkeley.bps.services.common.time.*;
import edu.berkeley.bps.services.corpus.Corpus;
import edu.berkeley.bps.services.corpus.SQLUtils;
import edu.berkeley.bps.services.corpus.XMLUtils;

public class corpusTest {

	public static void printUsage(String error) {
		if(error!=null)
			System.err.println(error);

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int i = 0;
		String arg;
		String corpusFile = null;
        // Defaults come from original test corpus - HBTIN
        double corpusTSStdDev =
    		TimeUtils.getApproxTimeInMillisForYearOffset(50);
        long corpusCenterPoint = TimeUtils.getTimeInMillisForYear(-142);

        while (i < args.length && args[i].startsWith("-")) {
            arg = args[i++];
            // Look for corpus arg
            if(arg.equals("-c")) {
                if (i < args.length)
                	corpusFile = args[i++];
                else
                    printUsage("-c requires a filename");
            } else if(arg.equals("-cy")) {
                if (i < args.length) {
                	int year = Integer.parseInt(args[i++]);
                	corpusCenterPoint = TimeUtils.getTimeInMillisForYear(year);
                } else
                	printUsage("-cy requires a year");
            } else if(arg.equals("-sd")) {
                if (i < args.length) {
                	double stdDevYrs = Double.parseDouble(args[i++]);
                	corpusTSStdDev =
                		TimeUtils.getApproxTimeInMillisForYearOffset(stdDevYrs);
                } else
                	printUsage("-cy requires a year");
            } else {
            	printUsage("Unknown option: "+arg);
            }
        }
        if(corpusFile==null) {
        	printUsage("Missing corpus file argument.");
        	System.exit(0);
        }
        String outFileBase = corpusFile.replace(".xml", "_");
        String corpusSQLfile = outFileBase+"corpus_load.txt";
        String docsSQLfile = outFileBase+"document_load.txt";
        String activitiesSQLfile = outFileBase+"activity_load.txt";
        String namesSQLfile = outFileBase+"name_load.txt";
        String familyLinksSQLfile = outFileBase+"familyLink_load.txt";
        String activityRolesSQLfile = outFileBase+"activityRole_load.txt";
        String nameRoleActivitiesSQLfile = outFileBase+"nameRoleActivity_load.txt";
        HashMap<Integer, Corpus> corpora = new HashMap<Integer, Corpus>();
        try {
        	System.out.print("Opening corpus file...");
	        org.w3c.dom.Document doc = XMLUtils.OpenXMLFile("file:"+corpusFile);
        	System.out.println("Done.");
        	System.out.print("Creating corpus with centerpoint: "
        			+TimeUtils.millisToSimpleYearString(corpusCenterPoint)
        			+" and StdDev: "
        			+TimeUtils.millisToYearOffsetString(corpusTSStdDev)
        			+" (yrs)");

        	EvidenceBasedTimeSpan defaultCorpusTS =
        		new EvidenceBasedTimeSpan(corpusCenterPoint, corpusTSStdDev);
			Corpus testCorpus = Corpus.CreateFromTEI(doc, true, defaultCorpusTS);
        	System.out.println("Done.");
			corpora.put(testCorpus.getId(), testCorpus);
        	System.out.print("Generating Corpus SQL...");
			SQLUtils.generateCorpusSQL(corpusSQLfile, corpora);
        	System.out.println("Done.");
        	System.out.println("Generating Dependent SQL...");
			testCorpus.generateDependentSQL(docsSQLfile, activitiesSQLfile,
					namesSQLfile, familyLinksSQLfile,
					activityRolesSQLfile, nameRoleActivitiesSQLfile);
        	System.out.println("Corpus test completed.");
        } catch (Exception e) {
        	System.err.println(e);
            e.printStackTrace();
        }
	}

}
