package edu.berkeley.bps.services.workspace.collapser;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import edu.berkeley.bps.services.workspace.Entity;
import edu.berkeley.bps.services.workspace.Person;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public class UnqualifiedEqualNameShiftRule extends CollapserRuleBaseWithUI {
	private static final String myClass = "UnqualifiedEqualNameShiftRule";
	private static final String DESCRIPTION = 
		"Collapse equal, unqualified citations (e.g., \"PNa\" and \"PNa\")";
	private static final String UIGROUP_INTRA = "Step1A";
	private static final String UIGROUP_INTER = "Step2A";

	public UnqualifiedEqualNameShiftRule() {
		this(WEIGHT_ALWAYS, WITHIN_DOCUMENTS);
	}

	public UnqualifiedEqualNameShiftRule(double weight, boolean intraDocument) {
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
		if(fromPerson.isUnfiliated() && toPerson.isUnfiliated()
			&& Person.EQUAL==fromPerson.compareByNames(toPerson))
			return weight;
		else	// not equal, so this rule does not apply
			return SHIFT_RULE_NO_MATCH;
	}

}
