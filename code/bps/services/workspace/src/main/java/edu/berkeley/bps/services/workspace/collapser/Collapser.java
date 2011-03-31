package edu.berkeley.bps.services.workspace.collapser;

import java.util.List;
import java.util.Map;

import edu.berkeley.bps.services.corpus.NameRoleActivity;
import edu.berkeley.bps.services.workspace.Entity;

public interface Collapser {

	/**
	 * Evaluates collapsing (shifting weight) among the entities.
	 * @param entities The list of entities to consider.
	 * @param entityToNRADLinks map indexed by Entity id of lists of NRADs linked to the Entity
	 * @param intraDocument if true, this is within a document, else this is corpora-wide
	 */
	public void evaluateList(List<? extends Entity> entities, 
			Map<Integer, NameRoleActivity> entityToNRADLinks, 
			boolean intraDocument);
	
	/**
	 * Adds a rule to this Collapser. 
	 * @param rule the new rule to add
	 * @param insertBefore set to null to add at the end, otherwise, pass the name
	 * 				of a rule before which this should be added.
	 */
	public void addRule(CollapserRule rule, String insertBefore);

	/**
	 * Return the rules for this Collapser
	 * @param typeFilter if <=0, returns all rules. Otherwise, filters to rules
	 * 					matching this type. 
	 * @return list of rules
	 * @see edu.berkeley.bps.services.workspace.collapser.CollapserRule#getType()
	 */
	public List<CollapserRule> getRules(int typeFilter);

}
