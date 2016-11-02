package edu.berkeley.bps.services.workspace.collapser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.RuntimeErrorException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	static final Logger logger = LoggerFactory.getLogger(PersonCollapser.class);

	public PersonCollapser() {
		super();
	}

	public PersonCollapser(CollapserBase base) {
		super(base);
	}

	public static class EntitySortByNQuals implements Comparator<Entity> {
		boolean ascending = false;
		public EntitySortByNQuals(boolean asc) {
			ascending = asc;
		}
		public int compare(Entity p1, Entity p2) {
			int diff = (p1.getNumQualifiers()-p2.getNumQualifiers());
			if(!ascending)
				diff *= -1;
			return diff;
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
			HashMap<Person, List<EntityLinkSet<NameRoleActivity>>> personToEntityLinkSets,
			boolean intraDocument) {
		if(entities==null || entities.isEmpty())
			throw new IllegalArgumentException("No Persons to collapse");

		// First, sort the list by #qualifications, ascending
		Collections.sort(entities, new EntitySortByNQuals(true));
		
		/*
		 * We loop over the Persons with a basic name match, and consider rules
		 * to find matches for shifting weight. 
		 * Given that set of matches, we then compute:
		 * 	1) TotalShift = 1-Product(1-shift for each match)
		 *		Above takes the product of the remainders after the shifts, 
		 *		subtracts from 1 to get total shift
		 *	2) ProportionalShift = rule-shift/(sum(rule shifts for all matches))
		 *		Compute the proportion of the total shift, for each match
		 *	3) Shift per rule = ProportionalShift * TotalShift
		 *		Compute the final shift from the proportion and the total shift.
		 *	This allows us to have 3 matches that sum to more than 100%, and get a reasonable
		 *	value for each rule.  
		 */
		
		try {
			int nPersons = entities.size();
			double[] personShifts = new double[nPersons]; 
			for(int iFromPers=0; iFromPers<nPersons-1;iFromPers++) {	// Loop over n-1 persons to get pairs
				Person fromPerson = (Person)entities.get(iFromPers);
				int nTotalPersonsWithShift = 0;
				// We need to sum the shifts for each person, to compute the proportional shift for each 
				double sumOfShifts = 0;
				// We need to compute the Total shift, from the product of shift remainders 
				double productOfShiftRemainders = 1;
				Arrays.fill(personShifts, 0);	// Reset to 0 for each outer loop
				// Consider this Person against all the following (more qualified) ones.
				for(int iToPers=iFromPers+1; iToPers<nPersons;iToPers++) {
					Person toPerson = (Person)entities.get(iToPers);
					logger.trace("Collapser considering from {} to {}",
							fromPerson.getDisplayName(),toPerson.getDisplayName());
					// First, if we are within a doc and either person is an ancestor
					// of the other, skip any consideration of shifting. 
					// We use the originalNRAD for this. 
					// Thus, Joe son-of Bob son-of Joe, should skip trying to
					// collapse the two Joes. 
					{
						NameRoleActivity fromNRAD = fromPerson.getOriginalNRAD();
						NameRoleActivity toNRAD = toPerson.getOriginalNRAD();
						if(intraDocument) {
							if(fromNRAD.hasFamilyLinkFor(toNRAD) ||
									toNRAD.hasFamilyLinkFor(fromNRAD))
								continue;
						} else {	// Inter doc pass (looking for matches in different docs)
							// If from and to have base NRADS that share a doc, skip as we 
							// have already considered them in the intraDocument pass
							if(fromNRAD.getDocumentId() == toNRAD.getDocumentId())
								continue;
						}
					}
					// Now, we run through the rules. 
					// First try the SHIFT rules, taking the
					// best match (highest weight shift) we get - we may get more than one
					double netShift = 0;
					List<CollapserRuleBase> ruleList = 
						getRules(CollapserRule.SHIFT_RULE, intraDocument);
					for(CollapserRule rule:ruleList) {
						double shift = rule.evaluate(fromPerson, toPerson);
						if(shift!=0) {	// we have a match. Can go either direction.
							logger.trace("Shift Rule: {} shifts:{} from {} to {}",
									new Object[] { rule.getName(), shift, fromPerson.getDisplayName(),toPerson.getDisplayName()});
							if(shift<-1 || shift>1)
								throw new RuntimeException(
										"Shift CollapserRule "+rule.getName()
										+"returned value out of range:"+shift);
							if(shift < 0)
								logger.debug("Shift Rule: {} returned a negative shift!",rule.getName());
							// TODO If we can prove that we get no negative shifts, then we can remove the Math.abs calls
							if(Math.abs(shift)>Math.abs(netShift))
								netShift = shift;
							// break;	Keep going to find the biggest shift
						}
					}
					if(netShift!=0) {	// any match in the SHIFT set?
						// Look for matching DISCOUNT rules, accumulating all that match
						ruleList = 
							getRules(CollapserRule.DISCOUNT_RULE, intraDocument);
						for(CollapserRule rule:ruleList) {
							double discount = rule.evaluate(fromPerson, toPerson);
							if(discount==0) {	// we have a match, with a complete rejection of the two names
								logger.trace("Discount Rule: {} discounts ALL from {} to {}",
										new Object[] { rule.getName(), fromPerson.getDisplayName(),toPerson.getDisplayName()});
								netShift = 0;
								break;			// We can stop here
							} else if(discount>0) {		// match
								if(discount>1)	// Discounts should be between 0 and 1 (inclusive)
									throw new RuntimeException(
											"Discount CollapserRule "+rule.getName()
											+"returned value out of range:"+discount);
								netShift *= discount;
								logger.trace("Discount Rule: {} discounts:{} for from {} to {}",
									new Object[] { rule.getName(), discount, fromPerson.getDisplayName(),toPerson.getDisplayName()});
							} // if < 0, indicates no match
						}
						if(netShift!=0) { // Any shift left to discount? Skip if already 0
							// Look for matching BOOST rules, accumulating all that match
							ruleList = 
								getRules(CollapserRule.BOOST_RULE, intraDocument);
							for(CollapserRule rule:ruleList) {
								double boost = rule.evaluate(fromPerson, toPerson);
								if(boost > 1) {	// we have a match
									// Apply the boost, but max out the net shift at 1
									logger.trace("Boost Rule: {} boosts:{} for from {} to {}",
											new Object[] { rule.getName(), boost, fromPerson.getDisplayName(),toPerson.getDisplayName()});
									netShift = Math.min(1, netShift*boost);
								} else if(boost<1) { // bad value
									throw new RuntimeException(
												"Boost CollapserRule "+rule.getName()
												+"returned value out of range:"+boost);
								} // if == 1, indicates no match or no action
							}
							// Dates are a special case of the DISCOUNT rule.
							if(netShift!=0) { // Any shift left?
								// Consider the dates.
								double likelihood = 
									fromPerson.getDateOverlapLikelihood(toPerson);
								netShift *= likelihood;
								logger.trace("Date likelihood:{} for from {} and {}",
										new Object[] { likelihood, fromPerson.getDisplayName(),toPerson.getDisplayName()});
							}
						}
					}
					
					logger.trace("Net shift:{} for from {} and {}",
							new Object[] { netShift, fromPerson.getDisplayName(),toPerson.getDisplayName()});
					// All the shifts should start from the same original sets of weights, 
					// so we cannot shift yet - we have to consider all the matches for this fromPerson,
					// and then compute a distribution among the various rules for each toPerson match.
					// So we keep track of the shift (based upon the matching rules) for each fromPerson
					if(netShift!=0) {	// any shift to do?
						// Hold this shift for this person
						personShifts[iToPers] = netShift;
						nTotalPersonsWithShift++;
						sumOfShifts += netShift;
						productOfShiftRemainders *= (1-netShift); 
					}
				}
				// TODO Need to think about how to trim long tail matches.
				// Consider removing matches with a shift proportion that is less than
				// an absolute threshold (e.g., 3%), and also less than ~50% of the average.
				// The latter constraint keeps us from trimming when there are many
				// matches of equal weight (i.e., small proportions because of a large divisor).
				// Have to remove these from the list, and then recompute the sum and product
				// before proceeding to compute the finalShift for those shifts not trimmed.

				// Now we handle all the accumulated shifts
				if(nTotalPersonsWithShift > 0) {		// Any matches at all?
					double totalShiftForAllMatches = 1 - productOfShiftRemainders;
					List<EntityLinkSet<NameRoleActivity>> linkSetsForFromPerson = 
							personToEntityLinkSets.get(fromPerson);
					if(linkSetsForFromPerson==null||linkSetsForFromPerson.isEmpty()) {
						throw new RuntimeException(myClass+
								".evaluateList: linkSets for fromPerson is null or emtpy");
					}
					// For each link set that includes fromPerson, we need to scale the total shift
					// for that fromLink, and then shift proportionately to each toPerson
					for(EntityLinkSet<NameRoleActivity> fromLinkSet:linkSetsForFromPerson) {
						// If we are shifting 70% of weight, we scale current link to 30% (1-70%) of weight
						// It may not be 1 now, so we get back the amount of weight actually reduced
						double remainderAfterTotalShift = 1 - totalShiftForAllMatches;
						// scaleLinkWeight returns the resulting delta, given the scale 
						double totalShiftForAllMatchesScaledToFrom = 
								fromLinkSet.scaleLinkWeight(fromPerson, remainderAfterTotalShift);
						// Since the shift will return a negative delta, convert it back to a positive shift.
						totalShiftForAllMatchesScaledToFrom *= -1;
						
						if(totalShiftForAllMatchesScaledToFrom > 0)	{ // From may have no more weight - skip
							// Consider all the toPersons in the link set, and if find a match from above loop, 
							// do the shift
							for(int iToPers=iFromPers+1; iToPers<nPersons;iToPers++) {
								Person toPerson = (Person)entities.get(iToPers);
								if(personShifts[iToPers]!=0) { // If this is in the match set
									// Compute the final shift
									double proportionalShift = personShifts[iToPers]/sumOfShifts;
									double finalShift = proportionalShift * totalShiftForAllMatchesScaledToFrom;
									// Consider all the nrads that point to this toPerson
									List<EntityLinkSet<NameRoleActivity>> linkSetsForToPerson = 
											personToEntityLinkSets.get(toPerson);
									if(linkSetsForToPerson==null||linkSetsForToPerson.isEmpty()) {
										throw new RuntimeException(myClass+
												".evaluateList: linkSets for toPerson is null or emtpy");
									}
									// Is there already a link to the toPerson?
									NRADEntityLink link = (NRADEntityLink)fromLinkSet.get(toPerson);
									if(link!=null) { // If so, just bump that link
										fromLinkSet.adjustLink(toPerson, finalShift);
									} else {	// Otherwise create a new link with the shifted weight
										link = new NRADEntityLink(fromLinkSet.getFromObj(), toPerson, finalShift,
																LinkType.Type.LINK_TO_PERSON);
										fromLinkSet.put(toPerson, link);
										// We need to add this linkSet to the List for toPerson 
										linkSetsForToPerson.add(fromLinkSet);
									}

								}
							}
						}
						// Now that we have completed the shifts we can normalize.
						// If we have done things right, this should be a no-op, since we
						// removed the total weight from the fromPerson, and added back all that
						// weight as links to the various toPersons.
						fromLinkSet.normalize();
					}

				}
			}
		} catch(ClassCastException cce) {
			throw new RuntimeException(
				"Apparent attempt to use PersonCollapser with non-Person entities."+cce);
		}
	}

}
