package edu.reversing.commons;

import java.util.*;

public class Digraph<V, E> implements Iterable<V> {

    private final Map<V, Set<E>> graph;

    public Digraph(Comparator<V> comparator) {
        this.graph = comparator != null ? new TreeMap<>(comparator) : new HashMap<>();
    }

    public Digraph() {
        this(null);
    }

    public int size() {
        return graph.size();
    }

    public boolean containsVertex(V vertex) {
        return graph.containsKey(vertex);
    }

    public boolean containsEdge(V from, E to) {
        return graph.containsKey(from) && graph.get(from).contains(to);
    }

    public boolean addVertex(V vertex) {
        if (graph.containsKey(vertex)) {
            return false;
        }
        graph.put(vertex, new HashSet<>());
        return true;
    }

    public void addEdge(V from, E to) {
        if (graph.containsKey(from)) {
            graph.get(from).add(to);
        }
    }

    public void removeEdge(V from, E to) {
        if (graph.containsKey(from)) {
            graph.get(from).remove(to);
        }
    }

    public Set<E> getEdges(V vertex) {
        return graph.get(vertex);
    }

    public void putAll(Digraph<V, E> graph) {
        this.graph.putAll(graph.graph);
    }

    @Override
    public final Iterator<V> iterator() {
        return graph.keySet().iterator();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (V v : graph.keySet()) {
            builder.append("\n    ").append(v).append(" -> ").append(graph.get(v));
        }
        return builder.toString();
    }

    public void clear() {
        graph.clear();
    }

    public boolean isEmpty() {
        return graph.size() == 0;
    }
}