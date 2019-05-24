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

    @Attribute(name = "breadCrumbColumn", required = false)
    private String breadCrumbColumn;

    @Attribute(name = "breadCrumbMaxChars", required = false)
    private Integer breadCrumbMaxChars;

    @Attribute(name="bread-crumb", required = false)
    @Localizable
    private String breadCrumb="Не определён";

    /**
     * Атрибут скрытия показа стрелки дочерней коллекции, если последняя пустая (не содержит ни одного дочернего элемента)
     */
    @Attribute(name = "hide-arrow-if-empty", required = false)
    private Boolean hideArrowIfEmpty = false;

    /**
     * Атрибут показа количества элементов дочерней коллекции (не зависимо от того имеются они или нет)
     */
    @Attribute(name = "show-childs-count", required = false)
    private Boolean showChildsCount = false;

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

    public String getBreadCrumbColumn() {
        return breadCrumbColumn;
    }

    public void setBreadCrumbColumn(String breadCrumbColumn) {
        this.breadCrumbColumn = breadCrumbColumn;
    }

    public Integer getBreadCrumbMaxChars() {
        return breadCrumbMaxChars;
    }

    public void setBreadCrumbMaxChars(Integer breadCrumbMaxChars) {
        this.breadCrumbMaxChars = breadCrumbMaxChars;
    }

    public void setBreadCrumb(String breadCrumb) {
        this.breadCrumb = breadCrumb;
    }

    public Boolean getHideArrowIfEmpty() {
        return hideArrowIfEmpty;
    }

    public void setHideArrowIfEmpty(Boolean hideArrowIfEmpty) {
        this.hideArrowIfEmpty = hideArrowIfEmpty;
    }

    public Boolean getShowChildsCount() {
        return showChildsCount;
    }

    public void setShowChildsCount(Boolean showChildsCount) {
        this.showChildsCount = showChildsCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChildCollectionViewerConfig that = (ChildCollectionViewerConfig) o;

        if (hideArrowIfEmpty != that.hideArrowIfEmpty) return false;
        if (showChildsCount != that.showChildsCount) return false;
        if (forDomainObjectType != null ? !forDomainObjectType.equals(that.forDomainObjectType) : that.forDomainObjectType != null)
            return false;
        if (filter != null ? !filter.equals(that.filter) : that.filter != null) return false;
        if (domainObjectTypeToCreate != null ? !domainObjectTypeToCreate.equals(that.domainObjectTypeToCreate) : that.domainObjectTypeToCreate != null)
            return false;
        if (breadCrumb != null ? !breadCrumb.equals(that.breadCrumb) : that.breadCrumb != null) return false;
        if (breadCrumbColumn != null ? !breadCrumbColumn.equals(that.breadCrumbColumn) : that.breadCrumbColumn != null) return false;
        if (breadCrumbMaxChars != null ? !breadCrumbMaxChars.equals(that.breadCrumbMaxChars) : that.breadCrumbMaxChars != null) return false;
        if (collectionViewerConfig != null ? !collectionViewerConfig.equals(that.collectionViewerConfig) : that.collectionViewerConfig != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = forDomainObjectType != null ? forDomainObjectType.hashCode() : 0;
        result = 31 * result + (filter != null ? filter.hashCode() : 0);
        result = 31 * result + (domainObjectTypeToCreate != null ? domainObjectTypeToCreate.hashCode() : 0);
        result = 31 * result + (breadCrumb != null ? breadCrumb.hashCode() : 0);
        result = 31 * result + (hideArrowIfEmpty ? 1 : 0);
        result = 31 * result + (showChildsCount ? 1 : 0);
        result = 31 * result + (collectionViewerConfig != null ? collectionViewerConfig.hashCode() : 0);

        return result;
    }

}
