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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CorpusParser {
	static final Logger logger = LoggerFactory.getLogger(CorpusParser.class);
	
	public static void buildFromTEI(Connection dbConn, Corpus corpus, String teiFile) {
        try {
        	XMLReader parser = XMLReaderFactory.createXMLReader();
        	DefaultHandler defaultHandler = new DefaultHandler();
        	DefaultHandler corpusHandler = 
        		new CorpusContentHandler(dbConn, corpus, parser, defaultHandler);
        	parser.setContentHandler(corpusHandler);
        	parser.setErrorHandler(defaultHandler);
        	logger.debug("Opening corpus file...");
        	try{
        		parser.parse(teiFile);
        	} catch(SAXException se) {
        		se.printStackTrace();
        		throw se;
        	} catch(IOException ioe) {
        		ioe.printStackTrace();
        	}
        	logger.debug("Corpus parsed; found {} documents", corpus.getNDocuments());
        	/*
        	logger.debug(corpus.toString());
        	logger.trace("Documents: ");
        	List<Document> docs = corpus.getDocuments();
        	for(Document doc:docs) {
            	logger.trace(" - "+doc.toString());
        	}
        	*/
        } catch (Exception e) {
        	logger.error(e.getLocalizedMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
	}

}
