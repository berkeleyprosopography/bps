package edu.berkeley.bps.services.corpus.sax;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;

import edu.berkeley.bps.services.common.LinkType;
import edu.berkeley.bps.services.common.hbtin.HBTIN_Constants;
import edu.berkeley.bps.services.common.sax.StackedContentHandler;
import edu.berkeley.bps.services.corpus.Activity;
import edu.berkeley.bps.services.corpus.ActivityRole;
import edu.berkeley.bps.services.corpus.Corpus;
import edu.berkeley.bps.services.corpus.Document;
import edu.berkeley.bps.services.corpus.Name;
import edu.berkeley.bps.services.corpus.NameFamilyLink;
import edu.berkeley.bps.services.corpus.NameRoleActivity;
import edu.berkeley.bps.services.corpus.TEI_Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersonNameContentHandler extends StackedContentHandler {
	final Logger logger = LoggerFactory.getLogger(PersonNameContentHandler.class);
	
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
	protected static final int T_CLEAR_GENDER_MASK = ~(T_MASCULINE|T_FEMININE|T_UNMARKED); 
	protected static final int T_PATRONYM = 0x8;
	protected static final int T_GRANDFATHER = T_PATRONYM|0x10;
	protected static final int T_ANCESTOR = T_PATRONYM|0x20;
	protected static final int T_IS_MALE = (T_MASCULINE|T_PATRONYM); 
	protected static final int T_IS_FEMALE = (T_FEMININE); 
	
	// TODO this should be configured or set in UI.
	protected static final String CDLI_MISSING_NAME_MARKER = "NENNI";

	protected static final String MISSING_NAME = "(Missing Name)";

	protected ActivityRole fatherAR;
	protected ActivityRole grandfatherAR;
	protected ActivityRole ancestorAR;
	protected ActivityRole clanAR;
	
	protected Name forename = null;
	protected int forenameType = T_MISSING;
	protected String forenameStateType = null;
	protected String forenameXMLId = null;
	
	protected Name addname = null;
	protected String addnameXMLId = null;

	protected NameRoleActivity baseForenameNRAD, father, grandfather, clan;
	protected List<NameRoleActivity> ancestors;

	public PersonNameContentHandler(Connection dbConn, Corpus corpus, Document document,
			Activity activity, ActivityRole defaultActRole, 
			XMLReader parser, ContentHandler previous) {
		super(parser, previous);
		this.dbConn = dbConn;
		this.corpus = corpus;
		this.document = document;
		this.activity = activity;
		this.activityRole = defaultActRole;
		fatherAR = corpus.findOrCreateActivityRole(ActivityRole.FATHER_ROLE, dbConn);
		grandfatherAR = corpus.findOrCreateActivityRole(ActivityRole.GRANDFATHER_ROLE, dbConn);
		ancestorAR = corpus.findOrCreateActivityRole(ActivityRole.ANCESTOR_ROLE, dbConn);
		clanAR = corpus.findOrCreateActivityRole(ActivityRole.CLAN_ROLE, dbConn);
		baseForenameNRAD = null;
		father = null;
		grandfather = null;
		clan = null;
		ancestors = new ArrayList<NameRoleActivity>();
	}

	public void startElement(String namespaceURI, String localName, String qName, 
			Attributes attrList) {
		super.startElement(namespaceURI, localName, qName, attrList);
		if(localName.equals(TEI_Constants.PERSNAME_EL)) {
			if( elPath.size()>1 ) {
				String xmlid = attrList.getValue(TEI_Constants.XMLID_ATTR);
				generateParseWarning(xmlid, "Illegal persName element within persName!");
			} else {
				handlePersname(attrList);
			}
		} else if(localName.equals(TEI_Constants.FORENAME_EL)) {
			handleForename(attrList);
		} else if(localName.equals(TEI_Constants.ADDNAME_EL)) {
			handleAddname(attrList);
		} else if(localName.equals(TEI_Constants.STATE_EL)) {	// Handle gender, features metadata
			if( TEI_Constants.FORENAME_EL.equals(elPath.peek(2))){	// but only on forenames
				handleForenameStateStartEl(attrList);
			}
		}
	}
	
	public void endElement(String namespaceURI, String localName, String qName) {
		// final String myName = ".endElement: ";
		try {
			// Handle forenames where name can be in attrs or text (text only available on endEl)
			if(localName.equals(TEI_Constants.FORENAME_EL)) {
				handleForenameEndEl();
			} else if(localName.equals(TEI_Constants.STATE_EL)) {			// Handle features metadata
				// stack top is this element, so have to look up two to see parent
				if( TEI_Constants.FORENAME_EL.equals(elPath.peek(2))){		// but only on forenames
					handleForenameStateEndEl(forenameStateType);
				}
			} else if(localName.equals(TEI_Constants.ADDNAME_EL)) {			// Handle clan names
				handleAddnameEndEl();
			} 
			super.endElement(namespaceURI, localName, qName);
		} catch(RuntimeException re) {
			generateParseWarning(forenameXMLId, re.getLocalizedMessage());
			throw re;
		}
	}
	
	public void handleForenameEndEl() {
		int forenameGender = isMale(forenameType)?Name.GENDER_MALE:
			(isFemale(forenameType)?Name.GENDER_FEMALE:
				Name.GENDER_UNKNOWN);
		if(forename==null) {
			String cleanNameStr = getCurrentText().trim().replaceAll("\\[.*\\]$", "");
			// Do not include missing name markers in the names 
			if(cleanNameStr!=null && !cleanNameStr.isEmpty()
				&& !CDLI_MISSING_NAME_MARKER.equalsIgnoreCase(cleanNameStr)) {
				forename = corpus.findOrCreateName(cleanNameStr, null,
								Name.NAME_TYPE_PERSON, forenameGender, dbConn);
			} else {
				// Ensures we have a name even if it is missing in corpus
				forename = corpus.findOrCreateName(MISSING_NAME, null,
						Name.NAME_TYPE_PERSON, Name.GENDER_UNKNOWN, dbConn);
			}
		} else {
			// This may be a no-op if gender was marked on the forename, 
			// but it may also come in sub-element and so get set here.
			forename.checkAndUpdateGender(forenameGender);
		}
		if(forename!=null) {
			forename.addCitation(document.getId());
		}
		if(isPrimaryOrMissing(forenameType)) {
			NameRoleActivity nra = 
				new NameRoleActivity(forename, activityRole, activity, forenameXMLId, document);
			if(state!=S_INIT) { 
				generateParseWarning(forenameXMLId, 
						"Found multiple primary names in person declaration");
			} else if(baseForenameNRAD!=null){
				generateParseWarning(forenameXMLId, 
				"Internal error - state is INIT but baseForename not null!");
			} else {
				baseForenameNRAD = nra;
			}
        	document.addNameRoleActivity(nra);
        	state = S_FOUND_FORENAME;
		} else { // Handle all patronyms
			// Note that this needs to handle matronyms as well. 
			if(state==S_INIT) {		// patronym w/o forename
				// Add the missing primary nrad
	        	NameRoleActivity nra = 
	        		new NameRoleActivity(null, activityRole, activity, 
	        				null, document, "(synthesized)");
				if(baseForenameNRAD!=null){
					generateParseWarning(forenameXMLId, 
					"Internal error - state is INIT but baseForename not null!");
				} else {
					baseForenameNRAD = nra;
				}
	        	document.addNameRoleActivity(nra);
	        	// Now add a father role
	        	father = new NameRoleActivity(forename, fatherAR, activity, 
	        			forenameXMLId, document);
	        	document.addNameRoleActivity(father);
	        	state = S_FOUND_PATRONYM;
			} else if(state==S_FOUND_FORENAME) { // typical case
				if(forenameType!=T_PATRONYM) {
					generateParseWarning(forenameXMLId, 
					"Assuming name \""+forename.getName()+
						"\" is patronym although type is:"+forenameType);
				}
	        	// Add a father role
	        	father = new NameRoleActivity(forename, fatherAR, activity, 
	        			forenameXMLId, document);
	        	document.addNameRoleActivity(father);
	        	state = S_FOUND_PATRONYM;
			} else if(state==S_FOUND_PATRONYM) { // extra patronym
				if(forenameType==T_GRANDFATHER) {
		        	// Add a grandfather role
		        	grandfather = 
		        		new NameRoleActivity(forename, grandfatherAR, activity, 
		        				forenameXMLId, document);
		        	document.addNameRoleActivity(grandfather);
				} else {
					NameRoleActivity ancestor =
						new NameRoleActivity(forename, ancestorAR, activity, 
								forenameXMLId, document);
					ancestors.add(ancestor);
		        	document.addNameRoleActivity(ancestor);
		        	if(forenameType!=T_ANCESTOR)
		        		generateParseWarning(forenameXMLId, 
							"Too many patronyms (not marked grandfather). Treating as ANCESTOR:\""
							+forename.getName()+"\"");
				}
			} else {	// found clan - out of order
				generateParseWarning(forenameXMLId, "Found names after clan in person declaration");
			}
		}

	}

	protected void handleForenameStateEndEl(String typeStr ) {
		// Note that while we may get a gender here, we may already
		// have gotten other declarations (like patronymic) that we need to preserve
		if(TEI_Constants.GENDER_TYPE_VAL.equalsIgnoreCase(typeStr)) {
			String genderString = getCurrentText().trim();
			if(TEI_Constants.TYPE_GENDER_MALE.equalsIgnoreCase(genderString)
				|| TEI_Constants.TYPE_GENDER_MASCULINE.equalsIgnoreCase(genderString)) {
				// Okay if unknown - just can't declare feminine in attrs and Male in state element
				if(isFemale(forenameType)) {
					generateParseWarning(forenameXMLId, "Conflicting gender declarations");
				} else if(!isMale(forenameType)) {
					// If already male (e.g., patronym, leave as is; otherwise, fix
					forenameType = (forenameType & T_CLEAR_GENDER_MASK)| T_MASCULINE;
				}
			} else if(TEI_Constants.TYPE_GENDER_FEMALE.equalsIgnoreCase(typeStr)
					|| TEI_Constants.TYPE_GENDER_FEMININE.equalsIgnoreCase(genderString)) {
				// Okay if unknown - just can't declare masculine in attrs and Feale in state element
				if(isMale(forenameType)) {
					generateParseWarning(forenameXMLId, "Conflicting gender declarations");
				} else if(!isFemale(forenameType)) {
					// If already female (e.g., matronym, leave as is; otherwise, set
					forenameType = (forenameType & T_CLEAR_GENDER_MASK)| T_FEMININE;
				}
			} else {
				generateParseWarning(forenameXMLId, "Ignoring unknown gender type in state declaration: "+genderString);
				// forenameType = T_MISSING;  Leave forenameType as set already
			}
		/* Could warn on unknown <state> types but will change this to handle general features later.
		} else {
			generateParseWarning(xmlid, "Ignoring unknown <state @type> in person declaration: "+typeStr);
		*/
		}

		
	}
	
	private static boolean isPrimary(int type) {
		return (type&T_PRIMARY)!=0;
	}
	
	private static boolean isPrimaryOrMissing(int type) {
		return (type&T_PRIMARY)!=0 || type==T_MISSING;
	}
	
	private static boolean isMale(int type) {
		return (type&T_IS_MALE)!=0;
	}
	
	private static boolean isFemale(int type) {
		return (type&T_IS_FEMALE)!=0;
	}
	
	protected void handlePersname(Attributes attrList) {
		String xmlid = attrList.getValue(TEI_Constants.XMLID_ATTR);
		try {
			String roleStr = attrList.getValue("", TEI_Constants.ROLE_ATTR);
			if(roleStr != null && !roleStr.isEmpty()) {
				this.activityRole = corpus.findOrCreateActivityRole(roleStr, dbConn);
			}
		} catch(RuntimeException re) {
			generateParseWarning(xmlid, re.getLocalizedMessage());
			throw re;
		}
	}
	
	protected void handleForename(Attributes attrList) {
		forenameXMLId = attrList.getValue(TEI_Constants.XMLID_ATTR);
		try {
			String nameAttr = attrList.getValue("", TEI_Constants.N_ATTR);
			String nymRefAttr = attrList.getValue("", TEI_Constants.NYMREF_ATTR);
			String typeStr = attrList.getValue("", TEI_Constants.TYPE_ATTR);
			String subTypeStr = attrList.getValue("", TEI_Constants.SUBTYPE_ATTR);
			forenameType = T_MISSING;
			if("masculine".equalsIgnoreCase(typeStr)) {
				forenameType = T_MASCULINE;
			} else if("feminine".equalsIgnoreCase(typeStr)) {
				forenameType = T_FEMININE;
			} else if("patronymic".equalsIgnoreCase(typeStr)) {
				if("grandfather".equalsIgnoreCase(subTypeStr)) {
					forenameType = (grandfather==null)?T_GRANDFATHER:T_ANCESTOR;
				} else { 
					forenameType = T_PATRONYM;
				}
			} else if("unmarked".equalsIgnoreCase(typeStr)) {
				forenameType = T_UNMARKED;
			} else if(typeStr==null) {
				// forenameType = T_MISSING; Set above
			} else {
				generateParseWarning(forenameXMLId, "Ignoring unknown forname type in person declaration: "+forenameType);
				// forenameType = T_MISSING; Set above
			}
			forename=null;	// Clear state from handling previous forenames (e.g., before patronym)
			
			if(nymRefAttr!=null) {
				nymRefAttr = nymRefAttr.trim();
				nymRefAttr = nymRefAttr.replaceAll("#", "");
				if(nymRefAttr.isEmpty()) {
					nymRefAttr = null;
				} else {
					forename = corpus.findNym(nymRefAttr);
				}
			}
			// state = S_FOUND_FORENAME; // We saw the element, even if we may have errors pending
			/*  This happens in the endElement handler now
			if(forename==null) {
				String cleanNameStr;
				if(nameAttr!=null) {
					cleanNameStr = nameAttr.replaceAll("\\[.*\\]$", "");
				}
				// Do not include missing name markers in the names 
				if(!MISSING_NAME_MARKER.equalsIgnoreCase(cleanNameStr)) {
					name = corpus.findOrCreateName(cleanNameStr, nymRefAttr,
									Name.NAME_TYPE_PERSON, gender, dbConn);
					name.addCitation(document.getId());
				}
			}
			*/
			/*
			if(isPrimaryOrMissing(type)) {
				NameRoleActivity nra = 
					new NameRoleActivity(name, activityRole, activity, xmlid, document);
				if(state!=S_INIT) {
					generateParseWarning(xmlid, 
							"Found multiple primary names in person declaration");
				} else if(baseForenameNRAD!=null){
					generateParseWarning(xmlid, 
					"Internal error - state is INIT but baseForename not null!");
				} else {
					baseForenameNRAD = nra;
				}
	        	document.addNameRoleActivity(nra);
	        	state = S_FOUND_FORENAME;
			} else { // Handle all patronyms
				if(state==S_INIT) {		// patronym w/o forename
					// Add the missing primary nrad
		        	NameRoleActivity nra = 
		        		new NameRoleActivity(null, activityRole, activity, 
		        				null, document, "(synthesized)");
					if(baseForenameNRAD!=null){
						generateParseWarning(xmlid, 
						"Internal error - state is INIT but baseForename not null!");
					} else {
						baseForenameNRAD = nra;
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
				*/
		} catch(RuntimeException re) {
			generateParseWarning(forenameXMLId, re.getLocalizedMessage());
			throw re;
		}
	}
	
	protected void handleForenameStateStartEl(Attributes attrList) {
		String xmlid = attrList.getValue(TEI_Constants.XMLID_ATTR);
		try {
			forenameStateType = attrList.getValue("", TEI_Constants.TYPE_ATTR);
		} catch(RuntimeException re) {
			generateParseWarning(xmlid, re.getLocalizedMessage());
			throw re;
		}
	}
	
	protected void handleAddname(Attributes attrList) {
		addnameXMLId = attrList.getValue(TEI_Constants.XMLID_ATTR);
		try {
			String nameAttr = attrList.getValue("", TEI_Constants.N_ATTR);
			String nymRefAttr = attrList.getValue("", TEI_Constants.NYMREF_ATTR);
			addname = null;
			if(nymRefAttr!=null) {
				nymRefAttr = nymRefAttr.trim();
				nymRefAttr = nymRefAttr.replaceAll("#", "");
				if(nymRefAttr.isEmpty()) {
					nymRefAttr = null;
				} else {
					addname = corpus.findNym(nymRefAttr);
					if(addname!=null) {
						// Since this is a clan name, we do not bother with gender checks.
						addname.addCitation(document.getId());
					}
				}
			}
			if(addname==null && nameAttr!=null) {
				String clanNameStr = nameAttr.replaceAll("\\[.*\\]$", "");
				addname = corpus.findOrCreateName(clanNameStr, nymRefAttr,
						Name.NAME_TYPE_CLAN, Name.GENDER_UNKNOWN, dbConn);
			}
			
			String typeStr = attrList.getValue("", TEI_Constants.TYPE_ATTR);
			if(!("clan".equalsIgnoreCase(typeStr))) {
				generateParseWarning(addnameXMLId, "Unknown addname type in person declaration");
				return;
			}
		} catch(RuntimeException re) {
			generateParseWarning(addnameXMLId, re.getLocalizedMessage());
			throw re;
		}
	}
	
	/**
	 * AddName elements can have the name in the body, so we may have to gather it here. 
	 * In any case, we do the bulk of the work here once we have the name.
	 */
	protected void handleAddnameEndEl() {
		try {
			if(addname==null) {
				String cleanNameStr = getCurrentText().trim().replaceAll("\\[.*\\]$", "");
				// Do not include missing name markers in the names 
				if(cleanNameStr!=null && !cleanNameStr.isEmpty()
					&& !CDLI_MISSING_NAME_MARKER.equalsIgnoreCase(cleanNameStr)) {
					addname = corpus.findOrCreateName(cleanNameStr, null,
									Name.NAME_TYPE_CLAN, Name.GENDER_UNKNOWN, dbConn);
				} else {
					// Ensures we have a name even if it is missing in corpus
					addname = corpus.findOrCreateName(MISSING_NAME, null,
							Name.NAME_TYPE_CLAN, Name.GENDER_UNKNOWN, dbConn);
				}
			}
			if(addname!=null) {
				addname.addCitation(document.getId());
			}

			if(state==S_INIT) {		// clan w/o forename?
	        	NameRoleActivity nra = 
	        			// Should we use the ID of the addname element?
	        		new NameRoleActivity(null, activityRole, activity, 
	        				null, document, "(synthesized)");
				if(baseForenameNRAD!=null){
					generateParseWarning(addnameXMLId, 
					"Internal error - state is INIT but baseForename not null!");
				} else {
					baseForenameNRAD = nra;
				}
	        	document.addNameRoleActivity(nra);
			} else if((state==S_FOUND_FORENAME)
					|| (state==S_FOUND_PATRONYM)) { // normal case - ignore
			} else if(state==S_FOUND_CLAN) {
				generateParseWarning(addnameXMLId, "Found additional clan name: ["+addname.getName()
											+"] in person declaration");
				return;
			}
			// TODO add clan name family links to all proceeding names
			if(clan!=null){
				generateParseWarning(addnameXMLId, 
						"Internal error - state is "+state
						+" (not FOUND_CLAN) but clan not null!");
			} else {
				clan = new NameRoleActivity(addname, clanAR, activity, 
						addnameXMLId, document, null);
	        	document.addNameRoleActivity(clan);
			}
	    	state = S_FOUND_CLAN;
		} catch(RuntimeException re) {
			generateParseWarning(addnameXMLId, re.getLocalizedMessage());
			throw re;
		}
	}
	
	private void addNameFamilyLinks() {
		if(baseForenameNRAD!=null) {	// Add to found forenames
			if(clan!=null) {
				baseForenameNRAD.addNameFamilyLink(clan, 
						LinkType.Type.LINK_TO_CLAN);
			}
			if(father!=null) {
				baseForenameNRAD.addNameFamilyLink(father, 
							LinkType.Type.LINK_TO_FATHER);
				if(clan!=null) {
					father.addNameFamilyLink(clan, 
							LinkType.Type.LINK_TO_CLAN);
				}
				if(grandfather!=null) {
					baseForenameNRAD.addNameFamilyLink(grandfather, 
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
						baseForenameNRAD.addNameFamilyLink(anc1, 
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
							baseForenameNRAD.addNameFamilyLink(anc2, 
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
								generateParseWarning(baseForenameNRAD.getXmlID(), 
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
		logger.warn("Warning near element: {} in document: {}", 
				((xmlid!=null)?xmlid:"(unknown)"), document.getAlt_id()	+": "+warning);
	}
	
}
