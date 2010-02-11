/**
 *
 */
package bps.services.corpus.main;

import java.util.ArrayList;

/**
 * @author pschmitz
 *
 */
public class NameRoleActivity {
	private static int	nextID = 1;

	private int				id;
	private Name			name;
	private Name			normalName;
	private ActivityRole	role;
	private Activity		activity;
	/**
	 * The ID of the token associated with this in the owning document
	 */
	private String			xmlID;

	// Any family links. May be null.
	private ArrayList<NameFamilyLink> nameFamilyLinks;

	/**
	 * Create a new empty instance.
	 */
	public NameRoleActivity() {
		this(nextID++, null, null, null, null);
	}

	/**
	 * @param name The Name involved in this instance
	 * @param role The ActivityRole involved in this instance
	 * @param activity The Activity involved in this instance
	 * @param xmlID The ID of the token associated with this in the owning document
	 */
	public NameRoleActivity(Name name, ActivityRole role,
			Activity activity, String xmlID) {
		this(nextID++, name, role, activity, xmlID);
	}

	/**
	 * Ctor with all params - not generally used.
	 * @param id ID of the instance to be created. Must be unique.
	 * @param name The Name involved in this instance
	 * @param role The ActivityRole involved in this instance
	 * @param activity The Activity involved in this instance
	 * @param xmlID The ID of the token associated with this in the owning document
	 */
	public NameRoleActivity(int id, Name name, ActivityRole role,
			Activity activity, String xmlID) {
		this.id = id;
		this.name = name;
		if(name==null)
			this.normalName = null;
		else if(null==(this.normalName = name.getNormal()))
			this.normalName = name;
		this.role = role;
		this.activity = activity;
		this.xmlID = xmlID;
		this.nameFamilyLinks = null;
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
	 * @return the name
	 */
	public Name getName() {
		return name;
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
	 * @param role the role to set
	 */
	public void setRole(ActivityRole role) {
		this.role = role;
	}

	/**
	 * @return the activity
	 */
	public Activity getActivity() {
		return activity;
	}

	/**
	 * @param activity the activity to set
	 */
	public void setActivity(Activity activity) {
		this.activity = activity;
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
	public void addNameFamilyLink(Name name, int linkType, String xmlID) {
		initNameFamilyLinks();
		nameFamilyLinks.add(new NameFamilyLink(name, linkType, xmlID));
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

	public boolean isValid() {
		return !(name==null||role==null||activity==null);
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
	public String toXMLLoadString(int docId, String sep, String nullStr ) {
		if(!isValid())
			throw new RuntimeException(
					"Attempt to generate XML loadfile string for invalid NameRoleActivity.");
		return id+sep+name.getId()+sep+role.getId()+sep+
				activity.getId()+sep+docId+sep+
				((xmlID!=null)?'"'+xmlID+'"':nullStr);
	}

}
