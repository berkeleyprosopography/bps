package edu.berkeley.bps.services.workspace.collapser;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import edu.berkeley.bps.services.corpus.NameRoleActivity;
import edu.berkeley.bps.services.workspace.Entity;
import edu.berkeley.bps.services.workspace.Person;

public class PersonCollapser extends CollapserBase implements Collapser {

	public static class EntitySortByNQuals implements Comparator<Entity> {
		public int compare(Entity p1, Entity p2) {
			// Note that we invert the normal subtraction direction to 
			// get a descending sort.
			return p2.getNumQualifiers()-p1.getNumQualifiers();
		}
	}
	
	/**
	 * Evaluates collapsing (shifting weight) among the entities.
	 * @param entities The list of persons to consider.
	 * @param entityToNRADLinks map indexed by Entity id of lists of NRADs linked to the Entity
	 * @param intraDocument if true, this is within a document, else this is corpora-wide
	 */
	@Override
	public void evaluateList(List<? extends Entity> entities, 
			Map<Integer, NameRoleActivity> entityToNRADLinks, 
			boolean intraDocument) {
		if(entities==null || entities.isEmpty())
			throw new IllegalArgumentException("No Persons to collapse");
		if(entityToNRADLinks==null || entityToNRADLinks.isEmpty())
			throw new IllegalArgumentException("Missing/invalid entityToNRADLinks map");

		// First, sort the list by #qualifications, descending
		Collections.sort(entities, new EntitySortByNQuals());
		
		try {
			int nPersons = entities.size();
			for(int iToPers=0; iToPers<nPersons;iToPers++) {
				Person toPerson = (Person)entities.get(iToPers);
				// Consider this Person against all the following ones.
				for(int iFromPers=iToPers+1; iFromPers<nPersons;iFromPers++) {
					Person fromPerson = (Person)entities.get(iFromPers);
					// Now, we run through the rules. 
					// First try the shift rules, taking the
					// first match we get - we should only get one.
					double totalShift = 0;
					for(CollapserRule rule:shiftRules) {
						double shift = rule.evaluate(fromPerson, toPerson);
						if(shift!=0) {	// we have a match. Can go either direction.
							if(shift<-1 || shift>1)
								throw new RuntimeException(
										"Shift CollapserRule "+rule.getName()
										+"returned value out of range:"+shift);
							totalShift = shift;
							break;
						}
					}
					if(totalShift!=0) {	// any match in the set?
						// Look for matching discount rules, using all that match
						for(CollapserRule rule:discountRules) {
							double discount = rule.evaluate(fromPerson, toPerson);
							if(discount==0) {	// we have a match
								totalShift = 0;
								break;			// We can stop here
							} else if(discount>0) {		// match
								if(discount>1)
									throw new RuntimeException(
											"Discount CollapserRule "+rule.getName()
											+"returned value out of range:"+discount);
								totalShift *= discount;
							} // if < 0, indicates no match
						}
						if(totalShift!=0) { // Any shift left?
							// Look for matching boost rules, using all that match
							for(CollapserRule rule:boostRules) {
								double boost = rule.evaluate(fromPerson, toPerson);
								if(boost > 1) {	// we have a match
									totalShift *= boost;
								} else if(boost<1) { // bad value
									throw new RuntimeException(
												"Boost CollapserRule "+rule.getName()
												+"returned value out of range:"+boost);
								} // if == 1, indicates no match or no action
							}
						}
					}
					if(totalShift!=0) {	// any shift to do?
						// Perform the shift on the nrad links
					}
				}
			}
		} catch(ClassCastException cce) {
			throw new RuntimeException(
				"Apparent attempt to use PersonCollapser with non-Person entities."+cce);
		}
	}

}
