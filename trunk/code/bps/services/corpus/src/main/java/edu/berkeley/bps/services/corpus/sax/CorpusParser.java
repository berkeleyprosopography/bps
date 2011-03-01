package edu.berkeley.bps.services.corpus.sax;

import java.io.IOException;
import java.util.List;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import edu.berkeley.bps.services.corpus.Corpus;
import edu.berkeley.bps.services.corpus.Document;


public class CorpusParser {
	
	public static void buildFromTEI(Corpus corpus, String teiFile) {
    	//final String parserClass = "org.apache.xerces.parsers.SAXParser";
        try {
        	//XMLReader parser = XMLReaderFactory.createXMLReader(parserClass);
        	XMLReader parser = XMLReaderFactory.createXMLReader();
        	DefaultHandler defaultHandler = new DefaultHandler();
        	DefaultHandler corpusHandler = 
        		new CorpusContentHandler(corpus, parser, defaultHandler);
        	parser.setContentHandler(corpusHandler);
        	parser.setErrorHandler(defaultHandler);
        	System.err.println("Opening corpus file...");
        	try{
        		parser.parse(teiFile);
        	} catch(SAXException se) {
        		se.printStackTrace();
        		throw se;
        	} catch(IOException ioe) {
        		ioe.printStackTrace();
        	}
        	System.err.println("Corpus parsed.");
        	System.err.println(corpus.toString());
        	System.err.println("Corpus document count: "+corpus.getNDocuments());
        	System.err.println("Documents: ");
        	List<Document> docs = corpus.getDocuments();
        	for(Document doc:docs) {
            	System.err.println(" - "+doc.toString());
        	}
        } catch (Exception e) {
        	System.err.println(e.getLocalizedMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
	}

}
