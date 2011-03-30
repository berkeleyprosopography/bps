package edu.berkeley.bps.services.workspace.collapser;

import edu.berkeley.bps.services.workspace.Entity;

public abstract class ShiftRule implements CollapserRule {

	/**
	 * Evaluates shifting weight from fromEntity into toEntity.
	 * Will scale any evaluation based upon the current value of weight.
	 * @param first First entity to consider
	 * @param second Second entity to consider
	 * @return 0 if this rule does not apply to the pair,
	 * 			-1 <= value <  0 to shift a proportion from second to first 
	 * 			0  <  value <= 1 to shift a proportion from first to second
	 */
	public abstract float evaluate(Entity fromEntity, Entity toEntity);

}
