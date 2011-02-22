package edu.berkeley.bps.services.corpus.test;

import java.io.IOException;
import java.util.List;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import edu.berkeley.bps.services.corpus.*;
import edu.berkeley.bps.services.corpus.sax.CorpusContentHandler;

public class CorpusSAXTest {

	public static void printUsage(String error) {
		if(error!=null)
			System.err.println(error);
		System.err.println("Usage: java CorpusSaxTest -c {corpusfile}");
	}
	
	public static void parse(String corpusFile) {
    	final String parserClass = "org.apache.xerces.parsers.SAXParser";
        try {
        	//XMLReader parser = XMLReaderFactory.createXMLReader(parserClass);
        	XMLReader parser = XMLReaderFactory.createXMLReader();
        	Corpus corpus = new Corpus();
        	DefaultHandler defaultHandler = new DefaultHandler();
        	DefaultHandler corpusHandler = 
        		new CorpusContentHandler(corpus, parser, defaultHandler);
        	parser.setContentHandler(corpusHandler);
        	parser.setErrorHandler(defaultHandler);
        	System.out.println("Opening corpus file...");
        	try{
        		parser.parse(corpusFile);
        	} catch(SAXException se) {
        		se.printStackTrace();
        	} catch(IOException ioe) {
        		ioe.printStackTrace();
        	}
        	System.out.println("Corpus parsed.");
        	System.out.println(corpus.toString());
        	System.out.println("Corpus document count: "+corpus.getNDocuments());
        	System.out.println("Documents: ");
        	List<Document> docs = corpus.getDocuments();
        	for(Document doc:docs) {
            	System.out.println(" - "+doc.toString());
        	}
        } catch (SAXException se) {
        	System.out.println(se.getLocalizedMessage());
        } catch (Exception e) {
        	System.err.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int i = 0;
		String arg;
		String corpusFile = null;
        // Defaults come from original test corpus - HBTIN
        while (i < args.length && args[i].startsWith("-")) {
            arg = args[i++];
            // Look for corpus arg
            if(arg.equals("-c")) {
                if (i < args.length)
                	corpusFile = args[i++];
                else
                    printUsage("-c requires a filename");
            } else {
            	printUsage("Unknown option: "+arg);
            }
        }
        if(corpusFile==null) {
        	printUsage("Missing corpus file argument.");
        	System.exit(0);
        }
        try {
        	parse(corpusFile);
        } catch (Exception e) {
        	System.err.println(e);
            e.printStackTrace();
        }
	}

}
