package edu.berkeley.bps.services.workspace.collapser;

import java.util.Map;

import edu.berkeley.bps.services.workspace.Entity;

public interface CollapserRule {

	/**
	 * Evaluates first and second for collapsing.
	 * @param first First entity to consider
	 * @param second Second entity to consider
	 * @return 0 if this rule does not apply to the pair,
	 * 			else a value that depends upon the class of rule 
	 */
	public float evaluate(Entity first, Entity second);

	/**
	 * @return the configured name for this rule
	 */
	public String getName();
	
	/**
	 * Sets the configured name for this rule
	 * @param name
	 */
	public void setName(String name);
	
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
	 * @return a user-readable description of this rule.
	 */
	public Map<String, Float> getUserSettings();

	
}
