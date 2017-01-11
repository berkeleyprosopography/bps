package edu.berkeley.bps.services.workspace.collapser;

import java.util.List;

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
	 * @return a group name for this rule. Matches the name of a Collapser.CollapserUIGroup.
	 */
	public String getUIGroup();
	
	/**
	 * Sets the group name for this rule. Matches the name of a Collapser.CollapserUIGroup.
	 * @param uiGroupName
	 */
	public void setUIGroup(String uiGroupName);
	
	/**
	 * Provides a list of names and values that can be used in a SELECT
	 * or equivalent list of options for the weight to use on a CollapserRule
	 * @return a list of named values for the weight of this rule,
	 * 			or null, if this rule UI does not support such a model. 
	 */
	public List<UserWeightSetting> getUserSettingsForWeight();

	
}
