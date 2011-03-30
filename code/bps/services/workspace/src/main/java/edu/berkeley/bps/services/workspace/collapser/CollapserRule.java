package edu.berkeley.bps.services.workspace.collapser;

import edu.berkeley.bps.services.workspace.Entity;

public interface CollapserRule {

	/**
	 * Evaluates collapsing (shifting weight) from fromEntity into toEntity.
	 * Will scale any evaluation based upon the current value of weight.
	 * @param first First entity to consider
	 * @param second Second entity to consider
	 * @return 0 if this rule does not apply to the pair,
	 * 			else a value that depends upon the class of rule 
	 */
	public float evaluate(Entity fromEntity, Entity toEntity);

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
	 * @return the current weight to give this rule.
	 */
	public float getWeight();
	
	/**
	 * Sets the current weight to give this rule. Set to 0 to disable the rule, 
	 * set to 1 for complete confidence in the rule, or some value between to
	 * correspond with the confidence or weight to give this rule.
	 * @param weight must be in the range of 0 to 1
	 * @throws IllegalArgumentException if weight < 0 or weight > 1
	 */
	public void setWeight(float weight);
	
}
