package ru.intertrust.cm.core.tools;

import java.util.Iterator;

public class DelegatingIterator<E> implements Iterator<E> {

    protected Iterator<E> delegate;

    public DelegatingIterator(Iterator<E> delegate) {
        this.delegate = delegate;
    }

    public DelegatingIterator(Iterable<E> source) {
        this.delegate = source.iterator();
    }

    @Override
    public boolean hasNext() {
        return delegate.hasNext();
    }

    @Override
    public E next() {
        return delegate.next();
    }

    @Override
    public void remove() {
        delegate.remove();
    }

}
