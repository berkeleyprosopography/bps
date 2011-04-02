package edu.berkeley.bps.services.corpus.sax;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;

import edu.berkeley.bps.services.common.LinkType;
import edu.berkeley.bps.services.common.sax.StackedContentHandler;
import edu.berkeley.bps.services.corpus.Activity;
import edu.berkeley.bps.services.corpus.ActivityRole;
import edu.berkeley.bps.services.corpus.Corpus;
import edu.berkeley.bps.services.corpus.Document;
import edu.berkeley.bps.services.corpus.Name;
import edu.berkeley.bps.services.corpus.NameFamilyLink;
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
	// TODO rewrite to hold nrads for base, patronym, ancestors, clan, etc. 
	// then can build up the links 
	// Note that when we add link for grandfather to the base, we must
	// also link that as father to the father 
	// Note that when we add links for ancestors to the base, we must
	// add them to all father, grandfather, etc. 
	// Note that when we add link for clan to the base, we must
	// also link that as clan to the father, grandfather, etc.  

	protected static final int T_MISSING = 0x0; 
	protected static final int T_MASCULINE = 0x1; 
	protected static final int T_FEMININE = 0x2; 
	protected static final int T_UNMARKED = 0x4;
	protected static final int T_PRIMARY = (T_MASCULINE|T_FEMININE|T_UNMARKED); 
	protected static final int T_PATRONYM = 0x8;
	protected static final int T_GRANDFATHER = T_PATRONYM|0x10;
	protected static final int T_ANCESTOR = T_PATRONYM|0x20;
	protected static final int T_MALE = (T_MASCULINE|T_PATRONYM); 
	protected static final int T_FEMALE = (T_FEMININE); 

	protected ActivityRole fatherAR;
	protected ActivityRole grandfatherAR;
	protected ActivityRole ancestorAR;
	protected ActivityRole clanAR;
	
	protected NameRoleActivity baseForename, father, grandfather, clan;
	protected List<NameRoleActivity> ancestors;

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
		ancestorAR = corpus.findOrCreateActivityRole(ActivityRole.ANCESTOR_ROLE, dbConn);
		clanAR = corpus.findOrCreateActivityRole(ActivityRole.CLAN_ROLE, dbConn);
		baseForename = null;
		father = null;
		grandfather = null;
		clan = null;
		ancestors = new ArrayList<NameRoleActivity>();
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
	
	private static boolean isPrimary(int type) {
		return (type&T_PRIMARY)!=0;
	}
	
	private static boolean isPrimaryOrMissing(int type) {
		return (type&T_PRIMARY)!=0 || type==T_MISSING;
	}
	
	private static boolean isMale(int type) {
		return (type&T_MALE)!=0;
	}
	
	private static boolean isFemale(int type) {
		return (type&T_FEMALE)!=0;
	}
	
	protected void handleForename(Attributes attrList) {
		String xmlid = attrList.getValue("xml:id");
		try {
			String nameAttr = attrList.getValue("", "n");
			Name name = null;
			String typeStr = attrList.getValue("", "type");
			String subTypeStr = attrList.getValue("", "subtype");
			int type = 0;
			if("masculine".equalsIgnoreCase(typeStr)) {
				type = T_MASCULINE;
			} else if("feminine".equalsIgnoreCase(typeStr)) {
				type = T_FEMININE;
			} else if("patronymic".equalsIgnoreCase(typeStr)) {
				if("grandfather".equalsIgnoreCase(subTypeStr)) {
					type = (grandfather==null)?T_GRANDFATHER:T_ANCESTOR;
				} else { 
					type = T_PATRONYM;
				}
			} else if("unmarked".equalsIgnoreCase(typeStr)) {
				type = T_UNMARKED;
			} else if(typeStr==null) {
				type = T_MISSING;
			} else {
				generateParseWarning(xmlid, "Ignoring unknown forname type in person declaration: "+typeStr);
				type = T_MISSING;
			}
			int gender = isMale(type)?Name.GENDER_MALE:
								(isFemale(type)?Name.GENDER_FEMALE:
									Name.GENDER_UNKNOWN);
			if(nameAttr!=null) {
				String cleanNameStr = nameAttr.replaceAll("\\[.*\\]$", "");
				name = corpus.findOrCreateName(cleanNameStr, 
								Name.NAME_TYPE_PERSON, gender, dbConn);
				name.addCitation(document.getId());
			}
			if(isPrimaryOrMissing(type)) {
				NameRoleActivity nra = 
					new NameRoleActivity(name, activityRole, activity, xmlid, document);
				if(state!=S_INIT) {
					generateParseWarning(xmlid, 
							"Found multiple primary names in person declaration");
				} else if(baseForename!=null){
					generateParseWarning(xmlid, 
					"Internal error - state is INIT but baseForename not null!");
				} else {
					baseForename = nra;
				}
	        	document.addNameRoleActivity(nra);
	        	state = S_FOUND_FORENAME;
			} else { // Handle all patronyms
				if(state==S_INIT) {		// patronym w/o forename
					// Add the missing primary nrad
		        	NameRoleActivity nra = 
		        		new NameRoleActivity(null, activityRole, activity, 
		        				null, document, "(synthesized)");
					if(baseForename!=null){
						generateParseWarning(xmlid, 
						"Internal error - state is INIT but baseForename not null!");
					} else {
						baseForename = nra;
					}
		        	document.addNameRoleActivity(nra);
		        	// Now add a father role
		        	father = new NameRoleActivity(name, fatherAR, activity, 
		        									xmlid, document);
		        	document.addNameRoleActivity(father);
		        	state = S_FOUND_PATRONYM;
				} else if(state==S_FOUND_FORENAME) { // typical case
					if(type!=T_PATRONYM) {
						generateParseWarning(xmlid, 
						"Assuming name \""+name.getName()+
							"\" is patronym although type is:"+type);
					}
		        	// Add a father role
		        	father = new NameRoleActivity(name, fatherAR, activity, 
		        									xmlid, document);
		        	document.addNameRoleActivity(father);
		        	state = S_FOUND_PATRONYM;
				} else if(state==S_FOUND_PATRONYM) { // extra patronym
					if(type==T_GRANDFATHER) {
			        	// Add a grandfather role
			        	grandfather = 
			        		new NameRoleActivity(name, grandfatherAR, activity, 
			        								xmlid, document);
			        	document.addNameRoleActivity(grandfather);
					} else {
						NameRoleActivity ancestor =
							new NameRoleActivity(name, ancestorAR, activity, 
													xmlid, document);
						ancestors.add(ancestor);
			        	document.addNameRoleActivity(ancestor);
			        	if(type!=T_ANCESTOR)
			        		generateParseWarning(xmlid, 
								"Too many patronyms (not marked grandfather). Treating as ANCESTOR:\""
								+name.getName()+"\"");
					}
				} else {	// found clan - out of order
					generateParseWarning(xmlid, "Found names after clan in person declaration");
				}
			}
		} catch(RuntimeException re) {
			generateParseWarning(xmlid, re.getLocalizedMessage());
			throw re;
		}
	}
	
	protected void handleAddname(Attributes attrList) {
		String xmlid = attrList.getValue("xml:id");
		try {
			String nameAttr = attrList.getValue("", "n");
			Name name = null;
			if(nameAttr!=null) {
				String clanNameStr = nameAttr.replaceAll("\\[.*\\]$", "");
				name = corpus.findOrCreateName(clanNameStr, Name.NAME_TYPE_CLAN,
												Name.GENDER_UNKNOWN, dbConn);
				name.addCitation(document.getId());
			}
			String typeStr = attrList.getValue("", "type");
			if(!("clan".equalsIgnoreCase(typeStr))) {
				generateParseWarning(xmlid, "Unknown addname type in person declaration");
				return;
			}
			if(state==S_INIT) {		// clan w/o forename?
	        	NameRoleActivity nra = 
	        		new NameRoleActivity(null, activityRole, activity, 
	        				null, document, "(synthesized)");
				if(baseForename!=null){
					generateParseWarning(xmlid, 
					"Internal error - state is INIT but baseForename not null!");
				} else {
					baseForename = nra;
				}
	        	document.addNameRoleActivity(nra);
			} else if((state==S_FOUND_FORENAME)
					|| (state==S_FOUND_PATRONYM)) { // normal case - ignore
			} else if(state==S_FOUND_CLAN) {
				generateParseWarning(xmlid, "Found multiple clan names in person declaration");
				return;
			}
			// TODO add clan name family links to all proceeding names
			if(clan!=null){
				generateParseWarning(xmlid, 
						"Internal error - state is "+state
						+" (not FOUND_CLAN) but clan not null!");
			} else {
				clan = new NameRoleActivity(name, clanAR, activity, 
	    				xmlid, document, null);
	        	document.addNameRoleActivity(clan);
			}
	    	state = S_FOUND_CLAN;
		} catch(RuntimeException re) {
			generateParseWarning(xmlid, re.getLocalizedMessage());
			throw re;
		}
	}
	
	private void addNameFamilyLinks() {
		if(baseForename!=null) {	// Add to found forenames
			if(clan!=null) {
				baseForename.addNameFamilyLink(clan, 
						LinkType.Type.LINK_TO_CLAN);
			}
			if(father!=null) {
				baseForename.addNameFamilyLink(father, 
							LinkType.Type.LINK_TO_FATHER);
				if(clan!=null) {
					father.addNameFamilyLink(clan, 
							LinkType.Type.LINK_TO_CLAN);
				}
				if(grandfather!=null) {
					baseForename.addNameFamilyLink(grandfather, 
							LinkType.Type.LINK_TO_GRANDFATHER);
					father.addNameFamilyLink(grandfather, 
							LinkType.Type.LINK_TO_FATHER);
					if(clan!=null) {
						grandfather.addNameFamilyLink(clan, 
								LinkType.Type.LINK_TO_CLAN);
					}
					// Handle up to 2 ancestors for now - later
					// rewrite this to be all one array and handle it 
					// cleanly
					int nAncestors = ancestors.size();
					if(nAncestors>=1) {
						NameRoleActivity anc1 = ancestors.get(0);
						baseForename.addNameFamilyLink(anc1, 
								LinkType.Type.LINK_TO_ANCESTOR);
						father.addNameFamilyLink(anc1, 
								LinkType.Type.LINK_TO_ANCESTOR);
						grandfather.addNameFamilyLink(anc1, 
								LinkType.Type.LINK_TO_FATHER);
						if(clan!=null) {
							anc1.addNameFamilyLink(clan, 
									LinkType.Type.LINK_TO_CLAN);
						}
						if(nAncestors>=2) {
							NameRoleActivity anc2 = ancestors.get(1);
							baseForename.addNameFamilyLink(anc2, 
									LinkType.Type.LINK_TO_ANCESTOR);
							father.addNameFamilyLink(anc2, 
									LinkType.Type.LINK_TO_ANCESTOR);
							grandfather.addNameFamilyLink(anc2, 
									LinkType.Type.LINK_TO_GRANDFATHER);
							if(clan!=null) {
								anc2.addNameFamilyLink(clan, 
										LinkType.Type.LINK_TO_CLAN);
							}
							if(nAncestors>=3) {
								generateParseWarning(baseForename.getXmlID(), 
									"persName has more than 2 ancestors declared - ignoring some");
							}
						}
					}
				}
			}
		}
	}
	
	// All done with the persName element, so finish up
	// Need to build the NameFamilyLinks among the various nrads 
	protected void pop() {
		addNameFamilyLinks();
		super.pop();
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
