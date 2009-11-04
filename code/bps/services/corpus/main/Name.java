package bps.services.corpus.main;

/**
 * @author pschmitz
 *
 */
public class Name {
	private static int	nextID = 1;

	private int			id;
	/**
	 * The form of this name
	 */
	private String		name;
	/**
	 * Any notes about this form
	 */
	private String		notes;
	/**
	 * If this is not the normalized form, then normal is a reference to the normalized form.
	 * If normal is null, this is a normalized form.
	 */
	private Name		normal;

	/**
	 * Create a new empty name.
	 */
	public Name() {
		this(Name.nextID++, null, null, null);
	}

	/**
	 * Create a new Name.
	 * @param name A shorthand name for use in UI, etc.
	 * @param description Any description useful to users.
	 */
	public Name( String name ) {
		this(Name.nextID++, name, null, null);
	}

	/**
	 * Ctor with all params - not generally used.
	 * @param id ID of the Name to be created. Must be unique.
	 * @param name The name as represented in a document
	 * @param notes Researcher notes about this name.
	 * @param normal Reference to the normalized form (null if this is the normal form)
	 */
	public Name(int id, String name, String notes, Name normal) {
		super();
		this.id = id;
		this.name = name;
		this.notes = notes;
		this.normal = normal;
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
	 * @return the notes
	 */
	public String getNotes() {
		return notes;
	}

	/**
	 * @param notes the notes to set
	 */
	public void setNotes(String notes) {
		this.notes = notes;
	}

	/**
	 * @return the normal
	 */
	public Name getNormal() {
		return normal;
	}

	/**
	 * @param normal the normal to set
	 */
	public void setNormal(Name normal) {
		this.normal = normal;
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
			((notes!=null)?'"'+notes+'"':nullStr)+sep+
			((normal!=null)?normal.id:nullStr);
	}

}
