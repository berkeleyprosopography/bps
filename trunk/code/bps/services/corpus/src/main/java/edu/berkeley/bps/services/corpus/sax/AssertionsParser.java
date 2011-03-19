package edu.berkeley.bps.services.corpus.sax;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import edu.berkeley.bps.services.corpus.CachedEntity;
import edu.berkeley.bps.services.corpus.Corpus;
import edu.berkeley.bps.services.corpus.Document;

public class AssertionsParser {
	
	public static void updateCorpusDates(Corpus corpus, String assertionsFile) {
        try {
        	XMLReader parser = XMLReaderFactory.createXMLReader();
        	DefaultHandler defaultHandler = new DefaultHandler();
        	DefaultHandler assertionsHandler = 
        		new AssertionsContentHandler(corpus, parser, defaultHandler);
        	parser.setContentHandler(assertionsHandler);
        	parser.setErrorHandler(defaultHandler);
        	System.err.println("Opening assertions file...");
        	try{
        		parser.parse(assertionsFile);
        	} catch(SAXException se) {
        		se.printStackTrace();
        		throw se;
        	} catch(IOException ioe) {
        		ioe.printStackTrace();
        	}
        	System.err.println("Assertions parsed.");
        } catch (Exception e) {
        	System.err.println(e.getLocalizedMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
	}

}
