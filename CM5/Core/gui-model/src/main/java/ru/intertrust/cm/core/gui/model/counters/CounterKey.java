package ru.intertrust.cm.core.gui.model.counters;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Created by andrey on 20.03.14.
 */
public class CounterKey implements Dto {

    private String linkName;
    private String collectionName;

    public CounterKey(String name, String collectionToBeOpened) {
        this.linkName = name;
        this.collectionName = collectionToBeOpened;
    }

    public CounterKey() {
    }

    public String getLinkName() {
        return linkName;
    }

    public void setLinkName(String linkName) {
        this.linkName = linkName;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CounterKey that = (CounterKey) o;

        if (!collectionName.equals(that.collectionName)) return false;
        if (!linkName.equals(that.linkName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = linkName.hashCode();
        result = 31 * result + collectionName.hashCode();
        return result;
    }
}
