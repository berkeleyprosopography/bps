package edu.berkeley.bps.services.corpus.sax;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;

import edu.berkeley.bps.services.common.sax.StackedContentHandler;
import edu.berkeley.bps.services.corpus.Activity;
import edu.berkeley.bps.services.corpus.Corpus;
import edu.berkeley.bps.services.corpus.Document;
import edu.berkeley.bps.services.corpus.TEI_Constants;

public class DocumentContentHandler extends StackedContentHandler {
	protected static String[] namepath = 
	{"TEI","text","body"};

	protected Corpus corpus;
	protected Document document;
	protected Activity activity;

	private boolean inText_transliteration = false;
	private boolean inBody = false;
	private boolean onBack = false;
	private boolean inWitnesses = false;
	private boolean onAltIDElement = false;

	public DocumentContentHandler(Corpus corpus, 
			XMLReader parser, ContentHandler previous) {
		super(parser, previous);
		this.corpus = corpus;
		document = new Document(corpus);
		// TODO set the activity to be "Unknown" with FindOrAdd...
		activity = null;
	}

	public void startElement(String namespaceURI, String localName, String qName, 
			Attributes attrList) {
		super.startElement(namespaceURI, localName, qName, attrList);
		if(pathMatches(TEI_Constants.ALT_ID_PATH)
				&& TEI_Constants.ALT_ID_PATH_TYPE_ATTR.equalsIgnoreCase(
						attrList.getValue(namespaceURI, "type"))) {
			onAltIDElement = true;
		} else if(localName.equals("text")) {
			// This may have to be more flexible, saving the stack size
			// and then clear this when we see endEl with text for saved stack size
			if("transliteration".equalsIgnoreCase(
					attrList.getValue(namespaceURI, "type"))) {
				inText_transliteration = true;
			} else {
				inText_transliteration = false;
			}
		} else if(inText_transliteration) {
			if(localName.equals("body")) {
				inBody = true;
				onBack = false;
				inWitnesses = false;
			} else if(localName.equals("back")) {
				inBody = false;
				onBack = true;
				inWitnesses = false;
			} else if(localName.equals("div")) {
				if(onBack && "witnesses".equalsIgnoreCase(
							attrList.getValue(namespaceURI, "subtype"))) {
				inWitnesses = true;
				}
			} else if(localName.equals("persName")) {
				if(inBody) {
					// Create a persNameHandler with the Principle Role,
					// Passing in the activity (unknown) on this doc
				} else if(inWitnesses) {
					// Create a persNameHandler with the Witness Role,
					// Passing in the activity (unknown) on this doc
				}
			}
		}
	}
	
	
	public void endElement(String namespaceURI, String localName, String qName) {
		// Look for the teiHeader/fileDesc/titleStmt/title path
		if(onAltIDElement) {
			document.setAlt_id(getCurrentText().trim().replaceAll("[\\s]+", " "));
			onAltIDElement = false;
		}
		super.endElement(namespaceURI, localName, qName);
	}
	

}
