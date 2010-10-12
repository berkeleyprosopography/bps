package edu.berkeley.bps.services.graphbuilder;

import edu.berkeley.bps.services.common.LinkTypes;
import edu.berkeley.bps.services.common.utils.Pair;
import edu.berkeley.bps.services.common.utils.SortedQueue;
import java.util.HashMap;
import java.util.ArrayList;

/*
 * Models a set of links from objects of type O to Persons. Includes
 * convenience methods to adjust the weights of the links by the linked to
 * Person, and to normalize the set of weights. Supports iteration over the set.
 */
public class PersonLinkSet<O> extends HashMap<Person, PersonLink<O>> {

	private O fromObj = null;
	private LinkTypes linkType = null;
	private double summedWeight;

	public PersonLinkSet(O fromObj, LinkTypes linkType) {
		if(fromObj== null)
			throw new IllegalArgumentException("Must specify link base (fromObj).");
		this.fromObj = fromObj;
		this.linkType = linkType;
		summedWeight = 0;
	}

	/**
	 * @param fromObj
	 * @param toPerson
	 * @param weight
	 */
	public void addLink(Person toPerson, double weight) {
		if(get(toPerson)!= null)
			throw new IllegalArgumentException("Already have a link to Person: "+toPerson);
		PersonLink<O> link = new PersonLink<O>(fromObj, toPerson, weight, linkType);
		put(toPerson, link);
		summedWeight += weight;
	}

	/**
	 * @param fromObj
	 * @param toPerson
	 * @param weight
	 */
	public void updateLink(Person toPerson, double weight) {
		PersonLink<O> link = get(toPerson);
		if(link == null)
			throw new IllegalArgumentException("No link found to Person: "+toPerson);
		double delta = weight-link.getWeight();
		summedWeight += delta;
		link.adjustWeight(delta);
	}

	/**
	 * @param fromObj
	 * @param toPerson
	 * @param weight
	 */
	public void adjustLink(Person toPerson, double delta) {
		PersonLink<O> link = get(toPerson);
		if(link == null)
			throw new IllegalArgumentException("No link found to Person: "+toPerson);
		summedWeight += delta;
		link.setWeight(link.getWeight()+delta);
	}

	/**
	 * Normalizes the weights
	 */
	public void verifySummedWeight() {
		double calcSum = 0;
		for(PersonLink link:values()) {
			calcSum += link.getWeight();
		}
		if(calcSum!=summedWeight)
			throw new RuntimeException("Summed weight: "+summedWeight
					+" != calculated sum: "+calcSum);

	}

	/**
	 * Normalizes the weights
	 */
	public void normalize() {
		// TODO remove once debugged
		verifySummedWeight();
		for(PersonLink link:values()) {
			link.setWeight(link.getWeight()/summedWeight);
		}
		summedWeight = 1;
	}

	/**
	 * Filters all links below specified threshold weight
	 * @param belowWeight lower weight bound to keep link in set
	 */
	public void filterLinksBelowWeight(double belowWeight) {
		summedWeight = 0;
		for(Person person:keySet()) {
			PersonLink link = get(person);
			double weight = link.getWeight();
			if(weight < belowWeight)
				put(person, null);
			else
				summedWeight += weight;
		}
	}

	/**
	 * Filters links  with lowest weight until proportion of weight is removed.
	 * If no link as less than proportion of weight, none will be removed.
	 * Set must be normalized for this to function properly; this will throw
	 * a RuntimeException if the set is not normalized.
	 * @param proportion amount of weight to filter from set
	 */
	public void filterTailProportion(double proportion, boolean renormalize) {
		if(summedWeight!=1)
			throw new RuntimeException("Set not normalized.");
		ArrayList<PersonLink> pal = asSortedQueue().asArrayList();
		for(int ip=pal.size()-1; ip>0; ip--) {
			PersonLink link = pal.get(ip);
			double weight = link.getWeight();
			if(weight < proportion) {
				put(link.getPerson(), null); // Remove from set
				proportion -= weight;
				summedWeight -= weight;
			} else {
				break;	// We hit the limit
			}
		}
		if(renormalize)
			normalize();
	}

	/**
	 * Builds a priority queue whose items are the counter's items, and
	 * whose priorities are those items' counts in the counter.
	 */
	public SortedQueue<PersonLink> asSortedQueue() {
		SortedQueue<PersonLink> pq = new SortedQueue<PersonLink>(size());
		for(PersonLink link:values()) {
			pq.add(link, link.getWeight());
		}
		return pq;
	}


}
