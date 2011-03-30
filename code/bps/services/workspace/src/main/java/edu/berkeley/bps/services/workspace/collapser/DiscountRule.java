package edu.berkeley.bps.services.workspace.collapser;

import edu.berkeley.bps.services.workspace.Entity;

public abstract class DiscountRule implements CollapserRule {

	/**
	 * Allows a rule to discount a (non-zero) weight shift
	 * Will scale any evaluation based upon the current value of weight.
	 * A return value of 0 will preclude the collapse. 
	 * @param first First entity to consider
	 * @param second Second entity to consider
	 * @return 1 if this rule does not apply to the pair,
	 * 			0 >= value < 1 to discount any shift that has been computed. 
	 */
	public abstract float evaluate(Entity fromEntity, Entity toEntity);

}
