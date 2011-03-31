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
	protected NameRoleActivity originalNRAD = null;
	@XmlElement
	protected Name declaredName = null;
	@XmlElement
	protected String displayName = null;

	public Entity(NameRoleActivity nrad) {
		super();
		String fNameStr = null;
		if((originalNRAD=nrad)==null 
			|| (declaredName=originalNRAD.getName())==null 
			// || (declaredName.getNameType()!=Name.NAME_TYPE_CLAN)
			|| (fNameStr=declaredName.getName())==null
			||  fNameStr.isEmpty())
			throw new IllegalArgumentException(
				this.getClass().getName()+"Entity ctor must have valid name.");
		//displayName = declaredName.getName();
		displayName = nrad.getDisplayName();
	}

	public Name getDeclaredName() {
		return declaredName;
	}

	public void setDeclaredName(Name declaredName) {
		this.declaredName = declaredName;
	}

	/**
	 * @return the displayName
	 */
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