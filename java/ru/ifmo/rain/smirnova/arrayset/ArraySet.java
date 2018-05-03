package ru.ifmo.rain.smirnova.arrayset;

import java.util.*;

public class ArraySet<E> extends AbstractSet<E> implements SortedSet<E> {

    private final List<E> list;
    private final Comparator<? super E> comparator;

    public ArraySet() {
        list = Collections.emptyList();
        comparator = null;
    }

    public ArraySet(Comparator<? super E> comp) {
        list = Collections.emptyList();
        comparator = comp;
    }

    public ArraySet(Collection<E> coll) {
        list = new ArrayList<>(new TreeSet<>(coll));
        comparator = null;
    }

    public ArraySet(Collection<E> coll, Comparator<? super E> comp) {
        comparator = comp;

        TreeSet<E> treeSet = new TreeSet<>(comp);
        treeSet.addAll(coll);
        list = new ArrayList<>(treeSet);
    }

    public ArraySet(List<E> list, Comparator<? super E> comp) {
        this.list = list;
        comparator = comp;
    }

    @Override
    public ArraySet<E> headSet(E toElement) {
        if (!list.isEmpty()) {
            return sSet(list.get(0), toElement, false);
        } else {
            return this;
        }
    }

    @Override
    public SortedSet tailSet(E fromElement) {
        if (!list.isEmpty()) {
            return sSet(fromElement, list.get(list.size() - 1), true);
        } else {
            return this;
        }
    }

    @Override
    public ArraySet<E> subSet(E fromElement, E toElement) {
        if (!list.isEmpty()) {
            return sSet(fromElement, toElement, false);
        } else {
            return this;
        }
    }

    @Override
    public Comparator comparator() {
        return comparator;
    }

    @Override
    public E first() {
        checkEmpty();
        return list.get(0);
    }

    @Override
    public Iterator iterator() {
        return Collections.unmodifiableList(list).iterator();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        return Collections.binarySearch(list, (E) o, comparator) >= 0;
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public E last() {
        checkEmpty();
        return list.get(list.size() - 1);
    }

    private int find(E element) {
        int e = Collections.binarySearch(list, element, comparator);

        if (e < 0) {
            return -(e + 1);
        } else {
            return e;
        }
    }

    private ArraySet<E> sSet(E fromElement, E toElement, boolean include) {
        ArraySet<E> arraySet;

        int l = find(fromElement);
        int r = find(toElement);

        if (include) {
            r++;
        }

        arraySet = new ArraySet<>(list.subList(l, r), comparator);

        return arraySet;
    }

    private void checkEmpty() {
        if (list.isEmpty()) {
            throw new NoSuchElementException("Empty list");
        }
    }

}