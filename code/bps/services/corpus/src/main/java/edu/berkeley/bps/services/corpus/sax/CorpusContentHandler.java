package edu.berkeley.bps.services.corpus.sax;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;

import edu.berkeley.bps.services.common.sax.StackedContentHandler;
import edu.berkeley.bps.services.corpus.Corpus;

public class CorpusContentHandler extends StackedContentHandler {
	protected static String[] namepath = 
		{"teiHeader","fileDesc","titleStmt","title"};
	protected static String[] descpath = 
		{"teiHeader","fileDesc","titleStmt","title"};
	
	Corpus corpus;

	public CorpusContentHandler(Corpus corpus, 
			XMLReader parser, ContentHandler previous) {
		super(parser, previous);
		this.corpus = corpus;
	}
	
	public void endElement(String namespaceURI, String localName, String qName) {
		// Look for the teiHeader/fileDesc/titleStmt/title path
		if(pathMatches(namepath)) {
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
				new DocumentContentHandler(corpus,parser,this);
			// Pretend docCH was already set when we started this element
			docCH.startElement(namespaceURI, localName, qName, attrList);
			parser.setContentHandler(docCH);
		} else {
			super.startElement(namespaceURI, localName, qName, attrList);
		}
	}
	
}