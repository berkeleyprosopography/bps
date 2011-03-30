package edu.berkeley.bps.services.workspace.collapser;

import java.util.List;
import edu.berkeley.bps.services.common.utils.Pair;

public interface CollapserRuleUI {

	/**
	 * @return a user-readable description of this rule.
	 */
	public String getDescription();
	
	/**
	 * Sets the user-readable description for this rule
	 * @param description
	 */
	public void setDescription(String description);
	
	/**
	 * Provides a list of names and values that can be used in a SELECT
	 * or equivalent list of options for the weight to use on a CollapserRule
	 * @return a list of named values for the weight of this rule,
	 * 			or null, if this rule UI does not support such a model. 
	 */
	public List<Pair<String, Float>> getUserSettings();

	
}
