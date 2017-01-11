package edu.berkeley.bps.services.workspace.collapser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import edu.berkeley.bps.services.common.utils.Pair;

@XmlAccessorType(XmlAccessType.NONE)
@XmlSeeAlso({CollapserRuleBaseWithUI.class,
		FullyQualifiedEqualNameShiftRule.class,
		PartlyQualifiedEqualNameShiftRule.class,
		UnqualifiedEqualNameShiftRule.class,
		PartlyQualifiedCompatibleNameShiftRule.class,
		UnqualifiedCompatibleNameShiftRule.class,
		RoleMatrixDiscountRule.class})
@XmlRootElement
public abstract class CollapserBase implements Collapser {
	
	@XmlAccessorType(XmlAccessType.NONE)
	@XmlRootElement	
	public static class CollapserUIGroup {
		@XmlElement
		String name;
		@XmlElement
		String header;
		
		public CollapserUIGroup() {
			this(null, null);
		}

		public CollapserUIGroup(String name, String header) {
			this.name = name;
			this.header = header;
		}
	}
	
	@XmlElementWrapper
	protected List<CollapserUIGroup> uiGroups;

	protected HashMap<String, CollapserRuleBase> allRulesByName;

	protected List<CollapserRuleBase> allIntraDocRules;
	@XmlElementWrapper
	@XmlAnyElement
	protected List<CollapserRuleBase> intraDocShiftRules;
	@XmlElementWrapper
	@XmlAnyElement
	protected List<CollapserRuleBase> intraDocDiscountRules;
	@XmlElementWrapper
	@XmlAnyElement
	protected List<CollapserRuleBase> intraDocBoostRules;
	
	protected List<CollapserRuleBase> allCorpusWideRules;
	@XmlElementWrapper
	@XmlAnyElement
	protected List<CollapserRuleBase> corpusWideShiftRules;
	@XmlElementWrapper
	@XmlAnyElement
	protected List<CollapserRuleBase> corpusWideDiscountRules;
	@XmlElementWrapper
	@XmlAnyElement
	protected List<CollapserRuleBase> corpusWideBoostRules;
	
	public CollapserBase() {
		uiGroups = 					new ArrayList<CollapserUIGroup>();
		allRulesByName = 			new HashMap<String, CollapserRuleBase>();
		allIntraDocRules = 			new ArrayList<CollapserRuleBase>();
		intraDocShiftRules = 		new ArrayList<CollapserRuleBase>();
		intraDocDiscountRules = 	new ArrayList<CollapserRuleBase>();
		intraDocBoostRules = 		new ArrayList<CollapserRuleBase>();
		allCorpusWideRules = 		new ArrayList<CollapserRuleBase>();
		corpusWideShiftRules = 		new ArrayList<CollapserRuleBase>();
		corpusWideDiscountRules = 	new ArrayList<CollapserRuleBase>();
		corpusWideBoostRules = 		new ArrayList<CollapserRuleBase>();
	}
	
	public CollapserBase(CollapserBase base) {
		uiGroups = base.uiGroups;
		allRulesByName = base.allRulesByName;
		allIntraDocRules = base.allIntraDocRules;
		intraDocShiftRules = base.intraDocShiftRules;
		intraDocDiscountRules = base.intraDocDiscountRules;
		intraDocBoostRules = base.intraDocBoostRules;
		allCorpusWideRules = base.allCorpusWideRules;
		corpusWideShiftRules = base.corpusWideShiftRules;
		corpusWideDiscountRules = base.corpusWideDiscountRules;
		corpusWideBoostRules = base.corpusWideBoostRules;
	}

	/**
	 * Defines a group of rules for the UI 
	 * @param rule the new rule to add
	 */
	public void addUIGroup(String groupName, String groupHeader) {
		if(hasUIGroup(groupName))
			throw new IllegalArgumentException("A UI Group with the name ["+groupName+"] already exists.");
		uiGroups.add(new CollapserUIGroup(groupName, groupHeader));
	}
	
	protected boolean hasUIGroup(String uiGroupName) {
		for(CollapserUIGroup uig : uiGroups ) {
			if(uig.name.equals(uiGroupName))
				return true;
		}
		return false;
	}

	@Override
	public void addRule(CollapserRuleBase rule) {
		addRule(rule, null);
	}
	
	@Override
	public void addRule(CollapserRuleBase rule, String insertBefore) {
		if(insertBefore!=null)
			throw new UnsupportedOperationException("insertBefore support is NYI");
		if(rule instanceof CollapserRuleBaseWithUI) {
			String uig = ((CollapserRuleBaseWithUI)rule).getUIGroup();
			if(!hasUIGroup(uig))
				throw new IllegalArgumentException("Unknown UI Group specified for rule: "+uig);
		}
		if(allRulesByName.containsKey(rule.name)) {
			throw new IllegalArgumentException("Collapser already has a rule with name: "+rule.name);
		}
		switch(rule.getType()) {
		case CollapserRule.SHIFT_RULE:
			if(rule.appliesWithinDocument())
				intraDocShiftRules.add(rule);
			else
				corpusWideShiftRules.add(rule);
			break;
		case CollapserRule.DISCOUNT_RULE:
			if(rule.appliesWithinDocument())
				intraDocDiscountRules.add(rule);
			else
				corpusWideDiscountRules.add(rule);
			break;
		case CollapserRule.BOOST_RULE:
			if(rule.appliesWithinDocument())
				intraDocBoostRules.add(rule);
			else
				corpusWideBoostRules.add(rule);
			break;
		}
		if(rule.appliesWithinDocument())
			allIntraDocRules.add(rule);
		else
			allCorpusWideRules.add(rule);
		allRulesByName.put(rule.name, rule);
	}

	@Override
	public List<CollapserRuleBase> getRules(int typeFilter, boolean intraDocument) {
		if(typeFilter <=0)
			return intraDocument?allIntraDocRules:allCorpusWideRules;
		switch(typeFilter) {
		default:
			throw new IllegalArgumentException(this.getClass().getName()+
					".getRulesForContext: Unknown type specified as filter: "
					+typeFilter);
		case CollapserRule.SHIFT_RULE: 
			return  intraDocument?intraDocShiftRules:corpusWideShiftRules;
		case CollapserRule.DISCOUNT_RULE: 
			return  intraDocument?intraDocDiscountRules:corpusWideDiscountRules;
		case CollapserRule.BOOST_RULE: 
			return  intraDocument?intraDocBoostRules:corpusWideBoostRules;
		}
	}

}
