package edu.berkeley.bps.services.workspace.collapser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.berkeley.bps.services.corpus.NameRoleActivity;
import edu.berkeley.bps.services.workspace.Entity;
import edu.berkeley.bps.services.workspace.Person;

public abstract class CollapserBase implements Collapser {
	
	
	protected List<CollapserRule> allIntraDocRules;
	protected List<CollapserRule> intraDocShiftRules;
	protected List<CollapserRule> intraDocDiscountRules;
	protected List<CollapserRule> intraDocBoostRules;
	protected List<CollapserRule> allCorpusWideRules;
	protected List<CollapserRule> corpusWideShiftRules;
	protected List<CollapserRule> corpusWideDiscountRules;
	protected List<CollapserRule> corpusWideBoostRules;
	
	public CollapserBase() {
		allIntraDocRules = new ArrayList<CollapserRule>();
		intraDocShiftRules = new ArrayList<CollapserRule>();
		intraDocDiscountRules = new ArrayList<CollapserRule>();
		intraDocBoostRules = new ArrayList<CollapserRule>();
		allCorpusWideRules = new ArrayList<CollapserRule>();
		corpusWideShiftRules = new ArrayList<CollapserRule>();
		corpusWideDiscountRules = new ArrayList<CollapserRule>();
		corpusWideBoostRules = new ArrayList<CollapserRule>();
	}

	@Override
	public void addRule(CollapserRule rule) {
		addRule(rule, null);
	}
	
	@Override
	public void addRule(CollapserRule rule, String insertBefore) {
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
	public List<CollapserRule> getRules(int typeFilter, boolean intraDocument) {
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
