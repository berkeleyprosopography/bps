package edu.berkeley.bps.services.workspace.collapser;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import edu.berkeley.bps.services.workspace.Entity;
import edu.berkeley.bps.services.workspace.Person;
import edu.berkeley.bps.services.workspace.Workspace;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "Rule")
public class PartlyQualifiedCompatibleNameShiftRule extends CollapserRuleBaseWithUI {
	private static final String myClass = "PartlyQualifiedCompatibleNameShiftRule";
	private static final String DESCRIPTION_OLD = 
		"Collapse partly qualified citations with compatible, fully qualified citations"
		+" (e.g., \"PNa, son-of PNb\" and \"PNa, son-of PNb, in-clan CNc\")";
	
	private static final String DESCRIPTION = 
			"<b>Collapse partly qualified citations with compatible, fully qualified citations <br />"
			+"(e.g., &quot;<i>PN<sub>a</sub>, son-of PN<sub>b</sub></i>&quot; and "
			+"&quot;<i>PN<sub>a</sub>, son-of PN<sub>b</sub>,  in-clan CN<sub>c</sub></i>&quot;)</b>";
	
	private static final String UIGROUP_INTRA = "Step1B";
	private static final String UIGROUP_INTER = "Step2B";

	public PartlyQualifiedCompatibleNameShiftRule() {
		this(WEIGHT_ALWAYS, WITHIN_DOCUMENTS);
	}

	public PartlyQualifiedCompatibleNameShiftRule(double weight, boolean intraDocument) {
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
