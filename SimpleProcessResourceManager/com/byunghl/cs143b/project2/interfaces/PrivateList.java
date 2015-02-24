package com.byunghl.cs143b.project2.interfaces;

import java.util.Iterator;


public interface PrivateList<E> {

    public abstract void add(E element);

    public abstract void add(E element, int index);

    public abstract void remove(E element);

    public abstract E get(int index);

    public abstract int size() ;

    public abstract Iterator iterator();
}
