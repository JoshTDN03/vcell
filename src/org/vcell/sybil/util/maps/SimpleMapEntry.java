package org.vcell.sybil.util.maps;

import java.util.Map.Entry;

import org.vcell.sybil.util.JavaUtil;

public class SimpleMapEntry<K, V> implements Entry<K, V> {

	protected final K key;
	protected final V value;
	
	protected void throwUnsupportedOperationException() { throw new UnsupportedOperationException("Can not change entry of a constant map."); }
	
	public SimpleMapEntry(K key, V value) { this.key = key; this.value = value; }
	public K getKey() { return key; }
	public V getValue() { return value; }
	public V setValue(V arg0) { throwUnsupportedOperationException(); return null; }
	public int hashCode() { return JavaUtil.hashCode(key) ^ JavaUtil.hashCode(value); }

	public boolean equals(Object o) {
		if(o instanceof Entry) {
			Entry<?, ?> e = (Entry<?, ?>) o;
			return JavaUtil.equals(key, e.getKey()) && JavaUtil.equals(value, e.getValue());
		}
		return false;
	}
	
}
