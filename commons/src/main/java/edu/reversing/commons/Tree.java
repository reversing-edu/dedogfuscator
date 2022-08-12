package edu.reversing.commons;

import java.util.LinkedList;

public class Tree<T extends Tree<T>> extends LinkedList<T> {

    private T parent;

    public T getParent() {
        return parent;
    }

    public void setParent(T parent) {
        this.parent = parent;
    }

    @Override
    public void addFirst(T t) {
        t.setParent((T) this);
        super.addFirst(t);
    }
}
