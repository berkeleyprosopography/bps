package edu.berkeley.bps.services.workspace.collapser;

import edu.berkeley.bps.services.workspace.Entity;
import edu.berkeley.bps.services.workspace.Person;

public class UnqualifiedEqualNameShiftRule extends CollapserRuleBaseWithUI {
	private static final String myClass = "UnqualifiedEqualNameShiftRule";
	private static final String DESCRIPTION = "TO DO";

	public UnqualifiedEqualNameShiftRule(double weight, boolean intraDocument) {
		super(SHIFT_RULE, myClass, DESCRIPTION, weight, intraDocument);
	}

	@Override
	public double evaluate(Entity fromEntity, Entity toEntity) {
		if(!(fromEntity instanceof Person) || 
			!(toEntity instanceof Person))
			throw new IllegalArgumentException(myClass+".evaluate must take Persons!");
		Person fromPerson = (Person)fromEntity;
		Person toPerson = (Person)toEntity;
		if(fromPerson.isUnfiliated() && toPerson.isUnfiliated()
			&& Person.EQUAL==fromPerson.compareByNames(toPerson))
			return weight;
		else	// not equal, so this rule does not apply
			return SHIFT_RULE_NO_MATCH;
	}

}
