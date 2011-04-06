package edu.berkeley.bps.services.workspace.collapser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.berkeley.bps.services.corpus.NameRoleActivity;
import edu.berkeley.bps.services.workspace.Entity;
import edu.berkeley.bps.services.workspace.EntityLinkSet;
import edu.berkeley.bps.services.workspace.Person;

public interface Collapser {

	/**
	 * Evaluates collapsing (shifting weight) among the entities.
	 * @param entities The list of entities that are being compared to one another
	 * @param nradToEntityLinks Map indexed by nrad, of the sets of weights 
	 * 				to persons or clans
	 * @param personTopersonToEntityLinkSets Map indexed by personId, of lists of 
	 * 				EntityLinkSets for the NRADs that point to this person
	 * @param intraDocument true if evaluating pairs within a document,
	 * 				false if this is corpora-wide
	 */
	public void evaluateList(List<? extends Entity> entities, 
			HashMap<Integer, EntityLinkSet<NameRoleActivity>> nradToEntityLinks, 
			HashMap<Person, List<EntityLinkSet<NameRoleActivity>>> personTopersonToEntityLinkSets,
			boolean intraDocument);
	
	/**
	 * Adds a rule to this Collapser, at the end of the list 
	 * @param rule the new rule to add
	 */
	public void addRule(CollapserRule rule);

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
	 * @param intraDocument if true, return rules that apply within documents, else
	 * 				return rules that apply corpus-wide
	 * @return list of rules
	 * @see edu.berkeley.bps.services.workspace.collapser.CollapserRule#getType()
	 */
	public List<CollapserRule> getRules(int typeFilter, boolean intraDocument);

}
