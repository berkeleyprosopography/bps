package edu.berkeley.bps.services.workspace.collapser;

import edu.berkeley.bps.services.workspace.Entity;

public interface CollapserRule {

	public static final int SHIFT_RULE = 1;
	public static final int DISCOUNT_RULE = 2;
	public static final int BOOST_RULE = 3;
	
	/**
	 * Evaluates collapsing (shifting weight) from fromEntity into toEntity.
	 * Will scale any evaluation based upon the current value of weight.
	 * @param first First entity to consider
	 * @param second Second entity to consider
	 * @return 0 if this rule does not apply to the pair,
	 * 			else a value that depends upon the type of rule
	 * 			SHIFT rules must return a value from -1 to 1 
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
	public double getWeight();
	
	/**
	 * Sets the current weight to give this rule. Set to 0 to disable the rule, 
	 * set to 1 for complete confidence in the rule, or some value between to
	 * correspond with the confidence or weight to give this rule.
	 * @param weight must be greater than 0
	 * @throws IllegalArgumentException if weight < 0
	 */
	public void setWeight(double weight);
	
	/**
	 * @return the defined type for this rule - one of SHIFT_RULE, DISCOUNT_RULE, BOOST_RULE
	 */
	public int getType();

	/**
	 * @return true, if this rule applies within documents
	 */
	public boolean appliesWithinDocument();
	
	/**
	 * @return true, if this rule applies across documents in a corpus
	 */
	public boolean appliesAcrossCorpus();
	
}
