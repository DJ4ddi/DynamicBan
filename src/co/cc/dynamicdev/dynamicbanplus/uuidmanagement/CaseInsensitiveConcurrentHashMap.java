package co.cc.dynamicdev.dynamicbanplus.uuidmanagement;

import java.util.concurrent.ConcurrentHashMap;

public class CaseInsensitiveConcurrentHashMap<V> extends ConcurrentHashMap<String, V> {
	@Override
	public V put(String key, V value) {
		return (key == null ? null : super.put(key.toLowerCase(), value));
	}
	
	@Override
	public V get(Object key) {
		return (key == null ? null : super.get(((String) key).toLowerCase()));
	}
	
	@Override
	public boolean containsKey(Object key) {
		return (key == null ? false : super.containsKey(((String) key).toLowerCase()));
	}
	
	@Override
	public V remove(Object key) {
		return (key == null ? null : super.remove(((String) key).toLowerCase()));
	}
}
