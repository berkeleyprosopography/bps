/**
 *
 */
package edu.berkeley.bps.services.corpus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.jboss.resteasy.annotations.cache.Cache;

import edu.berkeley.bps.services.common.LinkType;

/**
 * @author pschmitz
 *
 */
public class NameFamilyLink {
	private final static String myClass = "NameFamilyLink";
	private static int	nextID = CachedEntity.UNSET_ID_VALUE;

	private int					id;
	private NameRoleActivity	owner_nrad;
	private Name				linkTo;
	private LinkType.Type	linkType;
	/**
	 * The ID of the token associated with this in the owning document
	 */
	private String				xmlID;

	/**
	 * Create a new empty instance.
	 */
	public NameFamilyLink() {
		this(nextID--, null, null, null, null);
	}

	/**
	 * @param nameRoleActivity the instance of a name being annotated
	 * @param linkTo The Name of the linked family member (or clan)
	 * @param linkType one of the LINK_TO_* constants defined in the class
	 * @param xmlID The ID of the token associated with this in the owning document
	 */
	public NameFamilyLink(NameRoleActivity owner, Name linkTo, LinkType.Type linkType, String xmlID) {
		this(nextID--, owner, linkTo, linkType, xmlID);
	}

	/**
	 * Ctor with all params - not generally used.
	 * @param id ID of the instance to be created. Must be unique.
	 * @param owner The NameRoleActivity that is the link-from
	 * @param linkTo The Name of the linked family member (or clan)
	 * @param linkType one of the LINK_TO_* constants defined in the class
	 * @param xmlID The ID of the token associated with this in the owning document
	 */
	public NameFamilyLink(int id, NameRoleActivity owner, Name linkTo, LinkType.Type linkType, String xmlID) {
		this.id = id;
		this.owner_nrad = owner;
		this.linkTo = linkTo;
		this.linkType = linkType;
		this.xmlID = xmlID;
	}

	/**
	 * Creates a new NameFamilyLink entity, persists to the DB store, and sets the created ID. 
	 * @param dbConn an open JDCB connection
	 * @param corpusId 0 if a generic Name, else set to a linked corpus
	 * @param linkTo The linkTo form
	 * @param nametype One of NAME_TYPE_PERSON or NAME_TYPE_CLAN
	 * @param gender One of GENDER_MALE, GENDER_FEMALE, or GENDER_UNKNOWN
	 * @param notes Any notes on form, etc.
	 * @param normal The normal form of this name, if 'name' is not the normal form. 
	 * @return
	 */
	public static NameFamilyLink CreateAndPersist(Connection dbConn, 
			NameRoleActivity owner, Name linkTo, LinkType.Type linkType, String xmlID) {
		int newId = persistNew(dbConn, owner.getId(), linkTo.getId(), linkType, xmlID);
		NameFamilyLink newNFL = new NameFamilyLink(newId, owner, linkTo, linkType, xmlID); 
		return newNFL;
	}
	
	/**
	 * Persists a new Name entity to the DB store, and returns the created ID. 
	 * @param dbConn an open JDCB connection
	 * @param corpusId 0 if a generic Name, else set to a linked corpus
	 * @param name The name form
	 * @param nametype One of NAME_TYPE_PERSON or NAME_TYPE_CLAN
	 * @param gender One of GENDER_MALE, GENDER_FEMALE, or GENDER_UNKNOWN
	 * @param notes Any notes on form, etc.
	 * @param normal The normal form of this name, if 'name' is not the normal form. 
	 * @return
	 */
	public static int persistNew(Connection dbConn, int nrad_id,
			int name_id, LinkType.Type linkType, String xml_idref) {
		final String myName = ".persistNew: ";
		final String INSERT_STMT = 
			"INSERT INTO `familylink`(`nrad_id`,`name_id`,`link_type`,`xml_idref`,creation_time)"
			+" VALUES(?,?,?,?,now())";
			
		int newId = 0;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(INSERT_STMT, 
												Statement.RETURN_GENERATED_KEYS);
			stmt.setInt(1, nrad_id);
			stmt.setInt(2, name_id);
			stmt.setString(3, LinkType.ValueToString(linkType));
			stmt.setString(4, xml_idref);
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
			throw new RuntimeException( tmp );
		}
		return newId;
	}
	
	public void persist(Connection dbConn) {
		final String myName = ".persist: ";
		if(id<=CachedEntity.UNSET_ID_VALUE) {
			id = persistNew(dbConn, owner_nrad.getId(), linkTo.getId(), linkType, xmlID);
		} else {
			final String UPDATE_STMT = 
				"UPDATE `familylink`"
				+ " SET `nrad_id`=?,`name_id`=?,`link_type`=?,`xml_idref`=?"
				+ " WHERE id=?";
			try {
				PreparedStatement stmt = dbConn.prepareStatement(UPDATE_STMT);
				stmt.setInt(1, owner_nrad.getId());
				stmt.setInt(2, linkTo.getId());
				stmt.setString(3, LinkType.ValueToString(linkType));
				stmt.setString(4, xmlID);
				stmt.setInt(5, id);
				stmt.executeUpdate();
			} catch(SQLException se) {
				String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
				System.err.println(tmp);
				throw new RuntimeException( tmp );
			}
		}
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
	}

	/**
	 * @return the linkTo
	 */
	public Name getLinkToName() {
		return linkTo;
	}

	/**
	 * @param linkTo the linkTo to set
	 */
	public void setLinkToName(Name linkTo) {
		this.linkTo = linkTo;
	}

	/**
	 * @return the linkType
	 */
	public LinkType.Type getLinkType() {
		return linkType;
	}

	/**
	 * @param linkType the linkType to set
	 */
	public void setLinkType(LinkType.Type linkType) {
		this.linkType = linkType;
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

	public boolean isValid() {
		return !(linkTo==null||linkType==null);
	}

	/**
	 * @return Name, link type, XML idref concatenated.
	 */
	public String toString() {
		return "{"+((linkTo==null)?"?":linkTo.toString())+","
				+linkType+","
				+((xmlID==null)?"?":xmlID)+"}";
	}

}
