package edu.berkeley.bps.services.corpus.sax;

import java.sql.Connection;
import java.util.Date;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;

import edu.berkeley.bps.services.common.hbtin.HBTIN_Constants;
import edu.berkeley.bps.services.common.sax.StackedContentHandler;
import edu.berkeley.bps.services.common.time.TimeUtils;
import edu.berkeley.bps.services.corpus.Activity;
import edu.berkeley.bps.services.corpus.ActivityRole;
import edu.berkeley.bps.services.corpus.Corpus;
import edu.berkeley.bps.services.corpus.Document;
import edu.berkeley.bps.services.corpus.TEI_Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO This main class belongs elsewhere - in common.sax
public class AssertionsContentHandler extends StackedContentHandler {
	final Logger logger = LoggerFactory.getLogger(AssertionsContentHandler.class);

	private static final String myClass = "AssertionsContentHandler";

	protected Corpus corpus;

	private String docName = null;
	private String dateString = null;
	private long date_norm = 0L;

	public AssertionsContentHandler(Corpus corpus, 
			XMLReader parser, ContentHandler previous) {
		super(parser, previous);
		this.corpus = corpus;
	}

	public void startElement(String namespaceURI, String localName, String qName, 
			Attributes attrList) {
		final String myName = ".startElement: ";
		super.startElement(namespaceURI, localName, qName, attrList);
		if(localName.equals("assertions")) {
			//logger.trace(myClass+myName+"Saw assertions wrapper");
		} else if(localName.equals("assert")) {
			//logger.trace(myClass+myName+"Saw assert element");
			docName = null;
			dateString = null;
		}
	}
	
	public void endElement(String namespaceURI, String localName, String qName) {
		final String myName = ".endElement: ";
		if(localName.equals("assert")) {
			if(docName==null) {
				throw new RuntimeException(myClass+
				" Assertions parse error: missing document resource.");
			}
			if(dateString==null) {
				throw new RuntimeException(myClass+
						" Assertions parse error: missing date value for document: "
						+docName);
			}
			Document doc = corpus.getDocumentByAltId(docName);
			if(doc==null) {
				String msg = myClass+
				" Assertions error: Unrecognized document resource: "
				+docName;
				logger.error(msg);
				//throw new RuntimeException(msg);
			} else {
				doc.setDate_str(dateString);
				doc.setDate_norm(date_norm);
				//logger.debug(myClass+myName+"Set year: "+dateString+
				//		" for document: "+docName);
			}
			// TODO move this to a handler for document dates. Allow this to take a handler
			// for assert content. Later, can have a registry of handlers by resource and property
		} else if(localName.equals("resource")) {
			final String resourcePrefix = "documents/"; 
			final int prefixLen = resourcePrefix.length();
			String resource = getCurrentText().trim();
			if(!resource.startsWith(resourcePrefix)) {
				throw new RuntimeException(myClass+
						" Assertions parse error: expecting document resource, found: "
						+resource);
			}
			docName = resource.substring(prefixLen);
		} else if(localName.equals("property")) {
			final String acceptedProperty = "date"; 
			if(!acceptedProperty.equalsIgnoreCase(getCurrentText().trim())) {
				throw new RuntimeException(myClass+" Assertions parse error: expecting date properties.");
			}
		} else if(localName.equals("value")) {
			dateString = getCurrentText().trim();
			try {
				int year = Integer.parseInt(dateString);
				date_norm = TimeUtils.getTimeInMillisForYear(year);
			} catch (NumberFormatException nfe) {
				throw new RuntimeException(myClass+" Assertions parse error: bad year value: "+dateString+
						" for document: "+docName);
			}
		}
		super.endElement(namespaceURI, localName, qName);
	}

}
