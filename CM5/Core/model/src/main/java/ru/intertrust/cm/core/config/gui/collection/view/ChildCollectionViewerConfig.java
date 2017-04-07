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
    private String breadCrumb="Не определён";

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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChildCollectionViewerConfig that = (ChildCollectionViewerConfig) o;

        if (forDomainObjectType != null ? !forDomainObjectType.equals(that.forDomainObjectType) : that.forDomainObjectType != null)
            return false;
        if (filter != null ? !filter.equals(that.filter) : that.filter != null) return false;
        if (domainObjectTypeToCreate != null ? !domainObjectTypeToCreate.equals(that.domainObjectTypeToCreate) : that.domainObjectTypeToCreate != null)
            return false;
        if (breadCrumb != null ? !breadCrumb.equals(that.breadCrumb) : that.breadCrumb != null) return false;
        if (collectionViewerConfig != null ? !collectionViewerConfig.equals(that.collectionViewerConfig) : that.collectionViewerConfig != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return forDomainObjectType != null ? forDomainObjectType.hashCode() : 0;
    }
}
