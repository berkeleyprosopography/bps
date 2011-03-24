/**
 *
 */
package edu.berkeley.bps.services.corpus;

import edu.berkeley.bps.services.common.LinkTypes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author pschmitz
 *
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="nameRoleActivity")
public class NameRoleActivity 
	implements Comparable<NameRoleActivity> {
	private final static String myClass = "NameRoleActivity";
	private static int	nextID = CachedEntity.UNSET_ID_VALUE;

	@XmlElement
	private int				id;
	private Name			name;
	private Name			normalName;
	private ActivityRole	role;
	private Activity		activity;
	private Document		document;
	
	private int[]			compKey = {0,0,0};
	
	/**
	 * The ID of the token associated with this in the owning document
	 */
	@XmlElement
	private String			xmlID;

	// Any family links. May be null.
	private ArrayList<NameFamilyLink> nameFamilyLinks;

	/**
	 * Create a new empty instance.
	 */
	private NameRoleActivity() {
		this(nextID--, null, null, null, null, null);
	}

	/**
	 * @param name The Name involved in this instance
	 * @param role The ActivityRole involved in this instance
	 * @param activity The Activity involved in this instance
	 * @param xmlID The ID of the token associated with this in the owning document
	 */
	public NameRoleActivity(Name name, ActivityRole role,
			Activity activity, String xmlID, Document document) {
		this(nextID--, name, role, activity, xmlID, document);
	}

	/**
	 * Ctor with all params - not generally used.
	 * @param id ID of the instance to be created. Must be unique.
	 * @param name The Name involved in this instance
	 * @param role The ActivityRole involved in this instance
	 * @param activity The Activity involved in this instance
	 * @param xmlID The ID of the token associated with this in the owning document
	 */
	private NameRoleActivity(int id, Name name, ActivityRole role,
			Activity activity, String xmlID, Document document) {
		this.id = id;
		this.name = name;
		if(name==null)
			this.normalName = null;
		else if(null==(this.normalName = name.getNormal()))
			this.normalName = name;
		this.role = role;
		this.activity = activity;
		this.xmlID = xmlID;
		this.document = document;
		this.nameFamilyLinks = null;
		this.resetCompKey();
	}
	
	public NameRoleActivity cloneInDocument(Connection dbConn, Document inDoc) {
		Corpus corpus = inDoc.getCorpus();
		Name nameClone = (name==null)?null:corpus.findName(name.getName());
		ActivityRole roleClone = (role==null)?null:corpus.findActivityRole(role.getName());
		Activity activityClone = (activity==null)?null:corpus.findActivity(activity.getName());
		NameRoleActivity clone = 
			new NameRoleActivity(nameClone, roleClone, activityClone,
					xmlID, inDoc);
		clone.persist(dbConn);
		// TODO clone all the name-family links
		return clone;
	}
	
	private void resetCompKey() {
		compKey[0] = (activity==null)?0:activity.getId();
		compKey[1] = (role==null)?0:role.getId();
		compKey[2] = id;
	}
	
	public int compareTo(NameRoleActivity nrad2) {
		int result = compKey[0] - nrad2.compKey[0]; 
		if(result == 0) {
			result = compKey[1] - nrad2.compKey[1];
			if(result == 0) {
				result = compKey[2] - nrad2.compKey[2];
			}
		}
		return result;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
		resetCompKey();
	}

	/**
	 * @return the name
	 */
	public Name getName() {
		return name;
	}

	/**
	 * @return the id of the name
	 */
	@XmlElement(name="nameId")
	public int getNameId() {
		return (name==null)?0:name.getId();
	}

	/**
	 * @return the string for the name
	 */
	@XmlElement(name="name")
	public String getNameString() {
		return (name==null)?null:name.getName();
	}

	/**
	 * @param name the name to set
	 */
	public void setName(Name name) {
		this.name = name;
	}

	/**
	 * @return the normalName
	 */
	public Name getNormalName() {
		return normalName;
	}

	/**
	 * @return the id of the normalName, (may be same as nameId)
	 */
	@XmlElement(name="normalNameId")
	public int getNormalNameId() {
		return (normalName==null)?0:normalName.getId();
	}

	/**
	 * @return the string for the name
	 */
	@XmlElement(name="normalName")
	public String getNormalNameString() {
		return (normalName==null)?null:normalName.getName();
	}

	/**
	 * @param normalName the normalName to set
	 */
	public void setNormalName(Name normalName) {
		this.normalName = normalName;
	}

	/**
	 * @return the role
	 */
	public ActivityRole getRole() {
		return role;
	}

	/**
	 * @return the id of the activityRole
	 */
	@XmlElement(name="activityRoleId")
	public int getRoleId() {
		return (role==null)?0:role.getId();
	}

	/**
	 * @return the string for the name
	 */
	@XmlElement(name="activityRole")
	public String getRoleString() {
		return (role==null)?null:role.getName();
	}

	/**
	 * @param role the role to set
	 */
	public void setRole(ActivityRole role) {
		this.role = role;
		resetCompKey();
	}

	/**
	 * @return the activity
	 */
	public Activity getActivity() {
		return activity;
	}

	/**
	 * @return the id of the activity
	 */
	@XmlElement(name="activityId")
	public int getActivityId() {
		return (activity==null)?0:activity.getId();
	}

	/**
	 * @return the string for the name
	 */
	@XmlElement(name="activity")
	public String getActivityString() {
		return (activity==null)?null:activity.getName();
	}

	/**
	 * @param activity the activity to set
	 */
	public void setActivity(Activity activity) {
		this.activity = activity;
		resetCompKey();
	}

	/**
	 * @return the xmlID
	 */
	public String getXmlID() {
		return xmlID;
	}

	/**
	 * @param xmlID the xmlID to set
	 */
	public void setXmlID(String xmlID) {
		this.xmlID = xmlID;
	}

	public void addNameFamilyLink( NameFamilyLink nfl ) {
		initNameFamilyLinks();
		nameFamilyLinks.add(nfl);
	}

	/**
	 * @param nameRoleActivity the instance of a name being annotated
	 * @param name The Name of the linked family member (or clan)
	 * @param linkType one of the LINK_TO_* constants defined in the class
	 * @param xmlID The ID of the token associated with this in the owning document
	 */
	public void addNameFamilyLink(Name name, LinkTypes.Values linkType, String xmlID) {
		initNameFamilyLinks();
		nameFamilyLinks.add(new NameFamilyLink(this, name, linkType, xmlID));
	}

	/**
	 * Init the nameFamilyLinks
	 */
	private void initNameFamilyLinks() {
		if(null==nameFamilyLinks) {
			nameFamilyLinks = new ArrayList<NameFamilyLink>();
		}
	}

	/**
	 * @return the nameFamilyLinks
	 */
	public ArrayList<NameFamilyLink> getNameFamilyLinks() {
		return nameFamilyLinks;
	}

	/**
	 * @return a declared Father if there is one
	 */
	public Name getFatherName() {
		return findFamilyLinkNameByType(LinkTypes.Values.LINK_TO_FATHER);
	}

	/**
	 * @return a declared Clan if there is one
	 */
	public Name getClanName() {
		return findFamilyLinkNameByType(LinkTypes.Values.LINK_TO_CLAN);
	}

	/**
	 * @return a declared GrandFather if there is one
	 */
	public Name getGrandFatherName() {
		return findFamilyLinkNameByType(LinkTypes.Values.LINK_TO_GRANDFATHER);
	}

	/**
	 * @return any declared ancestors
	 */
	public List<Name> getAncestorNames() {
		return findFamilyLinkNamesByType(LinkTypes.Values.LINK_TO_ANCESTOR);
	}

	/**
	 * @param linkType one of LinkTypes.LINK_TO_*
	 * @return name of the first family link matching linkType
	 */
	public Name findFamilyLinkNameByType(LinkTypes.Values linkType) {
		for(NameFamilyLink nfl:nameFamilyLinks) {
			if(nfl.getLinkType()==linkType)
				return nfl.getLinkToName();
		}
		return null;
	}

	/**
	 * @param linkType one of NameFamilyLink.LINK_TO_*
	 * @return array of names for family links matching linkType
	 */
	public ArrayList<Name> findFamilyLinkNamesByType(LinkTypes.Values linkType) {
		ArrayList<Name> list = null;
		for(NameFamilyLink nfl:nameFamilyLinks) {
			if(nfl.getLinkType()==linkType) {
				if(list==null) {
					list = new ArrayList<Name>();
				}
				list.add(nfl.getLinkToName());
			}
		}
		return list;
	}

	public boolean isValid() {
		// Have to allow empty names - sometimes they are missing...
		return (role!=null && activity!=null && document!=null);
	}

	public void persist(Connection dbConn) {
		final String myName = ".persist: ";
		int nameId = getNameId();
		int roleId = getRoleId();
		int activityId = getActivityId();
		int docId = getDocumentId();
		if(id <= CachedEntity.UNSET_ID_VALUE) {
			setId(persistNew(dbConn, nameId, roleId, activityId, docId, xmlID));
		} else {
			final String UPDATE_STMT = 
				"UPDATE name_role_activity_doc SET"
				+" name_id=?,act_role_id=?,activity_id=?,document_id=?,xml_idref=?"
				+" WHERE id=?";
			try {
				PreparedStatement stmt = dbConn.prepareStatement(UPDATE_STMT);
				stmt.setInt(1, nameId);
				stmt.setInt(2, roleId);
				stmt.setInt(3, activityId);
				stmt.setInt(4, docId);
				stmt.setString(5, xmlID);
				stmt.setInt(6, id);
				stmt.executeUpdate();
			} catch(SQLException se) {
				String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
				System.err.println(tmp);
				throw new RuntimeException( tmp );
			}
			
		}
	}
		
	private static int persistNew(Connection dbConn, 
			int name_id, int act_role_id, int activity_id, int document_id,  
			String xml_idref) {
		final String myName = ".persistNew: ";
		final String INSERT_STMT = 
			"INSERT INTO name_role_activity_doc"
			+ "(`name_id`,`act_role_id`,`activity_id`,`document_id`, `xml_idref`)"
			+ " VALUES(?,?,?,?,?)";
		int newId = 0;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(INSERT_STMT, 
												Statement.RETURN_GENERATED_KEYS);
			stmt.setInt(1, name_id);
			stmt.setInt(2, act_role_id);
			stmt.setInt(3, activity_id);
			stmt.setInt(4, document_id);
			stmt.setString(5, xml_idref);
			int nRows = stmt.executeUpdate();
			if(nRows==1){
				ResultSet rs = stmt.getGeneratedKeys();
				if(rs.next()){
					newId = rs.getInt(1); 
				}
				rs.close();
			}
		} catch(SQLException se) {
			String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
			System.err.println(tmp);
	    	throw new WebApplicationException( 
	    			Response.status(
	    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		}
		return newId;
	}

	public static List<NameRoleActivity> ListAllInDocument(Connection dbConn, Document document) {
		final String SELECT_BY_DOC_ID = 
			"SELECT id, name_id, act_role_id, activity_id, xml_idref"
			+" FROM name_role_activity_doc WHERE document_id = ?";
		int doc_id = 0;
		if(document==null || (doc_id=document.getId())<=0) {
			String tmp = myClass+".ListAllInDocument: Invalid document.\n";
			System.err.println(tmp);
			throw new IllegalArgumentException( tmp );
		}
		ArrayList<NameRoleActivity> nradList = new ArrayList<NameRoleActivity>();
		Corpus corpus = document.getCorpus();
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_BY_DOC_ID);
			stmt.setInt(1, doc_id);
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
				int name_id = rs.getInt("name_id");
				int act_role_id = rs.getInt("act_role_id");
				int activity_id = rs.getInt("activity_id");
				String xml_idref = rs.getString("xml_idref");
				Name name = corpus.findName(name_id);
				ActivityRole role = corpus.findActivityRole(act_role_id);
				Activity activity = corpus.findActivity(activity_id);
				NameRoleActivity nrad = new NameRoleActivity(
						rs.getInt("id"), name, role, activity, xml_idref, document );
				nradList.add(nrad);
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+".ListAllInCorpus: Problem querying DB.\n"+ se.getMessage();
			System.err.println(tmp);
			throw new RuntimeException( tmp );
		}
		return nradList;
	}
	
	public static void DeleteAllInCorpus(Connection dbConn, Corpus corpus) {
		final String DELETE_ALL = 
			"DELETE name_role_activity_doc FROM name_role_activity_doc "
			+" INNER JOIN document d"
			+" ON name_role_activity_doc.document_id = d.id"
			+" WHERE d.corpus_id=?";
		int corpus_id = corpus.getId();
		try {
			PreparedStatement stmt = dbConn.prepareStatement(DELETE_ALL);
			stmt.setInt(1, corpus_id);
			stmt.executeUpdate();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+".DeleteAllInCorpus(): Problem querying DB.\n"+ se.getMessage();
			System.err.println(tmp);
			throw new WebApplicationException( 
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
							"Problem deleting nrads\n"+se.getLocalizedMessage()).build());
		}
	}
	
	/**
	 * @return Name, Role-name, Activity-name, XML idref concatenated.
	 */
	public String toString() {
		return "{"+((name==null)?"?":name.getName())+","+((role==null)?"?":role.getName())
				+","+((activity==null)?"?":activity.getName())+","+((xmlID==null)?"?":xmlID)+"}";
	}

	/**
	 * Produce SQL loadfile content for this instance
	 * @param sep The separator to use between entries
	 * @param nullStr The null indicator to use for missing entries
	 * @param docId The document in which this appears.
	 * @return loadfile string with no line terminator or newline.
	 */
	public String toXMLLoadString(String sep, String nullStr ) {
		if(!isValid())
			throw new RuntimeException(
					"Attempt to generate XML loadfile string for invalid NameRoleActivity.");
		return id+sep+((name==null)?nullStr:name.getName())+sep+role.getId()+sep+
				activity.getId()+sep+document.getId()+sep+
				((xmlID!=null)?'"'+xmlID+'"':nullStr);
	}

	public Document getDocument() {
		return document;
	}

	/**
	 * @return the id of the activity
	 */
	@XmlElement(name="document")
	public int getDocumentId() {
		return (document==null)?0:document.getId();
	}

	public void setDocument(Document document) {
		this.document = document;
	}

}
