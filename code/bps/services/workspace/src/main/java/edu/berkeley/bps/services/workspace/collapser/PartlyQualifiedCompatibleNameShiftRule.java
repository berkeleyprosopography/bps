package edu.berkeley.bps.services.workspace.collapser;

import edu.berkeley.bps.services.workspace.Entity;
import edu.berkeley.bps.services.workspace.Person;

public class PartlyQualifiedCompatibleNameShiftRule extends CollapserRuleBaseWithUI {
	private static final String myClass = "PartlyQualifiedCompatibleNameShiftRule";
	private static final String DESCRIPTION = 
		"Collapse partly qualified citations with compatible, fully qualified citations"
		+" (e.g., \"PNa, son-of PNb\" and \"PNa, son-of PNb, in-clan CNc\")";

	public PartlyQualifiedCompatibleNameShiftRule(double weight, boolean intraDocument) {
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
		if(fromPerson.isPartiallyFiliated() && toPerson.isFullyFiliated()
			&& Person.COMPAT_MORE_INFO==toPerson.compareByNames(fromPerson)) {
				return weight;
		// Consider shifting to into from
		} else if(fromPerson.isFullyFiliated() && toPerson.isPartiallyFiliated()
			&& Person.COMPAT_MORE_INFO==fromPerson.compareByNames(toPerson)) {
			return -weight;	// invert shift direction
		} else { // not equal, so this rule does not apply
			return SHIFT_RULE_NO_MATCH;
		}
	}

}
