package ru.intertrust.cm.core.config.model.gui.navigation;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 10/9/13
 *         Time: 12:05 PM
 */
@Root(name = "decorations")
public class DecorationsConfig implements Dto {

    @Element(name = "collection-counter", required = false)
    private CollectionCounterConfig collectionCounterConfig;

    public CollectionCounterConfig getCollectionCounterConfig() {
        return collectionCounterConfig;
    }

    public void setCollectionCounterConfig(
            CollectionCounterConfig collectionCounterConfig) {
        this.collectionCounterConfig = collectionCounterConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DecorationsConfig that = (DecorationsConfig) o;

        if (collectionCounterConfig != null ? !collectionCounterConfig.equals(that.getCollectionCounterConfig()) : that.
                getCollectionCounterConfig() != null) {
                    return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return collectionCounterConfig != null ? collectionCounterConfig.hashCode() : 0;
    }
}

