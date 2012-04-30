package edu.berkeley.bps.services.corpus.sax;

import java.sql.Connection;

import org.jboss.resteasy.annotations.cache.Cache;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;

import edu.berkeley.bps.services.common.hbtin.HBTIN_Constants;
import edu.berkeley.bps.services.common.sax.StackedContentHandler;
import edu.berkeley.bps.services.corpus.Activity;
import edu.berkeley.bps.services.corpus.ActivityRole;
import edu.berkeley.bps.services.corpus.CachedEntity;
import edu.berkeley.bps.services.corpus.Corpus;
import edu.berkeley.bps.services.corpus.Document;
import edu.berkeley.bps.services.corpus.TEI_Constants;

public class DocumentContentHandler extends StackedContentHandler {
	protected static String[] namepath = 
	{"TEI","text","body"};
	

	protected Connection dbConn;
	protected Corpus corpus;
	protected Document document;
	protected Activity activity;
	// TODO These are HBTIN specific, and should be handled generally
	protected ActivityRole principalAR;
	protected ActivityRole witnessAR;
	
	private boolean inText_transliteration = false;
	private boolean inNymList = false;
	private boolean inBody = false;
	private boolean onBack = false;
	private boolean inWitnesses = false;
	private boolean onAltIDElement = false;
	private boolean onPrimPubElement = false;
	private boolean foundAltIDElement = false;
	private boolean foundPrimPubElement = false;
	private boolean notADocument = false;

	public DocumentContentHandler(Connection dbConn, Corpus corpus, 
			XMLReader parser, ContentHandler previous) {
		super(parser, previous);
		this.dbConn = dbConn;
		this.corpus = corpus;
		document = new Document(corpus);
		activity = corpus.findOrCreateActivity(HBTIN_Constants.ACTIVITY_UNKNOWN, dbConn);
		principalAR = corpus.findOrCreateActivityRole(HBTIN_Constants.ROLE_PRINCIPAL, dbConn);
		witnessAR = corpus.findOrCreateActivityRole(HBTIN_Constants.ROLE_WITNESS, dbConn);
	}

	public void startElement(String namespaceURI, String localName, String qName, 
			Attributes attrList) {
		super.startElement(namespaceURI, localName, qName, attrList);
		if(pathMatches(TEI_Constants.ALT_ID_PATH)){
			String type = attrList.getValue("", "type");  
			onAltIDElement = (TEI_Constants.ALT_ID_PATH_TYPE_ATTR.equalsIgnoreCase(type));
			onPrimPubElement = (TEI_Constants.PRIMARY_PUB_PATH_TYPE_ATTR.equalsIgnoreCase(type));
			/*
			 if(onAltIDElement)
				System.err.println("Found AltIDElement");
			else
				System.err.println("Almost found AltIDElement; type:"+type);
			*/
		} else if(localName.equals("text")) {
			// This may have to be more flexible, saving the stack size
			// and then clear this when we see endEl with text for saved stack size
			String type = attrList.getValue("", "type");  
			if("transliteration".equalsIgnoreCase(type)) {
				inText_transliteration = true;
			} else if("listNym".equalsIgnoreCase(type)) {
				notADocument = true;
				inNymList = true;
			} else if("listPerson".equalsIgnoreCase(type)) {
				notADocument = true;
			} else {
				inText_transliteration = false;
			}
		} else if(inNymList) {
			if(localName.equals("nym")) {
				NymContentHandler nymCH = 
					new NymContentHandler(dbConn, corpus, parser, this);
				// Pretend nymCH was already set when we started this element
				elPath.pop();	// nymCH will close this element out.
				nymCH.startElement(namespaceURI, localName, qName, attrList);
				parser.setContentHandler(nymCH);
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
				if(onBack){
					String subtype = attrList.getValue("", "subtype");  
					if("witnesses".equalsIgnoreCase(subtype))
						inWitnesses = true;
				}
			} else if(localName.equals("persName")) {
				ActivityRole ar = null;
				if(inBody) {
					ar = principalAR;
				} else if(inWitnesses) {
					ar = witnessAR;
				}
				if(ar!=null) {
					PersonNameContentHandler pnCH = 
						new PersonNameContentHandler(dbConn, corpus,document,activity,
								ar, parser, this);
					// Pretend pnCH was already set when we started this element
					elPath.pop();	// pnCH will close this element out.
					pnCH.startElement(namespaceURI, localName, qName, attrList);
					parser.setContentHandler(pnCH);
				}
			}
		}
	}
	
	
	public void endElement(String namespaceURI, String localName, String qName) {
		// Look for the teiHeader/fileDesc/titleStmt/title path
		if(onAltIDElement) {
			String altID = getCurrentText().trim().replaceAll("[\\s]+", " ");
			//System.err.println("Found AltID for document:"+altID);
			document.setAlt_id(altID);
			onAltIDElement = false;
			foundAltIDElement = true;
		} else if(onPrimPubElement) {
			// Preserve spaces in primary pub. Only used in display
			String primPub = getCurrentText().trim();
			//System.err.println("Found Primary Publication for document:"+primPub);
			document.setPrimaryPubl(primPub);
			onPrimPubElement = false;
			foundPrimPubElement = true;
		}
		super.endElement(namespaceURI, localName, qName);
	}
	
	@Override
	public void pop() {
		if(!notADocument) {
			// Ensure we have an id before we try to add to the caches, etc.
			document.persist(dbConn, CachedEntity.DEEP_PERSIST);
			corpus.addDocument(document);
			if(!foundAltIDElement)
				System.err.println("No AltIDElement for document!");
			if(!foundPrimPubElement)
				System.err.println("No Primary Publication Element for document!");
		}
		super.pop();
	}
	
	protected void generateParseError(String xmlid, String error ) {
		throw new RuntimeException("Parse Error near element: "
				+((xmlid!=null)?xmlid:"(unknown)")
				+" in document: "+document.getAlt_id()
				+": "+error);
	}
	

}
