package ru.intertrust.cm.core.config.model.gui.navigation;

import java.io.Serializable;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * Author: Yaroslav Bondarchuk Date: 04.09.13 Time: 16:01
 */
@SuppressWarnings("serial")
@Root(name = "collection-counter")
public class CollectionCounterConfig implements Serializable {
    @Attribute(name = "collection", required = false)
    private String collection;

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CollectionCounterConfig that = (CollectionCounterConfig) o;

        if (collection != null ? !collection.equals(that.getCollection()) : that.getCollection() != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = collection != null ? collection.hashCode() : 0;
        return result * 23;
    }

}
