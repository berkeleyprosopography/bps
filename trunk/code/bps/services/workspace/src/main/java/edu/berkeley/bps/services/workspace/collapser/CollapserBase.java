package edu.berkeley.bps.services.workspace.collapser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.berkeley.bps.services.corpus.NameRoleActivity;
import edu.berkeley.bps.services.workspace.Entity;
import edu.berkeley.bps.services.workspace.Person;

public abstract class CollapserBase implements Collapser {
	
	protected List<CollapserRule> allRules;
	protected List<CollapserRule> shiftRules;
	protected List<CollapserRule> discountRules;
	protected List<CollapserRule> boostRules;
	
	public CollapserBase() {
		allRules = new ArrayList<CollapserRule>();
		shiftRules = new ArrayList<CollapserRule>();
		discountRules = new ArrayList<CollapserRule>();
		boostRules = new ArrayList<CollapserRule>();
	}

	@Override
	public void addRule(CollapserRule rule, String insertBefore) {
		if(insertBefore!=null)
			throw new UnsupportedOperationException("insertBefore support is NYI");
		switch(rule.getType()) {
		case CollapserRule.SHIFT_RULE:
			shiftRules.add(rule);
			break;
		case CollapserRule.DISCOUNT_RULE:
			discountRules.add(rule);
			break;
		case CollapserRule.BOOST_RULE:
			boostRules.add(rule);
			break;
		}
		allRules.add(rule);
	}

	@Override
	public List<CollapserRule> getRules(int typeFilter) {
		if(typeFilter <=0)
			return allRules;
		switch(typeFilter) {
		default:
			throw new IllegalArgumentException("Unknown type specified as filter");
		case CollapserRule.SHIFT_RULE: return  shiftRules;
		case CollapserRule.DISCOUNT_RULE: return  discountRules;
		case CollapserRule.BOOST_RULE: return  boostRules;
		}
	}

}
