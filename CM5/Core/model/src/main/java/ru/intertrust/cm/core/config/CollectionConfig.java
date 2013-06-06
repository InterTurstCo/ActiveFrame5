package ru.intertrust.cm.core.config;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * Java модель конфигурации одной коллекции
 * @author atsvetkov
 *
 */
@Root(name = "collection")
public class CollectionConfig {

    @Attribute(required = true)
    private String name;

    @Attribute(required = true)
    private String idField;

    @Attribute(name = "businessObjectTypeField", required = true)
    private String businessObjectTypeField;

    @Element(name = "display")
    private CollectionDisplayConfig displayConfig;

    @Element(name = "prototype", required = false, data=true)
    private String prototype;    

    @Element(name = "counting-prototype", required = false, data=true)
    private String countingPrototype;        

    @ElementList(entry = "filter", required = false, inline=true)
    private List<CollectionFilter> collectionFilter;    

    @Element(name = "renderer", required = false)
    private CollectionRenderer renderer;    
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdField() {
        return idField;
    }

    public void setIdField(String idField) {
        this.idField = idField;
    }

    public String getBusinessObjectTypeField() {
        return businessObjectTypeField;
    }

    public void setBusinessObjectTypeField(String businessObjectTypeField) {
        this.businessObjectTypeField = businessObjectTypeField;
    }

    public CollectionDisplayConfig getDisplayConfig() {
        return displayConfig;
    }

    public void setDisplayConfig(CollectionDisplayConfig displayConfig) {
        this.displayConfig = displayConfig;
    }
    
    public String getPrototype() {
        return prototype;
    }

    public void setPrototype(String prototype) {
        this.prototype = prototype;
    }

    public String getCountingPrototype() {
        return countingPrototype;
    }

    public void setCountingPrototype(String countingPrototype) {
        this.countingPrototype = countingPrototype;
    }

    public List<CollectionFilter> getCollectionFilter() {
        return collectionFilter;
    }

    public void setCollectionFilter(List<CollectionFilter> collectionFilter) {
        this.collectionFilter = collectionFilter;
    }
    
    public CollectionRenderer getRenderer() {
        return renderer;
    }

    public void setRenderer(CollectionRenderer renderer) {
        this.renderer = renderer;
    }

}
