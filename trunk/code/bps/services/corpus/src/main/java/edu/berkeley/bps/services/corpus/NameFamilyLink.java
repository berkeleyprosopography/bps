/**
 *
 */
package edu.berkeley.bps.services.corpus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
	private NameRoleActivity	linkFrom;
	private NameRoleActivity	linkTo;
	private LinkType.Type		linkType;

	/**
	 * Create a new empty instance.
	 */
	public NameFamilyLink() {
		this(nextID--, null, null, null);
	}

	/**
	 * @param linkFrom the instance of a name being annotated
	 * @param linkTo The NRAD of the linked family member (or clan)
	 * @param linkType one of the LINK_TO_* constants defined in the class
	 */
	public NameFamilyLink(NameRoleActivity linkFrom, NameRoleActivity linkTo, LinkType.Type linkType) {
		this(nextID--, linkFrom, linkTo, linkType);
	}

	/**
	 * Ctor with all params - not generally used.
	 * @param id ID of the instance to be created. Must be unique.
	 * @param linkFrom The NameRoleActivity that is the link-from
	 * @param linkTo The NameRoleActivity of the linked family member (or clan)
	 * @param linkType one of the LINK_TO_* constants defined in the class
	 */
	public NameFamilyLink(int id, NameRoleActivity linkFrom, NameRoleActivity linkTo, LinkType.Type linkType) {
		this.id = id;
		this.linkFrom = linkFrom;
		this.linkTo = linkTo;
		this.linkType = linkType;
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
			NameRoleActivity linkFrom, NameRoleActivity linkTo, LinkType.Type linkType) {
		int newId = persistNew(dbConn, linkFrom.getId(), linkTo.getId(), linkType);
		NameFamilyLink newNFL = new NameFamilyLink(newId, linkFrom, linkTo, linkType); 
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
	public static int persistNew(Connection dbConn, int from_nrad_id,
			int to_nrad_id, LinkType.Type linkType) {
		final String myName = ".persistNew: ";
		final String INSERT_STMT = 
			"INSERT INTO `familylink`(`from_nrad_id`,`to_nrad_id`,`link_type`)"
			+" VALUES(?,?,?)";
			
		int newId = 0;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(INSERT_STMT, 
												Statement.RETURN_GENERATED_KEYS);
			stmt.setInt(1, from_nrad_id);
			stmt.setInt(2, to_nrad_id);
			String enumStr = LinkType.ValueToString(linkType);
			stmt.setString(3, enumStr);
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
			id = persistNew(dbConn, linkFrom.getId(), linkTo.getId(), linkType);
		} else {
			final String UPDATE_STMT = 
				"UPDATE `familylink`"
				+ " SET `from_nrad_id`=?,`to_nrad_id`=?,`link_type`=?"
				+ " WHERE id=?";
			try {
				PreparedStatement stmt = dbConn.prepareStatement(UPDATE_STMT);
				stmt.setInt(1, linkFrom.getId());
				stmt.setInt(2, linkTo.getId());
				stmt.setString(3, LinkType.ValueToString(linkType));
				stmt.setInt(4, id);
				stmt.executeUpdate();
			} catch(SQLException se) {
				String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
				System.err.println(tmp);
				throw new RuntimeException( tmp );
			}
		}
	}
	
	public static List<NameFamilyLink> GetLinksForNRAD(Connection dbConn, 
			NameRoleActivity linkFrom, HashMap<Integer, NameRoleActivity> context) {
		final String SELECT_BY_FROM_ID = 
			"SELECT id, to_nrad_id,link_type"
			+" FROM familylink WHERE from_nrad_id = ?";
		int nrad_id = 0;
		if(linkFrom==null || (nrad_id=linkFrom.getId())<=0) {
			String tmp = myClass+".ListAllForNRAD: Invalid nrad.\n";
			System.err.println(tmp);
			throw new IllegalArgumentException( tmp );
		}
		ArrayList<NameFamilyLink> nflList = new ArrayList<NameFamilyLink>();
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_BY_FROM_ID);
			stmt.setInt(1, nrad_id);
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
				int id = rs.getInt("id");
				int to_nrad_id = rs.getInt("to_nrad_id");
				NameRoleActivity linkTo = context.get(to_nrad_id);
				if(linkTo==null) {
					throw new RuntimeException(myClass
							+".ListAllForNRAD: Cannot find NRAD for id:"+to_nrad_id);
				}
				String link_type = rs.getString("link_type");
				LinkType.Type linkType = LinkType.ValueFromString(link_type);
				NameFamilyLink nfl = new NameFamilyLink(id, linkFrom, linkTo, linkType);
				nflList.add(nfl);
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+".ListAllForNRAD: Problem querying DB.\n"+ se.getMessage();
			System.err.println(tmp);
			throw new RuntimeException( tmp );
		}
		return nflList;
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
	 * @return the linkFrom
	 */
	public NameRoleActivity getLinkFrom() {
		return linkFrom;
	}

	/**
	 * @param linkFrom the linkFrom to set
	 */
	public void setLinkFrom(NameRoleActivity linkFrom) {
		this.linkFrom = linkFrom;
	}

	/**
	 * @return the linkTo
	 */
	public NameRoleActivity getLinkTo() {
		return linkTo;
	}

	/**
	 * @param linkTo the linkTo to set
	 */
	public void setLinkTo(NameRoleActivity linkTo) {
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

	public boolean isValid() {
		return !(linkTo==null||linkType==null);
	}

	/**
	 * @return string representation of this link
	 */
	public String toString() {
		return "{"+((linkFrom==null)?"?":linkFrom.toString())
				+" has "+LinkType.ValueToString(linkType)+": "
				+((linkTo==null)?"?":linkTo.toString())+"}";
	}

}
