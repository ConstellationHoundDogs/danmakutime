package nl.weeaboo.dt;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Performance-conscious list implementation.
 * 
 * <ul>
 * <li>Provides access to its internals</li>
 * <li>No <code>null</code> values allowed</li>
 * <li>{@link #toArray()} returns the internal array</li>
 * <li>Equality relies on ==, {@link #equals(Object)} is not used</li>
 * <li>Removal leaves an array slot open, call {@link #compact()} every once in
 * a while to remove the empty slots.</li>
 * </ul>
 */
public class FastList<E> extends AbstractList<E> {

	private E arr[];
	private int size = 0;
	
	public FastList(Class<E> clazz) {
		this(clazz, 16);
	}
	
	@SuppressWarnings("unchecked")
	public FastList(Class<E> clazz, int initialCapacity) {
		arr = (E[])Array.newInstance(clazz, initialCapacity);
		size = 0;
	}
	
	//Functions	
	private void boundsCheck(int i) {
		if (i >= size || i < 0) {
			throw new ArrayIndexOutOfBoundsException(i);
		}
	}
	
	private void ensureCapacity(int newSize) {
		if (newSize > arr.length) {
			arr = Arrays.copyOf(arr, arr.length << 1);
		}
	}
	
	public void compact() {
		int read = 0;
		int write = 0;
		int blanks = 0;

		while (read < size) {
			if (arr[read] != null) {
				arr[write] = arr[read];				
				write++;
			} else {
				blanks++;
			}
			read++;
		}

		size -= blanks;
		Arrays.fill(arr, size, size + blanks, null);
	}
	
	@Override
    public boolean add(E e) {
    	ensureCapacity(size + 1);
    	arr[size] = e;
    	size++;
    	modCount++;
    	return true;
    }

	@Override
    public void add(int index, E value) {
    	if (index > size || index < 0) {
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
    	}
    	
    	if (index == size) {
    		add(value);
    		return;
    	}
    	
    	ensureCapacity(size + 1);

    	//Move elems to make room
    	System.arraycopy(arr, index, arr, index + 1, size - index);
    	arr[index] = value;
    	size++;
    	modCount++;
    }
		
	@Override
    public E remove(int index) {
    	boundsCheck(index);

    	E old = arr[index];
		size--;
    	arr[size] = null;
    	modCount++;

    	return old;
	}

	@Override
    public boolean remove(Object o) {
		for (int n = 0; n < size; n++) {
			if (arr[n] == o) {
				remove(n);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean removeAll(Collection<?> c) {
		int oldModCount = modCount;
		for (Object obj : c) {
			for (int n = 0; n < size; n++) {
				if (arr[n] == obj) {
					arr[n] = null;
					modCount++;
					break;
				}
			}
		}
		
		if (modCount != oldModCount) {
			compact();
			return true;
		}
		return false;
	}
	
	@Override
    public void clear() {
		Arrays.fill(arr, 0, size, null);
		size = 0;
    	modCount++;
	}
	
	//Getters
	@Override
	public E get(int index) {
		boundsCheck(index);
		return arr[index];
	}

	@Override
	public int size() {
		return size;
	}
	
	@Override
    public E[] toArray() {
		return arr;
	}

	@Override
	@SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < size) {
            return (T[])toArray();
        }
        
        System.arraycopy(arr, 0, a, 0, size);
        if (a.length > size) {
            a[size] = null;
        }
        return a;
	}
	
	//Setters
    public E set(int index, E element) {
    	boundsCheck(index);
    	E old = arr[index];
    	arr[index] = element;
    	return old;
    }
	
}
