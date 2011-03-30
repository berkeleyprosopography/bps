package edu.berkeley.bps.services.workspace.collapser;

import edu.berkeley.bps.services.workspace.Entity;

public abstract class BoostRule implements CollapserRule {

	/**
	 * Allows a rule to boost a (non-zero) weight shift
	 * Will scale any evaluation based upon the current value of weight.
	 * @param first First entity to consider
	 * @param second Second entity to consider
	 * @return 1 if this rule does not apply to the pair,
	 * 			value >  1 to boost any shift that has been computed. 
	 */
	public abstract float evaluate(Entity fromEntity, Entity toEntity);

}
