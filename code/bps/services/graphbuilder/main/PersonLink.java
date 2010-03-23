/**
 *
 */
package bps.services.graphbuilder.main;
import bps.services.common.main.LinkTypes;
/**
 * @author pschmitz
 *
 */
public class PersonLink<O> extends Object {
	private O fromObj;
	private Person person;
	private double weight;
	private LinkTypes type;

	public PersonLink(O fromObj, Person linkTo, double weight, LinkTypes linkType) {
		if(fromObj==null)
			throw new IllegalArgumentException("PersonLink must link from valid object");
		if(linkTo==null)
			throw new IllegalArgumentException("PersonLink must link to valid Person");
		if(weight <= 0 || weight > 1)
			throw new IllegalArgumentException("PersonLink weight must be in unit range (0-1)");
		this.fromObj = fromObj;
		this.person = linkTo;
		this.weight = weight;
		this.type = linkType;
	}

	public boolean equals(PersonLink<O> check){
		return fromObj==fromObj && check.person==person && check.type==type;
	}

	/**
	 * @return the weight
	 */
	public double getWeight() {
		return weight;
	}

	/**
	 * @param weight the weight to set
	 */
	public void setWeight(double weight) {
		this.weight = weight;
	}

	/**
	 * @param weight the weight to set
	 */
	public void adjustWeight(double delta) {
		this.weight += weight;
	}

	/**
	 * @return the fromObj
	 */
	public O getFromObj() {
		return fromObj;
	}

	/**
	 * @return the person
	 */
	public Person getPerson() {
		return person;
	}

	/**
	 * @return the type
	 */
	public LinkTypes getType() {
		return type;
	}
}
