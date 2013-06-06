package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

/**
 * 
 * @author atsvetkov
 *
 */
public class CollectionFilter {

    @Attribute(required = true)
    private String name;

    @Element(name = "reference", required = false)
    private CollectionFilterReference collectionFilterReference;
    
    @Element(name = "criteria", required = false)
    private CollectionFilterCriteria collectionFilterCriteria;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CollectionFilterReference getCollectionFilterReference() {
        return collectionFilterReference;
    }

    public void setCollectionFilterReference(CollectionFilterReference collectionFilterReference) {
        this.collectionFilterReference = collectionFilterReference;
    }

    public CollectionFilterCriteria getCollectionFilterCriteria() {
        return collectionFilterCriteria;
    }

    public void setCollectionFilterCriteria(CollectionFilterCriteria collectionFilterCriteria) {
        this.collectionFilterCriteria = collectionFilterCriteria;
    }        
        
}
