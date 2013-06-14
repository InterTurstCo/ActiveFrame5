package ru.intertrust.cm.core.config;

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

}
