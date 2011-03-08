package edu.berkeley.bps.services.corpus.sax;

import java.sql.Connection;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;

import edu.berkeley.bps.services.common.sax.StackedContentHandler;
import edu.berkeley.bps.services.corpus.Activity;
import edu.berkeley.bps.services.corpus.ActivityRole;
import edu.berkeley.bps.services.corpus.Corpus;
import edu.berkeley.bps.services.corpus.Document;
import edu.berkeley.bps.services.corpus.Name;
import edu.berkeley.bps.services.corpus.NameRoleActivity;
import edu.berkeley.bps.services.corpus.TEI_Constants;

public class PersonNameContentHandler extends StackedContentHandler {
	
	protected Connection dbConn;
	protected Corpus corpus;
	protected Document document;
	protected Activity activity;
	protected ActivityRole activityRole;
	protected static final int S_INIT = 0; 
	protected static final int S_FOUND_FORENAME = 1; 
	protected static final int S_FOUND_PATRONYM = 2; 
	protected static final int S_FOUND_CLAN = 3; 
	protected int state = S_INIT;

	protected static final int T_MISSING = 0x0; 
	protected static final int T_MASCULINE = 0x1; 
	protected static final int T_FEMININE = 0x2; 
	protected static final int T_UNMARKED = 0x4;
	protected static final int T_PRIMARY = (T_MASCULINE|T_FEMININE|T_UNMARKED); 
	protected static final int T_PATRONYM = 0x8;
	protected static final int T_MALE = (T_MASCULINE|T_PATRONYM); 
	protected static final int T_FEMALE = (T_FEMININE); 

	protected ActivityRole fatherAR;
	protected ActivityRole grandfatherAR;
	protected ArrayList<Name> names;

	public PersonNameContentHandler(Connection dbConn, Corpus corpus, Document document,
			Activity activity, ActivityRole actRole, 
			XMLReader parser, ContentHandler previous) {
		super(parser, previous);
		this.dbConn = dbConn;
		this.corpus = corpus;
		this.document = document;
		this.activity = activity;
		this.activityRole = actRole;
		fatherAR = corpus.findOrCreateActivityRole(ActivityRole.FATHER_ROLE, dbConn);
		grandfatherAR = corpus.findOrCreateActivityRole(ActivityRole.GRANDFATHER_ROLE, dbConn);
		names = new ArrayList<Name>();
	}

	public void startElement(String namespaceURI, String localName, String qName, 
			Attributes attrList) {
		super.startElement(namespaceURI, localName, qName, attrList);
		if(localName.equals("forename")) {
			handleForename(attrList);
		} else if(localName.equals("addName")) {
			handleAddname(attrList);
		} else if(localName.equals("persName") && (elPath.size()>1)) {
			String xmlid = attrList.getValue("xml:id");
			generateParseWarning(xmlid, "Illegal persName element within persName!");
		} 
	}
	
	protected void handleForename(Attributes attrList) {
		NameRoleActivity nra = null;
		String xmlid = attrList.getValue("xml:id");
		String name = attrList.getValue("", "n");
		String fnNameStr = null; 
		boolean isGrandfather = false;
		Name fnName = null;
		if(name!=null) {
			fnNameStr = name.replaceAll("\\[.*\\]$", "");
			fnName = corpus.findOrCreateName(fnNameStr, dbConn);
		}
		String typeStr = attrList.getValue("", "type");
		String subTypeStr = attrList.getValue("", "subtype");
		int type = 0;
		if("masculine".equalsIgnoreCase(typeStr)) {
			type = T_MASCULINE;
		} else if("feminine".equalsIgnoreCase(typeStr)) {
			type = T_FEMININE;
		} else if("patronymic".equalsIgnoreCase(typeStr)) {
			type = T_PATRONYM;
			isGrandfather = "grandfather".equalsIgnoreCase(subTypeStr);
		} else if("unmarked".equalsIgnoreCase(typeStr)) {
			type = T_UNMARKED;
		} else if(typeStr==null) {
			type = T_MISSING;
		} else {
			generateParseWarning(xmlid, "Ignoring unknown forname type in person declaration: "+typeStr);
			type = T_MISSING;
		}
		if(fnName!=null) {
			if((type&T_MALE)!=0)
				fnName.setGender(Name.GENDER_MALE);
			else if((type&T_FEMALE)!=0)
				fnName.setGender(Name.GENDER_FEMALE);
			else
				fnName.setGender(Name.GENDER_UNKNOWN);
			names.add(fnName);
			// TODO add name family links to all proceeding names
		}
		if((type&T_PRIMARY)!=0) {
			if(state!=S_INIT) {
				generateParseWarning(xmlid, 
						"Found multiple primary names in person declaration");
			}
        	nra = new NameRoleActivity(fnName, activityRole, activity, null, document);
        	document.addNameRoleActivity(nra);
        	state = S_FOUND_FORENAME;
		} else { // Handle patronyms
			if(state==S_INIT) {		// patronym w/o forename
				// Add the missing primary nrad
	        	nra = new NameRoleActivity(null, activityRole, activity, null, document);
	        	document.addNameRoleActivity(nra);
	        	// Now add a father role
	        	nra = new NameRoleActivity(fnName, fatherAR, activity, null, document);
	        	document.addNameRoleActivity(nra);
	        	state = S_FOUND_PATRONYM;
			} else if(state==S_FOUND_FORENAME) { // typical case
				if(type!=T_PATRONYM) {
					generateParseWarning(xmlid, 
					"Assuming name \""+fnName.getName()+
						"\" is patronym although type is:"+type);
				}
	        	// Add a father role
	        	nra = new NameRoleActivity(fnName, fatherAR, activity, null, document);
	        	document.addNameRoleActivity(nra);
	        	state = S_FOUND_PATRONYM;
			} else if(state==S_FOUND_PATRONYM) { // extra patronym
				if(isGrandfather) {
		        	// Add a grandfather role
		        	nra = new NameRoleActivity(fnName, grandfatherAR, activity, null, document);
		        	document.addNameRoleActivity(nra);
				} else {
					generateParseWarning(xmlid, 
							"Too many patronyms (not marked grandfather). Ignoring:\""
							+fnName.getName()+"\"");
				}
			} else {	// found clan - out of order
				generateParseWarning(xmlid, "Found names after clan in person declaration");
			}
		}
	}
	
	protected void handleAddname(Attributes attrList) {
		String xmlid = attrList.getValue("xml", "id");
		String name = attrList.getValue("", "n");
		String fnNameStr = null; 
		Name fnName = null;
		if(name!=null) {
			fnNameStr = name.replaceAll("\\[.*\\]$", "");
			fnName = corpus.findOrCreateName(fnNameStr, dbConn);
		}
		String typeStr = attrList.getValue("", "type");
		if(!("clan".equalsIgnoreCase(typeStr))) {
			generateParseWarning(xmlid, "Unknown addname type in person declaration");
			return;
		}
		if(state==S_INIT) {		// clan w/o forename?
			// Add the missing primary nrad
			NameRoleActivity nra = new NameRoleActivity(null, activityRole, activity, null, document);
        	document.addNameRoleActivity(nra);
		} else if((state==S_FOUND_FORENAME)
				|| (state==S_FOUND_PATRONYM)) { // normal case - ignore
		} else if(state==S_FOUND_CLAN) {
			generateParseWarning(xmlid, "Found multiple clan names in person declaration");
			return;
		}
    	state = S_FOUND_CLAN;
		if(fnName!=null) {
			names.add(fnName);
			// TODO add clan name family links to all proceeding names
		}
	}
	
	protected void generateParseError(String xmlid, String error ) {
		throw new RuntimeException("Parse Error near element: "
				+((xmlid!=null)?xmlid:"(unknown)")
				+" in document: "+document.getAlt_id()
				+": "+error);
	}
	
	protected void generateParseWarning(String xmlid, String warning ) {
		System.err.println("Warning near element: "
				+((xmlid!=null)?xmlid:"(unknown)")
				+" in document: "+document.getAlt_id()
				+": "+warning);
	}
	
}
