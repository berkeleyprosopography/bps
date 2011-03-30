package edu.berkeley.bps.services.workspace.collapser;

import edu.berkeley.bps.services.workspace.Entity;

public interface ShiftRule extends CollapserRule {

	/**
	 * Evaluates first and second, for shifting weight from one to the other.
	 * @param first First entity to consider
	 * @param second Second entity to consider
	 * @return 0 if this rule does not apply to the pair,
	 * 			-1 <= value <  0 to shift a proportion from second to first 
	 * 			0  <  value <= 1 to shift a proportion from first to second
	 */
	public float evaluate(Entity first, Entity second);

}
