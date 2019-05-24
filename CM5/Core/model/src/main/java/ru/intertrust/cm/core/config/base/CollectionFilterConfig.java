package ru.intertrust.cm.core.config.base;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

import java.io.Serializable;

/**
 *
 * @author atsvetkov
 *
 */
public class CollectionFilterConfig implements Serializable {

    @Attribute(required = true)
    private String name;

    @Element(name = "reference", required = false)
    private CollectionFilterReferenceConfig filterReference;

    @Element(name = "criteria", required = false)
    private CollectionFilterCriteriaConfig filterCriteria;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CollectionFilterReferenceConfig getFilterReference() {
        return filterReference;
    }

    public void setFilterReference(CollectionFilterReferenceConfig filterReference) {
        this.filterReference = filterReference;
    }

    public CollectionFilterCriteriaConfig getFilterCriteria() {
        return filterCriteria;
    }

    public void setFilterCriteria(CollectionFilterCriteriaConfig filterCriteria) {
        this.filterCriteria = filterCriteria;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CollectionFilterConfig that = (CollectionFilterConfig) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (filterReference != null ? !filterReference.equals(that.filterReference) : that.filterReference != null) {
            return false;
        }
        if (filterCriteria != null ? !filterCriteria.equals(that.filterCriteria) : that.filterCriteria != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (filterReference != null ? filterReference.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CollectionFilterConfig [name=" + name + ", filterReference=" + filterReference + ", filterCriteria=" + filterCriteria + "]";
    }
}
