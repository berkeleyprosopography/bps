package edu.berkeley.bps.services.workspace;

import edu.berkeley.bps.services.common.LinkType;
import edu.berkeley.bps.services.common.utils.SortedQueue;

import java.util.HashMap;
import java.util.ArrayList;

/*
 * Models a set of links from objects of type O to Entity instances. Includes
 * convenience methods to adjust the weights of the links by the linked to
 * Entity, and to normalize the set of weights. Supports iteration over the set.
 */
public class EntityLinkSet<O> extends HashMap<Entity, EntityLink<O>> {

	private O fromObj = null;
	private LinkType.Type linkType = null;
	private double summedWeight;

	// TODO add a threshold to Ctor, and use it when normalizing to filter noise
	public EntityLinkSet(O fromObj, LinkType.Type linkType) {
		if(fromObj== null)
			throw new IllegalArgumentException("Must specify link base (fromObj).");
		this.fromObj = fromObj;
		this.linkType = linkType;
		summedWeight = 0;
	}
	
	public O getFromObj() {
		return fromObj;
	}

	public EntityLink<O> put(Entity toEntity, EntityLink<O> link) {
		if(get(toEntity)!= null)
			throw new IllegalArgumentException("Already have a link to Entity: "+toEntity);
		if(link.getFromObj()!= fromObj)
			throw new IllegalArgumentException("Link fromObject does not match set");
		if(link.getType()!=linkType)
			throw new IllegalArgumentException("Passed link type ("
					+link.getTypeString()+")does not match set type: "
					+LinkType.ValueToString(linkType));
		if(link!=null)
			summedWeight += link.getWeight();
		return super.put(toEntity, link);
	}

	/**
	 * @param fromObj
	 * @param toEntity
	 * @param weight
	 */
	public void updateLink(Entity toEntity, double weight) {
		EntityLink<O> link = get(toEntity);
		if(link == null)
			throw new IllegalArgumentException("No link found to Entity: "+toEntity);
		double delta = link.setWeight(weight);
		summedWeight += delta;
	}

	/**
	 * @param toEntity
	 * @param delta amount to add to the current weight for the link to toEntity
	 */
	public void adjustLink(Entity toEntity, double delta) {
		EntityLink<O> link = get(toEntity);
		if(link == null)
			throw new IllegalArgumentException("No link found to Entity: "+toEntity);
		link.adjustWeight(delta);
		summedWeight += delta;
	}

	/**
	 * @param toEntity
	 * @param scaleFacter factor to multiple the current weight by for the link 
	 * 					to toEntity. Must be >=0 and <= 1.
	 */
	public double scaleLink(Entity toEntity, double scaleFactor) {
		EntityLink<O> link = get(toEntity);
		if(link == null)
			throw new IllegalArgumentException("No link found to Entity: "+toEntity);
		double delta = link.scaleWeight(scaleFactor);
		summedWeight += delta;
		return delta;
	}

	/**
	 * Normalizes the weights
	 */
	public void verifySummedWeight() {
		double calcSum = 0;
		for(EntityLink<O> link:values()) {
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
		for(EntityLink<O> link:values()) {
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
		for(Entity entity:keySet()) {
			EntityLink<O> link = get(entity);
			double weight = link.getWeight();
			if(weight < belowWeight)
				put(entity, null);
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
		ArrayList<EntityLink<O>> pal = asSortedQueue().asArrayList();
		for(int ip=pal.size()-1; ip>0; ip--) {
			EntityLink<O> link = pal.get(ip);
			double weight = link.getWeight();
			if(weight < proportion) {
				put(link.getEntity(), null); // Remove from set
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
	public SortedQueue<EntityLink<O>> asSortedQueue() {
		SortedQueue<EntityLink<O>> pq = new SortedQueue<EntityLink<O>>(size());
		for(EntityLink<O> link:values()) {
			pq.add(link, link.getWeight());
		}
		return pq;
	}


}
