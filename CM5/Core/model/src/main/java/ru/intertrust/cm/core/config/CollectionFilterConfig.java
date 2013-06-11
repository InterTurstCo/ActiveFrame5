package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

/**
 * 
 * @author atsvetkov
 *
 */
public class CollectionFilterConfig {

    @Attribute(required = true)
    private String name;

    @Element(name = "reference", required = false)
    private CollectionFilterReference filterReference;
    
    @Element(name = "criteria", required = false)
    private CollectionFilterCriteria filterCriteria;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CollectionFilterReference getFilterReference() {
        return filterReference;
    }

    public void setFilterReference(CollectionFilterReference filterReference) {
        this.filterReference = filterReference;
    }

    public CollectionFilterCriteria getFilterCriteria() {
        return filterCriteria;
    }

    public void setFilterCriteria(CollectionFilterCriteria filterCriteria) {
        this.filterCriteria = filterCriteria;
    }        
        
}
