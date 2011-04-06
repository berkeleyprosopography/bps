package edu.berkeley.bps.services.workspace.collapser;

import edu.berkeley.bps.services.workspace.Entity;
import edu.berkeley.bps.services.workspace.Person;

public class PartlyQualifiedEqualNameShiftRule extends CollapserRuleBaseWithUI {
	private static final String myClass = "PartlyQualifiedEqualNameShiftRule";

	public PartlyQualifiedEqualNameShiftRule(double weight, boolean intraDocument) {
		super(SHIFT_RULE, myClass, weight, intraDocument);
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
