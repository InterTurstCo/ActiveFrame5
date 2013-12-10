package ru.intertrust.cm.core.config.search;

import org.simpleframework.xml.Element;

public class TargetDomainObjectConfig extends IndexedDomainObjectConfig {

    @Element(name = "target-collection", required = true)
    private TargetCollectionConfig collectionConfig;

    public TargetCollectionConfig getCollectionConfig() {
        return collectionConfig;
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 31 ^ collectionConfig.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && collectionConfig.equals(((TargetDomainObjectConfig) obj).collectionConfig);
    }
}
