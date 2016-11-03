package edu.berkeley.bps.services.workspace.collapser;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

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
		allIntraDocRules = new ArrayList<CollapserRuleBase>();
		intraDocShiftRules = new ArrayList<CollapserRuleBase>();
		intraDocDiscountRules = new ArrayList<CollapserRuleBase>();
		intraDocBoostRules = new ArrayList<CollapserRuleBase>();
		allCorpusWideRules = new ArrayList<CollapserRuleBase>();
		corpusWideShiftRules = new ArrayList<CollapserRuleBase>();
		corpusWideDiscountRules = new ArrayList<CollapserRuleBase>();
		corpusWideBoostRules = new ArrayList<CollapserRuleBase>();
	}
	
	public CollapserBase(CollapserBase base) {
		allIntraDocRules = base.allIntraDocRules;
		intraDocShiftRules = base.intraDocShiftRules;
		intraDocDiscountRules = base.intraDocDiscountRules;
		intraDocBoostRules = base.intraDocBoostRules;
		allCorpusWideRules = base.allCorpusWideRules;
		corpusWideShiftRules = base.corpusWideShiftRules;
		corpusWideDiscountRules = base.corpusWideDiscountRules;
		corpusWideBoostRules = base.corpusWideBoostRules;
	}



	@Override
	public void addRule(CollapserRuleBase rule) {
		addRule(rule, null);
	}
	
	@Override
	public void addRule(CollapserRuleBase rule, String insertBefore) {
		if(insertBefore!=null)
			throw new UnsupportedOperationException("insertBefore support is NYI");
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
