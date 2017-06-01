package edu.uwm.cs.util;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * A kind of collection in which duplicates are tabulated.
 * It can be views as a map from elements to a count,
 * in which (readonly) iteration is in decreasing count order.
 */
public class Histogram<T extends Comparable<T>> {
	private HashMap<T,Integer> contents = new HashMap<>();
	private Set<Map.Entry<T,Integer>> entrySet = new TreeSet<>();
	
	/**
	 * Add the element to the histogram.
	 * @param element element to add to histogram
	 * @return true if this was the first occurrence of this element.
	 */
	public boolean add(T element) {
		Integer old = contents.get(element);
		Integer count;
		if (old != null) {
			entrySet.remove(new MyEntry<T>(element,old));
			count = old + 1;
		} else {
			count = 1;
		}
		contents.put(element,count);
		entrySet.add(new MyEntry<T>(element,count));
		return old == null;
	}
	
	/**
	 * Remove the element from the histogram.
	 * @param x element to remove (may be null)
	 * @return true if the element had been in.
	 */
	@SuppressWarnings("unchecked")
	public boolean remove(Object x) {
		Integer old = contents.get(x);
		Integer count;
		if (old != null) {
			entrySet.remove(new MyEntry<T>((T)x,old));
			count = old - 1;
		} else {
			return false;
		}
		if (old.intValue() == 1) {
			contents.remove(x);
		} else {
			contents.put((T)x, count);
			entrySet.add(new MyEntry<T>((T)x,count));
		}
		return true;
	}
	
	public Set<Map.Entry<T,Integer>> entrySet() {
		return Collections.unmodifiableSet(entrySet);
	}
	
	private static class MyEntry<T extends Comparable<T>> extends AbstractMap.SimpleEntry<T, Integer> implements Comparable<MyEntry<T>>{

		/**
		 * KEH
		 */
		private static final long serialVersionUID = 1L;

		public MyEntry(T key, Integer value) {
			super(key,value);
		}

		@Override
		public Integer setValue(Integer value) {
			throw new UnsupportedOperationException("cannot mutate histogram entries");
		}

		@Override
		public int compareTo(MyEntry<T> o) {
			int diff =  (o.getValue() - getValue());
			if (diff != 0) return diff;
			return getKey().compareTo(o.getKey());
		}
	}
}
