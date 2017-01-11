package edu.berkeley.bps.services.workspace.collapser;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import edu.berkeley.bps.services.workspace.Entity;
import edu.berkeley.bps.services.workspace.Person;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public class UnqualifiedCompatibleNameShiftRule extends CollapserRuleBaseWithUI {
	private static final String myClass = "UnqualifiedCompatibleNameShiftRule";
	private static final String DESCRIPTION = 
		"Collapse unqualified citations with compatible, more qualified citations"
		+" (e.g., \"PNa\" and \"PNa, son-of PNb, in-clan CNc\","
		+" OR, \"PNa\" and \"PNa, son-of PNb\")";
	private static final String UIGROUP_INTRA = "Step1B";
	private static final String UIGROUP_INTER = "Step2B";

	public UnqualifiedCompatibleNameShiftRule() {
		this(WEIGHT_ALWAYS, WITHIN_DOCUMENTS);
	}

	public UnqualifiedCompatibleNameShiftRule(double weight, boolean intraDocument) {
		super(SHIFT_RULE, ComputeDefaultName(myClass,intraDocument), DESCRIPTION, (intraDocument?UIGROUP_INTRA:UIGROUP_INTER),
				weight, intraDocument);
	}

	@Override
	public double evaluate(Entity fromEntity, Entity toEntity) {
		if(!(fromEntity instanceof Person) || 
			!(toEntity instanceof Person))
			throw new IllegalArgumentException(myClass+".evaluate must take Persons!");
		Person fromPerson = (Person)fromEntity;
		Person toPerson = (Person)toEntity;
		// Consider shifting from into to
		if(fromPerson.isUnfiliated() && 
				(toPerson.isFullyFiliated() || toPerson.isPartiallyFiliated())
			&& Person.COMPAT_MORE_INFO==toPerson.compareByNames(fromPerson)) {
				return weight;
		// Consider shifting to into from
		} else if(toPerson.isUnfiliated() && 
				(fromPerson.isFullyFiliated() || fromPerson.isPartiallyFiliated())
			&& Person.COMPAT_MORE_INFO==fromPerson.compareByNames(toPerson)) {
			return -weight;	// invert shift direction
		} else { // not equal, so this rule does not apply
			return SHIFT_RULE_NO_MATCH;
		}
	}

}
