package bps.services.corpus.main;

public class ActivityRole {
	private static int	nextID = 1;

	private int			id;
	private String		name;
	private String		description;

	/**
	 * Create a new empty ActivityRole.
	 */
	public ActivityRole() {
		this(ActivityRole.nextID++, null, null);
	}

	/**
	 * Create a new ActivityRole with just a name.
	 * @param name A shorthand name for use in UI, etc.
	 */
	public ActivityRole( String name ) {
		this(ActivityRole.nextID++, name, null);
	}

	/**
	 * Create a new ActivityRole with name and description.
	 * @param name A shorthand name for use in UI, etc.
	 * @param description Any description useful to users.
	 */
	public ActivityRole( String name, String description ) {
		this(ActivityRole.nextID++, name, description);
	}

	/**
	 * Ctor with all params - not generally used.
	 * @see ActivityRole( String name, String description )
	 * @param id ID of the ActivityRole to be created. Must be unique.
	 * @param name A shorthand name for use in UI, etc.
	 * @param description Any description useful to users.
	 */
	public ActivityRole(int id, String name, String description) {
		this.id = id;
		this.name = name;
		this.description = description;
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
	 * @return Name.
	 */
	public String toString() {
		return "{"+((name==null)?"(null)":name)+"}";
	}

	/**
	 * Produce SQL loadfile content for this instance
	 * @param sep The separator to use between entries
	 * @param nullStr The null indicator to use for missing entries
	 * @return loadfile string with no line terminator or newline.
	 */
	public String toXMLLoadString(String sep, String nullStr ) {
		return id+sep+
			((name!=null)?'"'+name+'"':nullStr)+sep+
			((description!=null)?'"'+description+'"':nullStr);
	}

}
