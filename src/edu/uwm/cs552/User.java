package edu.uwm.cs552;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class User {

	private static String DOMAIN = "uwm.edu";

	private final String name;
	
	private User() {
		throw new IllegalStateException("can't call empty constructor");
	}
	
	private User(String n) {
		name = n;
	}

	
	/**
	 * Return the name of this user (without domain).
	 * @return name of the user.
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name + "@" + DOMAIN;
	}

	public static class Users implements ListModel<User>, Iterable<User> {
		private Map<String,User> index = new TreeMap<>();
		private List<User> cache = new ArrayList<>();
		private List<ListDataListener> listeners = new ArrayList<>();
		
		public User find(String n) {
			return index.get(n);
		}
		
		public User get(String n) {
			User u  = find(n);
			if (u == null) {
				u = new User(n);
				index.put(n, u);
				// super easy, but inefficient
				cache.clear();
				cache.addAll(index.values());
				int index = cache.indexOf(u);
				for (ListDataListener l : listeners) {
					l.intervalAdded(new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, index, index));
				}
			}
			return u;
		}

		@Override
		public int getSize() {
			return cache.size();
		}

		@Override
		public User getElementAt(int index) {
			return cache.get(index);
		}

		@Override
		public void addListDataListener(ListDataListener l) {
			listeners.add(l);
		}

		@Override
		public void removeListDataListener(ListDataListener l) {
			listeners.remove(l);
		}
		
		@Override
		public Iterator<User> iterator() {
			return new MyIterator();
		}

		private class MyIterator implements Iterator<User> {
			private Iterator<User> underlying = index.values().iterator();

			@Override
			public boolean hasNext() {
				return underlying.hasNext();
			}

			@Override
			public User next() {
				return underlying.next();
			}
			
			// No remove needed: Java 8 remove has a default implementation
		}
	}

}
