package edu.berkeley.bps.services.workspace.collapser;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import edu.berkeley.bps.services.workspace.Entity;
import edu.berkeley.bps.services.workspace.Person;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public class PartlyQualifiedEqualNameShiftRule extends CollapserRuleBaseWithUI {
	private static final String myClass = "PartlyQualifiedEqualNameShiftRule";
	private static final String DESCRIPTION = "Collapse equal, partly qualified citations" 
			+" (e.g., \"PNa, son-of PNb\" and \"PNa, son-of PNb\")";

	public PartlyQualifiedEqualNameShiftRule() {
		this(WEIGHT_ALWAYS, WITHIN_DOCUMENTS);
	}

	public PartlyQualifiedEqualNameShiftRule(double weight, boolean intraDocument) {
		super(SHIFT_RULE, myClass, DESCRIPTION, weight, intraDocument);
	}

	@Override
	public double evaluate(Entity fromEntity, Entity toEntity) {
		if(!(fromEntity instanceof Person) || 
			!(toEntity instanceof Person))
			throw new IllegalArgumentException(myClass+".evaluate must take Persons!");
		Person fromPerson = (Person)fromEntity;
		Person toPerson = (Person)toEntity;
		// Consider shifting from into to
		if(fromPerson.isPartiallyFiliated() && toPerson.isPartiallyFiliated()
			&& Person.EQUAL==fromPerson.compareByNames(toPerson)) {
			return weight;
		} else { // not equal, so this rule does not apply
			return SHIFT_RULE_NO_MATCH;
		}
	}

}
