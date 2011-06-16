/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package org.vcell.sybil.util.collections;

/*   MultiMap  --- by Oliver Ruebenacker, UCHC --- July 2007
 *   Maps from a key to a set of values. 
 */

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public interface MultiMap<K, V> extends Map<K, Set<V>>{

	public void clear();
	public void add(K key, V value);
	public void remove(K key, V value);
	public void removeAll(K key);
	public Iterator<V> getAll(K key);
	public boolean contains(K key, V value);
	
	
}
