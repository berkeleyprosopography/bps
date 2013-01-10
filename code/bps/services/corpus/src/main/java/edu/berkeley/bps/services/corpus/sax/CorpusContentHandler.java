package edu.berkeley.bps.services.corpus.sax;

import java.sql.Connection;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;

import edu.berkeley.bps.services.common.sax.StackedContentHandler;
import edu.berkeley.bps.services.corpus.Corpus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CorpusContentHandler extends StackedContentHandler {
	final Logger logger = LoggerFactory.getLogger(CorpusContentHandler.class);
	protected static String[] namepath = 
		{"teiCorpus","teiHeader","fileDesc","titleStmt","title"};
	protected static String[] descpath = 
		{"teiCorpus","teiHeader","fileDesc","sourceDesc","p"};
	
	protected Corpus corpus;
	protected Connection dbConn;

	public CorpusContentHandler(Connection dbConn, Corpus corpus, 
			XMLReader parser, ContentHandler previous) {
		super(parser, previous);
		this.corpus = corpus;
		this.dbConn = dbConn;
	}
	
	public void endElement(String namespaceURI, String localName, String qName) {
		// Look for the teiHeader/fileDesc/titleStmt/title path
		if(pathMatches(namepath)) {
			// Do not override the name, if one has been set.
			if(corpus.getName()==null||corpus.getName().isEmpty())
				corpus.setName(getCurrentText().trim().replaceAll("[\\s]+", " ")); 
		} else if(pathMatches(descpath)) {
			corpus.setDescription(getCurrentText().trim().replaceAll("[\\s]+", " ")); 
		}
		super.endElement(namespaceURI, localName, qName);
	}
	
	public void startElement(String namespaceURI, String localName, String qName, 
			Attributes attrList) {
		// Do not call super, if we are passing this off elsewhere
		if(localName.equals("TEI")) {
			DocumentContentHandler docCH = 
				new DocumentContentHandler(dbConn, corpus,parser,this);
			// Pretend docCH was already set when we started this element
			docCH.startElement(namespaceURI, localName, qName, attrList);
			parser.setContentHandler(docCH);
		} else {
			super.startElement(namespaceURI, localName, qName, attrList);
		}
	}
	
}
