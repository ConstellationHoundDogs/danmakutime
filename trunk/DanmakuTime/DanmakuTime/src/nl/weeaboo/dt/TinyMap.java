package nl.weeaboo.dt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Map-like container optimized for small (&lt;10 elements) data sets.
 */
public class TinyMap<T> {

	private List<Integer> keys;
	private List<T> values;
	
	public TinyMap() {
		this(8);
	}
	public TinyMap(int capacity) {
		keys = new ArrayList<Integer>(capacity);
		values = new ArrayList<T>(capacity);
	}
	
	//Functions
	public void clear() {
		keys.clear();
		values.clear();
	}
	
	public T put(int key, T value) {
		//Replace existing value if there is one
		for (int n = 0; n < keys.size(); n++) {
			if (keys.get(n) == key) {
				return values.set(n, value);
			}
		}
				
		//Add new entry
		keys.add(key);
		values.add(value);
		return null;
	}
	
	public T remove(int key) {
		for (int n = 0; n < keys.size(); n++) {
			if (keys.get(n) == key) {
				keys.remove(n);
				return values.remove(n);
			}
		}
		return null;
	}
	
	//Getters
	public T get(int key) {
		for (int n = 0; n < keys.size(); n++) {
			if (keys.get(n) == key) {
				return values.get(n);
			}
		}
		return null;
	}
	
	public boolean containsKey(int key) {
		for (int n = 0; n < keys.size(); n++) {
			if (keys.get(n) == key) {
				return true;
			}
		}
		return false;
	}
	
	public Collection<Integer> getKeys() {
		return keys;
	}
	public Collection<T> getValues() {
		return values;
	}
	
	public int getSize() {
		return keys.size();
	}
	
	//Setters
	
}
