package bps.services.corpus.main;

import java.util.ArrayList;

public class Activity {
	private static int	nextID = 1;

	private int			id;
	private String		name;
	private String		description;
	private Activity	parent;
	private ArrayList<Activity>	children;
	/**
	 * Create a new empty corpus.
	 */
	public Activity() {
		this(Activity.nextID++, null, null, null);
	}

	/**
	 * @param name A shorthand name for use in UI, etc.
	 * @param description Any description useful to users.
	 * @param parent Broader activity that this specializes.
	 */
	public Activity(String name, String description, Activity parent) {
		this(Activity.nextID++, name, description, parent);
	}

	/**
	 * Ctor with all params - not generally used.
	 * @see Activity( String name, String description, Activity parent )
	 * @param id
	 * @param name A shorthand name for use in UI, etc.
	 * @param description Any description useful to users.
	 * @param parent Broader activity that this specializes.
	 */
	public Activity(int id, String name, String description, Activity parent) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.parent = parent;
		if(parent!=null)
			parent.addChild(this);
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
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the parent
	 */
	public Activity getParent() {
		return parent;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(Activity parent) {
		this.parent = parent;
	}

	/**
	 * @param child the child to add
	 */
	public void addChild(Activity child) {
		children.add(child);
	}

	/**
	 * @return the number of children activities
	 */
	public int getNChildren() {
		return children.size();
	}

	/**
	 * @return the ith child
	 */
	public Activity getChild(int iChild) {
		return children.get(iChild);
	}

	/**
	 * @return Name.
	 */
	public String toString() {
		return "{"+((name==null)?"(null)":name)+"}";
	}

	/**
	 * Produce SQL loadfile content for this instance
	 * @param sep The separator to use between entries
	 * @return loadfile string with no line terminator or newline.
	 */
	public String toXMLLoadString(String sep) {
		return id+sep+'"'+name+'"'+sep+'"'+description+'"'+sep+
			((parent==null)?"\\N":parent.id);
	}

}
