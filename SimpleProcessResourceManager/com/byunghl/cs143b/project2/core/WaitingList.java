package com.byunghl.cs143b.project2.core;

import com.byunghl.cs143b.project2.interfaces.PrivateList;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by aznnobless on 2/7/15.
 */
public class WaitingList implements PrivateList {

    private List<WaitingBundle> list;

    public WaitingList() {
        list = new LinkedList<WaitingBundle>();
    }


    @Override
    public void add(Object element) {
        list.add((WaitingBundle)element);
    }

    @Override
    public void add(Object element, int index) {
        list.add(index, (WaitingBundle)element);
    }

    @Override
    public void remove(Object element) {
        list.remove((WaitingBundle)element);
    }

    @Override
    public WaitingBundle get(int index) {
        return list.get(index);
    }

    @Override
    public int size() {
        return list.size();
    }



    public Iterator iterator() {
        return list.iterator();
    }

}
