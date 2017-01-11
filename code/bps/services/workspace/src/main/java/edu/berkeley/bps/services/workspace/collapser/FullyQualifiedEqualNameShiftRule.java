package edu.berkeley.bps.services.workspace.collapser;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import edu.berkeley.bps.services.workspace.Entity;
import edu.berkeley.bps.services.workspace.Person;
import edu.berkeley.bps.services.workspace.Workspace;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public class FullyQualifiedEqualNameShiftRule extends CollapserRuleBaseWithUI {
	private static final String myClass = "FullyQualifiedEqualNameShiftRule";
	private static final String DESCRIPTION = 
		"Collapse equal, fully qualified citations" 
		+" (e.g., \"PNa, son-of PNb, in-clan CNc\""
		+" and \"PNa, son-of PNb, in-clan CNc\")";
	private static final String UIGROUP_INTRA = "Step1A";
	private static final String UIGROUP_INTER = "Step2A";

	public FullyQualifiedEqualNameShiftRule() {
		this(WEIGHT_ALWAYS, WITHIN_DOCUMENTS);
	}

	public FullyQualifiedEqualNameShiftRule(double weight, boolean intraDocument) {
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
		if(fromPerson.isFullyFiliated() && toPerson.isFullyFiliated()
			&& Person.EQUAL==fromPerson.compareByNames(toPerson))
			return weight;
		else	// not equal, so this rule does not apply
			return SHIFT_RULE_NO_MATCH;
	}
	
	@Override
	public void initialize(Workspace workspace) {
		super.initialize(workspace);
		initSettings();						// Set up the user settings.
	}

}
