package edu.reversing.commons;

import java.util.*;
import java.util.function.Consumer;

public class Multimap<K, V> {

  private final Map<K, Set<V>> raw;

  public Multimap(Map<K, Set<V>> raw) {
    this.raw = raw;
  }

  public Multimap() {
    this(new HashMap<>());
  }

  public boolean put(K key, V value) {
    Set<V> values = raw.computeIfAbsent(key, x -> new HashSet<>());
    return values.add(value);
  }

  public boolean put(K key, Collection<V> value) {
    Set<V> values = raw.computeIfAbsent(key, x -> new HashSet<>());
    return values.addAll(value);
  }

  public Set<Map.Entry<K, Set<V>>> entrySet() {
    return raw.entrySet();
  }

  public Map<K, Set<V>> direct() {
    return raw;
  }

  public void clear() {
    raw.clear();
  }

  public boolean containsKey(K key) {
    return raw.containsKey(key);
  }

  public boolean contains(V value) {
    for (Set<V> values : raw.values()) {
      if (values.contains(value)) {
        return true;
      }
    }
    return false;
  }

  public Set<V> get(K key) {
    return raw.get(key);
  }

  public Set<V> getOrDefault(K key, Set<V> def) {
    Set<V> values = get(key);
    if (values == null) {
      return def;
    }
    return values;
  }

  public Set<K> keySet() {
    return raw.keySet();
  }

  public Multiset<V> valueSet() {
    Multiset<V> set = new Multiset<>();
    for (Set<V> values : raw.values()) {
      set.addAll(values);
    }
    return set;
  }

  public int size() {
    return raw.size();
  }

  public boolean isEmpty() {
    return raw.isEmpty();
  }

  public Set<V> remove(K key) {
    return raw.remove(key);
  }

  public Set<V> replace(K key, Collection<V> value) {
    Set<V> old = remove(key);
    put(key, value);
    return old;
  }

  public void ifPresent(K key, Consumer<V> visitor) {
    for (V value : getOrDefault(key, Collections.emptySet())) {
      visitor.accept(value);
    }
  }
}
