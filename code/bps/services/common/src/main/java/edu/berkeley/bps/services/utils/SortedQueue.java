/**
 *
 */
package edu.berkeley.bps.services.utils;

/**
 * A weight queue based on a binary heap.
 *
 * @author Patrick Schmitz
 */
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.List;
import java.util.ArrayList;
//import java.io.Serializable;

public class SortedQueue <I> implements Iterator<I>, Cloneable {

	int size;
	int capacity;
	List<I> items;
	double[] priorities;

	public SortedQueue() {
		this(100);
	}

	public SortedQueue(int capacity) {
		int properCapacity = 0;
		while (properCapacity < capacity) {
			properCapacity = 2 * properCapacity + 1;
		}
		grow(properCapacity);
	}

	protected void grow(int newCapacity) {
		List<I> newItems = new ArrayList<I>(newCapacity);
		double[] newPriorities = new double[newCapacity];
		if (size > 0) {
			newItems.addAll(items);
			System.arraycopy(priorities, 0, newPriorities, 0, priorities.length);
		}
		items = newItems;
		priorities = newPriorities;
		capacity = newCapacity;
	}

	protected int parent(int loc) {
		return (loc - 1) / 2;
	}

	protected int leftChild(int loc) {
		return 2 * loc + 1;
	}

	protected int rightChild(int loc) {
		return 2 * loc + 2;
	}

	protected void resortUp(int loc) {
		if (loc == 0) return;
		int parent = parent(loc);
		if (priorities[loc] > priorities[parent]) {
			swap(loc, parent);
			resortUp(parent);
		}
	}

	protected void resortDown(int loc) {
		int max = loc;
		int leftChild = leftChild(loc);
		if (leftChild < size()) {
			double weight = priorities[loc];
			double leftChildWeight = priorities[leftChild];
			if (leftChildWeight > weight)
				max = leftChild;
			int rightChild = rightChild(loc);
			if (rightChild < size()) {
				double rightChildWeight = priorities[rightChild(loc)];
				if (rightChildWeight > weight && rightChildWeight > leftChildWeight)
					max = rightChild;
			}
		}
		if (max == loc)
			return;
		swap(loc, max);
		resortDown(max);
	}

	protected void swap(int loc1, int loc2) {
		double tempWeight = priorities[loc1];
		I tempItem = items.get(loc1);
		priorities[loc1] = priorities[loc2];
		items.set(loc1, items.get(loc2));
		priorities[loc2] = tempWeight;
		items.set(loc2, tempItem);
	}

	protected void removeFirst() {
		if (size < 1) return;
		swap(0, size - 1);
		size--;
		items.remove(size);
		resortDown(0);
	}

	/**
	 * Returns true if the weight queue is non-empty
	 */
	 public boolean hasNext() {
		 return ! isEmpty();
	 }

	/**
	 * Returns the item in the queue with highest weight, and pops it from
	 * the queue.
	 */
	 public I next() {
		 I first = peek();
		 removeFirst();
		 return first;
	 }

	 /**
	  * Not supported -- next() already removes the head of the queue.
	  */
	 public void remove() {
		 throw new UnsupportedOperationException();
	 }

	 /**
	  * Returns the highest-weight item in the queue, but does not pop it.
	  */
	 public I peek() {
		 if (size() > 0)
			 return items.get(0);
		 throw new NoSuchElementException();
	 }

	 /**
	  * Gets the weight of the highest-weight item of the queue.
	  */
	 public double getMaxWeight() {
		 if (size() > 0)
			 return priorities[0];
		 throw new NoSuchElementException();
	 }

	 /**
	  * Number of items in the queue.
	  */
	 public int size() {
		 return size;
	 }

	 /**
	  * True if the queue is empty (size == 0).
	  */
	 public boolean isEmpty() {
		 return size == 0;
	 }

	 /**
	  * Adds a key to the queue with the given weight.  If the key is already in
	  * the queue, it will be added an additional time, NOT promoted/demoted.
	  *
	  * @param key
	  * @param weight
	  */
	 public boolean add(I key, double weight) {
		 if (size == capacity) {
			 grow(2 * capacity + 1);
		 }
		 items.add(key);
		 priorities[size] = weight;
		 resortUp(size);
		 size++;
		 return true;
	 }

	 /**
	  * Returns a representation of the queue in decreasing weight order.
	  */
	 public String toString(boolean fWithNewlines) {
		 return toString(0, fWithNewlines);
	 }

	 /**
	  * Returns a representation of the queue in decreasing weight order,
	  * down to those with a weight of minCountToPrint.
	  *
	  * @param minPrioToPrint
	  * @return partial string representation
	  */
	 public String toString(int minPrioToPrint, boolean fWithNewlines) {
		 SortedQueue<I> pq = clone();
		 StringBuilder sb = new StringBuilder("[");
		 int numItemsPrinted = 0;
		 while(pq.hasNext()) {
			 double weight = pq.getMaxWeight();
			 if(weight < minPrioToPrint)
				 break;
			 I item = pq.next();
			 sb.append(item.toString());
			 sb.append(" : ");
			 sb.append(weight);
			 if (numItemsPrinted < size() - 1) {
				 sb.append(fWithNewlines?"\n":", ");
			 }
			 numItemsPrinted++;
		 }
		 if (numItemsPrinted < size())
			 sb.append("...");
		 sb.append("]");
		 return sb.toString();
	 }

	 /**
	  * Returns a representation of the queue in decreasing weight order,
	  * down to those with a weight of minCountToPrint.
	  *
	  * @param minPrioToPrint
	  */
	 public void write( java.io.BufferedWriter writer, boolean fDestructive,
			 double minPrioToPrint, String separator, boolean quoteStrings, boolean fWithNewlines)
	 			throws java.io.IOException {
		 if(separator==null)
			 separator = " : ";
		 SortedQueue<I> pq = fDestructive?this:clone();
		 int numItemsPrinted = 0;
		 int nTotalMinus1 = size()-1;
		 while(pq.hasNext()) {
			 double weight = pq.getMaxWeight();
			 if(weight < minPrioToPrint)
				 break;
			 I item = pq.next();
			 String outStr = item.toString();
			 // TODO Consider whether we should escape strings, etc.
			 if(quoteStrings)
				 writer.append(weight+separator+'"'+outStr+'"');
			 else
				 writer.append(weight+separator+outStr);
			 if (numItemsPrinted < nTotalMinus1) {
				 if(fWithNewlines)
					 writer.append("\n");
				 else
					 writer.append(", ");
			 }
			 numItemsPrinted++;
		 }
		 if (numItemsPrinted < size())
			 writer.append("...");
		 writer.flush();
		 return;
	 }

	 /**
	  * Returns an ArrayList with the items in this weight queue, sorted
	  * by the priorities in this queue.
	  *
	  * @return new ArrayList for this weight queue.
	  */
	 public ArrayList<I> asArrayList() {
		 SortedQueue<I> pq = clone();
		 ArrayList<I> al = new ArrayList<I>();
		 while (pq.hasNext()) {
			 al.add(pq.next());
		 }
		 return al;
	 }

	 /**
	  * Returns a clone of this weight queue.  Modifications to one will not
	  * affect modifications to the other.
	  */
	 @Override
	public SortedQueue<I> clone() {
		 SortedQueue<I> clonePQ = new SortedQueue<I>();
		 clonePQ.size = size;
		 clonePQ.capacity = capacity;
		 clonePQ.items = new ArrayList<I>(capacity);
		 clonePQ.priorities = new double[capacity];
		 if (size() > 0) {
			 clonePQ.items.addAll(items);
			 System.arraycopy(priorities, 0, clonePQ.priorities, 0, size());
		 }
		 return clonePQ;
	 }

}
