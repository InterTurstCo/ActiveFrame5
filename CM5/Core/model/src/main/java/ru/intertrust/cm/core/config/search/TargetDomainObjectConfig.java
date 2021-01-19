package ru.intertrust.cm.core.config.search;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Order;

import java.util.Objects;

@Order(elements={"indexed-field", "indexed-content", "linked-domain-object", "target-collection", "filter"})
public class TargetDomainObjectConfig extends IndexedDomainObjectConfig {

    @Element(name = "target-collection", required = false)
    private TargetCollectionConfig collectionConfig;

    public TargetCollectionConfig getCollectionConfig() {
        return collectionConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TargetDomainObjectConfig that = (TargetDomainObjectConfig) o;
        return Objects.equals(collectionConfig, that.collectionConfig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), collectionConfig);
    }
}
