package ru.intertrust.cm.core.config.gui.collection.view;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.base.Localizable;
import ru.intertrust.cm.core.config.gui.navigation.CollectionViewerConfig;

/**
 * @author Lesia Puhova
 *         Date: 09.09.14
 *         Time: 18:09
 */
@Root(name = "child-collection-viewer")
public class ChildCollectionViewerConfig implements Dto {

    @Attribute(name="for-domain-object-type", required = false)
    private String forDomainObjectType;

    @Attribute(name="filter")
    private String filter;

    @Attribute(name="domain-object-type-to-create")
    private String domainObjectTypeToCreate;

    @Attribute(name="bread-crumb", required = false)
    @Localizable
    private String breadCrumb;

    @Element(name = "collection-viewer")
    private CollectionViewerConfig collectionViewerConfig;

    public String getForDomainObjectType() {
        return forDomainObjectType;
    }

    public String getFilter() {
        return filter;
    }

    public String getDomainObjectTypeToCreate() {
        return domainObjectTypeToCreate;
    }

    public String getBreadCrumb() {
        return breadCrumb;
    }

    public CollectionViewerConfig getCollectionViewerConfig() {
        return collectionViewerConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ChildCollectionViewerConfig that = (ChildCollectionViewerConfig) o;

        if (!collectionViewerConfig.equals(that.collectionViewerConfig)) {
            return false;
        }
        if (!domainObjectTypeToCreate.equals(that.domainObjectTypeToCreate)) {
            return false;
        }
        if (!filter.equals(that.filter)) {
            return false;
        }
        if (!breadCrumb.equals(that.breadCrumb)) {
            return false;
        }
        if (forDomainObjectType != null ? !forDomainObjectType.equals(that.forDomainObjectType) : that.forDomainObjectType != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = forDomainObjectType != null ? forDomainObjectType.hashCode() : 0;
        result = 31 * result + filter.hashCode();
        result = 31 * result + domainObjectTypeToCreate.hashCode();
        result = 31 * result + breadCrumb.hashCode();
        result = 31 * result + collectionViewerConfig.hashCode();
        return result;
    }
}
