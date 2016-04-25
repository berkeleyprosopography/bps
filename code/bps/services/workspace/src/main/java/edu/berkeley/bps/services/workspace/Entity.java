package edu.berkeley.bps.services.workspace;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import edu.berkeley.bps.services.corpus.Document;
import edu.berkeley.bps.services.corpus.Name;
import edu.berkeley.bps.services.corpus.NameRoleActivity;

@XmlAccessorType(XmlAccessType.NONE)
public abstract class Entity {

	@XmlElement
	protected int id = -1;

	@XmlElement
	protected NameRoleActivity originalNRAD = null;
	protected Name declaredName = null;
	protected String displayName = null;

	protected Entity() {
		throw new RuntimeException("No-arg Ctor should not be called");
	}

	public Entity(NameRoleActivity nrad) {
		super();
		String fNameStr = null;
		if((originalNRAD=nrad)==null 
			// || (declaredName=originalNRAD.getName())==null 
			// We can create Clan entities now
			// || (declaredName.getNameType()!=Name.NAME_TYPE_CLAN)
			// Need to support Person with missing forename...
			// || (fNameStr=declaredName.getName())==null
			// ||  fNameStr.isEmpty()
			)
			throw new IllegalArgumentException(
				this.getClass().getName()+"Entity ctor must have valid name.");
		this.id = originalNRAD.getId();		// Tied to base - simplifies mapping
		declaredName=originalNRAD.getName();
		//displayName = declaredName.getName();
		displayName = nrad.getDisplayName();
	}

	public Name getDeclaredName() {
		return declaredName;
	}

	@XmlElement(name="declaredName")
	public String getDeclaredNameString() {
		return declaredName.getName();
	}

	public void setDeclaredName(Name declaredName) {
		this.declaredName = declaredName;
	}

	/**
	 * @return the displayName
	 */
	@XmlElement(name="displayName")
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * @param displayName the displayName to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * @return the originalNRAD
	 */
	public NameRoleActivity getOriginalNRAD() {
		return originalNRAD;
	}

	/**
	 * @return the originalNRAD
	 */
	public Document getOriginalDocument() {
		return originalNRAD.getDocument();
	}
	
	public abstract int getNumQualifiers();


	public String toString() {
		return displayName;
	}

}