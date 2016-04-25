/**
 *
 */
package edu.berkeley.bps.services.workspace;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.berkeley.bps.services.common.LinkType;
/**
 * @author pschmitz
 * Represents a link of two entities with a weight and a link type.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="entityLink")
public class EntityLink<O> extends Object {
	protected O fromObj;
	protected Entity entity;
	protected double weight;
	protected LinkType.Type type;
	
	protected EntityLink() {
		throw new RuntimeException("No-arg Ctor should not be called");
	}

	public EntityLink(O fromObj, Entity linkTo, double weight, LinkType.Type linkType) {
		if(fromObj==null)
			throw new IllegalArgumentException("EntityLink must link from valid object");
		if(linkTo==null)
			throw new IllegalArgumentException("EntityLink must link to valid Entity");
		if(weight <= 0 || weight > 1)
			throw new IllegalArgumentException("EntityLink weight not in unit range (0-1): "+weight);
		this.fromObj = fromObj;
		this.entity = linkTo;
		this.weight = weight;
		this.type = linkType;
	}

	public boolean equals(EntityLink<O> check){
		return fromObj==fromObj && check.entity==entity && check.type==type;
	}

	/**
	 * @return the weight
	 */
	@XmlElement(name="weight")
	public double getWeight() {
		return weight;
	}

	/**
	 * @param weight the weight to set
	 */
	public double setWeight(double weight) {
		double delta = weight - this.weight;
		this.weight = weight;
		return delta;
	}

	/**
	 * @param weight the weight to set
	 */
	public double adjustWeight(double delta) {
		this.weight += delta;
		return delta;
	}

	/**
	 * @param scaleFacter factor to multiple the current weight by for the link 
	 * 					to toEntity. Must be >=0 and <= 1.
	 */
	public double scaleWeight(double scaleFactor) {
		if(scaleFactor<0 || scaleFactor>1)
			throw new IllegalArgumentException(
					"scaleLink(): scaleFactor out of range: "+scaleFactor);
		double newWeight = weight*scaleFactor;
		double delta = newWeight - weight;
		weight = newWeight;
		return delta;
	}


	/**
	 * @return the fromObj
	 */
	public O getFromObj() {
		return fromObj;
	}

	/**
	 * @return the entity
	 */
	public Entity getEntity() {
		return entity;
	}

	/**
	 * @return the display name of the linked to entity
	 */
	@XmlElement(name="linkTo")
	public String getLinkTo() {
		return getEntity().getDisplayName();
	}

	/**
	 * @return the relative URL of the linked to entity. Encompasses the type and the ID
	 * TODO Create proper persisted entities for the entities, so that we can fetch them. 
	 */
	@XmlElement(name="linkToRelPath")
	public String getLinkToID() {
		return "persons/"+"??? (NYI)";
	}

	/**
	 * @return the type
	 */
	public LinkType.Type getType() {
		return type;
	}

	/**
	 * @return the type
	 */
	@XmlElement(name="type")
	public String getTypeString() {
		return LinkType.ValueToString(type);
	}
}
