package edu.reversing.commons;

import java.util.*;

public class Multiset<E> extends HashSet<E> {

  private final Map<E, Integer> counts = new HashMap<>();

  @Override
  public boolean add(E element) {
    boolean added = super.add(element);
    counts.put(element, counts.getOrDefault(element, 0));
    return added;
  }

  public Set<Map.Entry<E, Integer>> entries() {
    return counts.entrySet();
  }

  @Override
  public boolean addAll(Collection<? extends E> elements) {
    for (E element : elements) {
      if (!add(element)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean remove(Object o) {
    boolean removed = super.remove(o);
    counts.remove(o);
    return removed;
  }

  @Override
  public void clear() {
    super.clear();
    counts.clear();
  }

  public int count(E element) {
    return counts.getOrDefault(element, 0);
  }

  public int uniqueCount() {
    return counts.size();
  }

  public E maximum() {
    E element = null;
    int count = 0;
    for (Map.Entry<E, Integer> entry : counts.entrySet()) {
      if (entry.getValue() > count) {
        element = entry.getKey();
        count = entry.getValue();
      }
    }
    return element;
  }

  public E minimum() {
    E element = null;
    int count = Integer.MAX_VALUE;
    for (Map.Entry<E, Integer> entry : counts.entrySet()) {
      if (entry.getValue() < count) {
        element = entry.getKey();
        count = entry.getValue();
      }
    }
    return element;
  }
}