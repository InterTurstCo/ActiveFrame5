package ru.intertrust.cm.core.business.impl.universalentity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

import javax.annotation.Nonnull;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.util.Args;

abstract class ListWrapper<E> implements List<E>, RandomAccess, Serializable {

    private static final long serialVersionUID = 1L;
    private static final String FIELD_OWNER = "Owner";
    private static final String FIELD_INDEX = "Idx";

    private static final Comparator<DomainObject> compOdopsByIdx = new Comparator<DomainObject>() {

        @Override
        public int compare (final DomainObject o1, final DomainObject o2) {
            return Long.compare(o1.getLong(FIELD_INDEX), o2.getLong(FIELD_INDEX));
        }

    };

    final Class<E> clazz;
    final DomainObjectContainer cnt;
    final String odopName;
    private final boolean isAllowNulls;
    private final List<E> list;
    private Integer size;
    private boolean isLoaded;

    ListWrapper (final @Nonnull String odopName, final @Nonnull Class<E> clazz, final boolean isAllowNulls, final @Nonnull DomainObjectContainer cnt) {
        this.odopName = Args.notNull(odopName, "odopName");
        this.clazz = Args.notNull(clazz, "clazz");
        this.isAllowNulls = isAllowNulls;
        this.cnt = Args.notNull(cnt, "cnt");
        this.list = Collections.checkedList(new ArrayList<E>(), clazz);
    }

    @Override
    public boolean isEmpty () {
        return this.size() == 0;
    }

    @Override
    public int size () {

        if (this.isLoaded) {
            return this.list.size();
        } else if (this.size == null) {
            this.size = this.cnt.getDomainObject().isNew() ? 0 : BeansHolder.get().colls.findCollectionByQuery(
                "select count(*) from " + this.odopName + " where \"Owner=\" = {0}",
                Arrays.asList(new ReferenceValue(this.cnt.getDomainObject().getId()))).size();
        }

        return this.size;

    }

    @Override
    public boolean contains (final Object o) {
        this.loadFromDb();
        return this.list.contains(o);
    }

    @Override
    public Object[] toArray () {
        this.loadFromDb();
        return this.list.toArray();
    }

    @Override
    public <T> T[] toArray (final T[] a) {
        this.loadFromDb();
        return this.list.toArray(a);
    }

    @Override
    public boolean add (final E e) {
        this.loadFromDb();
        return this.list.add(e);
    }

    @Override
    public boolean remove (final Object o) {
        this.loadFromDb();
        return this.list.remove(o);
    }

    @Override
    public boolean containsAll (final Collection<?> c) {
        this.loadFromDb();
        return this.list.containsAll(c);
    }

    @Override
    public boolean addAll (final Collection<? extends E> c) {
        this.loadFromDb();
        return this.list.addAll(c);
    }

    @Override
    public boolean addAll (final int index, final Collection<? extends E> c) {
        this.loadFromDb();
        return this.list.addAll(index, c);
    }

    @Override
    public boolean removeAll (final Collection<?> c) {
        this.loadFromDb();
        return this.list.removeAll(c);
    }

    @Override
    public boolean retainAll (final Collection<?> c) {
        this.loadFromDb();
        return this.list.retainAll(c);
    }

    @Override
    public void clear () {
        this.list.clear();
    }

    @Override
    public E get (final int index) {
        this.loadFromDb();
        return this.list.get(index);
    }

    @Override
    public E set (final int index, final E element) {
        this.loadFromDb();
        return this.list.set(index, element);
    }

    @Override
    public void add (final int index, final E element) {
        this.loadFromDb();
        this.list.add(index, element);
    }

    @Override
    public E remove (final int index) {
        this.loadFromDb();
        return this.list.remove(index);
    }

    @Override
    public int indexOf (final Object o) {
        this.loadFromDb();
        return this.list.indexOf(o);
    }

    @Override
    public int lastIndexOf (final Object o) {
        this.loadFromDb();
        return this.list.lastIndexOf(o);
    }

    @Override
    public Iterator<E> iterator () {
        this.loadFromDb();
        return this.list.iterator();
    }

    @Override
    public ListIterator<E> listIterator () {
        this.loadFromDb();
        return this.list.listIterator();
    }

    @Override
    public ListIterator<E> listIterator (final int index) {
        this.loadFromDb();
        return this.list.listIterator(index);
    }

    @Override
    public List<E> subList (final int fromIndex, final int toIndex) {
        this.loadFromDb();
        return this.list.subList(fromIndex, toIndex);
    }

    // ----------------------------------------------------------------------------------------------------------------

    private void loadFromDb () {

        if (this.isLoaded) {
            return;
        }

        this.isLoaded = true;

        if ((this.size != null && this.size == 0) || this.cnt.getDomainObject().isNew()) {
            return;
        }

        for (final DomainObject odop : this.loadOdops()) {
            final E value = this.fromOdop(odop);
            this.list.add(value);
        }
    }

    final void update () {

        if (!this.isLoaded) {
            return;
        }

        final DomainObject[] odopsArr = this.loadOdops().toArray(new DomainObject[0]);
        final Iterator<E> currValuesIter = this.list.iterator();
        int currValuesCount = 0;
        final CrudService crud = BeansHolder.get().crud;

        while (currValuesIter.hasNext() && currValuesCount < odopsArr.length) {

            final E currValue = currValuesIter.next();

            if (odopsArr[currValuesCount] == null || !this.isAllFieldsEquals(odopsArr[currValuesCount], currValue)) {

                if (currValue != null) {

                    for (int i = 0; i < currValuesCount; i++) {
                        if (odopsArr[i] != null && this.isUniqueConflicts(odopsArr[i], currValue)) {
                            throw new RuntimeException("unique-constraint violated at positions " + i + " and " + currValuesCount);
                        }
                    }

                    for (int i = currValuesCount + 1; i < odopsArr.length; i++) {
                        if (odopsArr[i] != null && this.isUniqueConflicts(odopsArr[i], currValue)) {
                            crud.delete(odopsArr[i].getId());
                            odopsArr[i] = null;
                        }
                    }

                }

                if (odopsArr[currValuesCount] == null) {
                    odopsArr[currValuesCount] = crud.createDomainObject(this.odopName);
                    odopsArr[currValuesCount].setReference(FIELD_OWNER, this.cnt.getDomainObject());
                    odopsArr[currValuesCount].setLong(FIELD_INDEX, (long)currValuesCount);
                }

                this.toOdop(odopsArr[currValuesCount], currValue);
                odopsArr[currValuesCount] = crud.save(odopsArr[currValuesCount]);

            }

            currValuesCount++;

        }

        if (currValuesIter.hasNext()) {

            while (currValuesIter.hasNext()) {
                final DomainObject newOdop = crud.createDomainObject(this.odopName);
                newOdop.setReference(FIELD_OWNER, this.cnt.getDomainObject());
                newOdop.setLong(FIELD_INDEX, (long)currValuesCount++);
                this.toOdop(newOdop, currValuesIter.next());
                crud.save(newOdop);
            }

        } else {

            for (int i = currValuesCount; i < odopsArr.length; i++) {
                if (odopsArr[i] != null) {
                    crud.delete(odopsArr[i].getId());
                }
            }

        }

    }

    @Nonnull
    private List<DomainObject> loadOdops () {
        final List<DomainObject> result = BeansHolder.get().crud.findLinkedDomainObjects(this.cnt.getDomainObject().getId(), this.odopName, FIELD_OWNER);
        Collections.sort(result, compOdopsByIdx);
        return result;
    }

    @Nonnull
    abstract E fromOdop (@Nonnull DomainObject odop);

    abstract boolean isAllFieldsEquals (@Nonnull DomainObject odop, @Nonnull E currState);

    abstract boolean isUniqueConflicts (@Nonnull DomainObject odop, @Nonnull E currState);

    abstract void toOdop (@Nonnull DomainObject odop, @Nonnull E currState);

}