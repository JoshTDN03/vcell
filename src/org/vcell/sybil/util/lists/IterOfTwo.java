package org.vcell.sybil.util.lists;

/*   IterOfTwo  --- by Oliver Ruebenacker, UCHC --- April 2008
 *   An iterator for a list with two elements
 */

import java.util.ListIterator;
import java.util.NoSuchElementException;

public class IterOfTwo<E> implements ListIterator<E> {

	protected E e1, e2;
	protected int i;
	
	public IterOfTwo(E e1New, E e2New) { e1 = e1New; e2 = e2New; }
	public IterOfTwo(E e1New, E e2New, int iNew) { e1 = e1New; e2 = e2New; i = iNew; }
	
	public boolean hasNext() { return i < 2; }
	public boolean hasPrevious() { return i > -1; }
	public int nextIndex() { return i; }
	public int previousIndex() { return i-1; }
	
	public E next() { 
		if(i == 0) { i = 1; return e1; }
		else if(i == 1) { i = 2; return e2; }
		throw new NoSuchElementException(); 
	}
	
	public E previous() { 
		if(i == 1) { i = 0; return e1; }
		else if(i == 2) { i = 1; return e2; }
		throw new NoSuchElementException(); 
	}
	
	public void add(E e) { throw new UnsupportedOperationException(); }
	public void remove() { throw new UnsupportedOperationException(); }
	public void set(E e) { throw new UnsupportedOperationException(); }

}
