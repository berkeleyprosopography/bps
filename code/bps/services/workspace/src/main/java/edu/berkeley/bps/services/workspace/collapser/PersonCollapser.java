package edu.berkeley.bps.services.workspace.collapser;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.RuntimeErrorException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import edu.berkeley.bps.services.common.LinkType;
import edu.berkeley.bps.services.corpus.NameRoleActivity;
import edu.berkeley.bps.services.workspace.Entity;
import edu.berkeley.bps.services.workspace.EntityLinkSet;
import edu.berkeley.bps.services.workspace.NRADEntityLink;
import edu.berkeley.bps.services.workspace.Person;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public class PersonCollapser extends CollapserBase implements Collapser {
	private final static String myClass = "PersonCollapser";
	
	public PersonCollapser() {
		super();
	}

	public PersonCollapser(CollapserBase base) {
		super(base);
	}

	public static class EntitySortByNQuals implements Comparator<Entity> {
		public int compare(Entity p1, Entity p2) {
			// Note that we invert the normal subtraction direction to 
			// get a descending sort.
			return p2.getNumQualifiers()-p1.getNumQualifiers();
		}
	}
	
	// TODO We need to hold the shifts for each entity as actions, 
	// as a list attached to the from entity, and with a rule association.
	// Where there are multiple shifts for a given rule, we need to divide
	// the shift by the number of matches. This means that we need to ALSO
	// keep track of any discounts applied, and any boosts applied, but 
	// hold them separate until we split the shift weight. Once we complete 
	// all the pairs in the list, we then do this shift-splitting, apply the
	// discount/boost weights, and make the shifts happen. An exception is if 
	// we get a discount of 0, then we discard that shift and do not consider 
	// it any further. 
	// TODO - if we shift all the weight away from an NRAD, do not shift weight
	// to it later. Also, if we shift all weight away from a Person (no linked
	// nrads with any weight), then we stop considering that Person. Mark it
	// as GONE. 
	// TODO - implement the weight threshold: if after shifting (and normalizing),
	// the weight associated to any link is below the threshold, 0 it. This
	// must be refined so that we gather sets of links with equal weights and
	// treat them all the same. This argues for a total weight threshold, rather
	// than an individual amount threshold. I.e., We order by weight descending,
	// then we loop and add weight for each. Each time we see a new weight from the
	// last one, we consider the total, and if it exceeds the threshold, we toss the
	// rest. That way, if we have a threshold of 10%, but have lots of links at
	// 10%, we will not toss out a huge chunk of weight at once. We can then 
	// put the threshold at something like 90% or even 80%, and filter out noise.
	// We should probably work from the NRADs, since they have to piont to something
	// and we want to remove their misc links.
	/**
	 * Evaluates collapsing (shifting weight) among the entities.
	 * Evaluates collapsing (shifting weight) among the entities.
	 * @param entities The list of entities that are being compared to one another
	 * @param nradToEntityLinks Map indexed by nrad, of the sets of weights 
	 * 				to persons or clans
	 * @param personTopersonToEntityLinkSets Map indexed by personId, of lists of 
	 * 				EntityLinkSets for the NRADs that point to this person
	 * @param intraDocument true if evaluating pairs within a document, 
	 * 				false if this is corpora-wide
	 */
	@Override
	public void evaluateList(List<? extends Entity> entities, 
			HashMap<Integer, EntityLinkSet<NameRoleActivity>> nradToEntityLinks, 
			HashMap<Person, List<EntityLinkSet<NameRoleActivity>>> personToEntityLinkSets,
			boolean intraDocument) {
		if(entities==null || entities.isEmpty())
			throw new IllegalArgumentException("No Persons to collapse");
		if(nradToEntityLinks==null || nradToEntityLinks.isEmpty())
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
					List<CollapserRuleBase> ruleList = 
						getRules(CollapserRule.SHIFT_RULE, intraDocument);
					for(CollapserRule rule:ruleList) {
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
						// normalize the shift direction for simplicity
						if(totalShift<0) {
							Person temp = fromPerson;
							fromPerson = toPerson;
							toPerson = temp;
							totalShift = -totalShift;
						}
						// Look for matching discount rules, using all that match
						ruleList = 
							getRules(CollapserRule.DISCOUNT_RULE, intraDocument);
						for(CollapserRule rule:ruleList) {
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
							ruleList = 
								getRules(CollapserRule.BOOST_RULE, intraDocument);
							for(CollapserRule rule:ruleList) {
								double boost = rule.evaluate(fromPerson, toPerson);
								if(boost > 1) {	// we have a match
									// Apply the boost, but max out at 1
									totalShift = Math.min(1, totalShift*boost);
								} else if(boost<1) { // bad value
									throw new RuntimeException(
												"Boost CollapserRule "+rule.getName()
												+"returned value out of range:"+boost);
								} // if == 1, indicates no match or no action
							}
							if(totalShift!=0) { // Any shift left?
								// Consider the dates.
								double likelihood = 
									fromPerson.getDateOverlapLikelihood(toPerson);
								totalShift *= likelihood;
							}
						}
					}
					if(totalShift!=0) {	// any shift to do?
						// Perform the shift on the nrad links
						handleShift( fromPerson, toPerson, totalShift,
								nradToEntityLinks, 
								personToEntityLinkSets );
					}
				}
			}
		} catch(ClassCastException cce) {
			throw new RuntimeException(
				"Apparent attempt to use PersonCollapser with non-Person entities."+cce);
		}
	}
	
	protected void handleShift( Person fromPerson, Person toPerson, double shift,
			HashMap<Integer, EntityLinkSet<NameRoleActivity>> nradToEntityLinks, 
			HashMap<Person, List<EntityLinkSet<NameRoleActivity>>> personToEntityLinkSets ) {
		// We run through all the NRADs that point to fromPerson,
		// reduce their current weight by 1-shift (multiply by 1-shift).
		// Then for each of those NRADs:
		//   If toPerson does not already have a link from the same NRAD,
		//   the first create a link and insert it into the LinkSet for toPerson.
		//   Increase (or set, if new) the weight on the NRAD to toPerson link.
		// Then normalize the linkSets for both fromPerson and toPerson
		if(shift<=0||shift>1)
			throw new IllegalArgumentException(
					"Shift value for PersonCollapser.handleShift out of range (0-1]: "+shift);
		List<EntityLinkSet<NameRoleActivity>> linkSetsForFromPerson = 
			personToEntityLinkSets.get(fromPerson);
		if(linkSetsForFromPerson==null||linkSetsForFromPerson.isEmpty()) {
			throw new RuntimeException(myClass+
					".handleShift: linkSets for fromPerson is null or emtpy");
		}
		List<EntityLinkSet<NameRoleActivity>> linkSetsForToPerson = 
			personToEntityLinkSets.get(toPerson);
		if(linkSetsForToPerson==null||linkSetsForToPerson.isEmpty()) {
			throw new RuntimeException(myClass+
					".handleShift: linkSets for toPerson is null or emtpy");
		}
		// For each set that includes fromPerson
		for(EntityLinkSet<NameRoleActivity> linkSet:linkSetsForFromPerson) {
			// scale the link to the fromPerson - returns the weight shifted.
			// delta will always be negative, since we are reducing fromPerson weight.
			// convert it to the positive delta we will add to toPerson
			double delta = linkSet.scaleLink(fromPerson, 1-shift);
			// If delta is 0, there was no weight left to shift, so we are done.
			if(delta!=0) {
				delta *= -1;
				// Now we shift that to the toPerson. If toPerson is in linkSet, 
				// just adjust it. Otherwise, create a new link.
				NRADEntityLink link = (NRADEntityLink)linkSet.get(toPerson);
				if(link!=null) {
					linkSet.adjustLink(toPerson, delta);
				} else {
					link = new NRADEntityLink(linkSet.getFromObj(), toPerson, delta,
											LinkType.Type.LINK_TO_PERSON);
					linkSet.put(toPerson, link);
					// We need to add this linkSet to the List for toPerson 
					linkSetsForToPerson.add(linkSet);
				}
			}
		}
		
	}

}
