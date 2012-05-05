package edu.berkeley.bps.services.corpus.sax;

import java.sql.Connection;
import java.util.ArrayList;
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
import edu.berkeley.bps.services.corpus.Name;
import edu.berkeley.bps.services.corpus.TEI_Constants;

// TODO This main class belongs elsewhere - in common.sax
public class NymContentHandler extends StackedContentHandler {
	
	private static final String myClass = "NymContentHandler";
	
	private class NameOrthography {
		String name;
		String id;
		public NameOrthography(String name, String id) {
			this.name = name;
			this.id = id;
		}
	}

	protected Corpus corpus;
	protected Connection dbConn;

	private String nymId = null;
	private String normalizedForm = null;
	private Name normalName = null;
	private String orthId = null;

	public NymContentHandler(Connection dbConn, Corpus corpus, 
			XMLReader parser, ContentHandler previous) {
		super(parser, previous);
		this.corpus = corpus;
		this.dbConn = dbConn;
	}

	public void startElement(String namespaceURI, String localName, String qName, 
			Attributes attrList) {
		final String myName = ".startElement: ";
		super.startElement(namespaceURI, localName, qName, attrList);
		if(localName.equals("nym")) {
			nymId = attrList.getValue("xml:id");
		} else if(localName.equals("form")) {
			if(normalizedForm!=null) {
				generateParseWarning(nymId, "Cannot handle multiple forms for one nym.");
			}
			normalizedForm = attrList.getValue("", "norm");
			if(normalizedForm==null) {
				generateParseWarning(nymId, "Form has missing/empty normalizedForm.");
			}
			if(nymId==null) {
				generateParseWarning(nymId, "Form has missing/empty nymId.");
			}
			normalName = corpus.findOrCreateName(normalizedForm, nymId,
								Name.NAME_TYPE_PERSON, Name.GENDER_UNKNOWN, dbConn);
		} else if(localName.equals("orth")) {
			orthId = attrList.getValue("xml:id");
		}
	}

	public void endElement(String namespaceURI, String localName, String qName) {
		final String myName = ".endElement: ";
		if(localName.equals("form")) {
			if(normalName==null || orthId==null) {
				generateParseWarning(nymId, "Failed to build normalName, or found not orthId.");
			}
		} else if(localName.equals("orth")) {
			if(orthId==null) {
				generateParseError(nymId," missing orth ID.");
			}
			String orth = getCurrentText().trim();
			Name variant = corpus.findOrCreateName(orth, orthId,
					Name.NAME_TYPE_PERSON, Name.GENDER_UNKNOWN, dbConn);
			variant.setNormal(normalName);
		}
		super.endElement(namespaceURI, localName, qName);
	}

	protected void generateParseError(String xmlid, String error ) {
		throw new RuntimeException(myClass+": Parse Error near element: "
				+((xmlid!=null)?xmlid:"(unknown)")
				+": "+error);
	}
	
	protected void generateParseWarning(String xmlid, String warning ) {
		System.err.println(myClass+": Parse Warning near element: "
				+((xmlid!=null)?xmlid:"(unknown)")
				+": "+warning);
	}
	
}
