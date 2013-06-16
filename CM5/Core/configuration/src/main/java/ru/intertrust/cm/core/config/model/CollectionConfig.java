package ru.intertrust.cm.core.config.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.io.Serializable;
import java.util.List;

/**
 * Java модель конфигурации одной коллекции
 * @author atsvetkov
 *
 */
@Root(name = "collection")
public class CollectionConfig implements Serializable {

    @Attribute(required = true)
    private String name;

    @Attribute(required = true)
    private String idField;

    @Attribute(name = "domain-object-type", required = true)
    private String domainObjectType;

    @Element(name = "display")
    private CollectionDisplayConfig displayConfig;

    @Element(name = "prototype", required = false, data=true)
    private String prototype;

    @Element(name = "counting-prototype", required = false, data=true)
    private String countingPrototype;

    @ElementList(entry = "filter", required = false, inline=true)
    private List<CollectionFilterConfig> filters;

    @Element(name = "renderer", required = false)
    private CollectionRendererConfig renderer;

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

    public String getDomainObjectType() {
        return domainObjectType;
    }

    public void setDomainObjectType(String domainObjectType) {
        this.domainObjectType = domainObjectType;
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

    public List<CollectionFilterConfig> getFilters() {
        return filters;
    }

    public void setFilters(List<CollectionFilterConfig> filters) {
        this.filters = filters;
    }

    public CollectionRendererConfig getRenderer() {
        return renderer;
    }

    public void setRenderer(CollectionRendererConfig renderer) {
        this.renderer = renderer;
    }

}
