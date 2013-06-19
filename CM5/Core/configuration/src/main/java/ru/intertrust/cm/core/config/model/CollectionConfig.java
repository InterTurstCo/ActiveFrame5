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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CollectionConfig that = (CollectionConfig) o;

        if (countingPrototype != null ? !countingPrototype.equals(that.countingPrototype) : that.countingPrototype != null)
            return false;
        if (displayConfig != null ? !displayConfig.equals(that.displayConfig) : that.displayConfig != null)
            return false;
        if (domainObjectType != null ? !domainObjectType.equals(that.domainObjectType) : that.domainObjectType != null)
            return false;
        if (filters != null ? !filters.equals(that.filters) : that.filters != null) return false;
        if (idField != null ? !idField.equals(that.idField) : that.idField != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (prototype != null ? !prototype.equals(that.prototype) : that.prototype != null) return false;
        if (renderer != null ? !renderer.equals(that.renderer) : that.renderer != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (idField != null ? idField.hashCode() : 0);
        result = 31 * result + (domainObjectType != null ? domainObjectType.hashCode() : 0);
        result = 31 * result + (displayConfig != null ? displayConfig.hashCode() : 0);
        result = 31 * result + (prototype != null ? prototype.hashCode() : 0);
        result = 31 * result + (countingPrototype != null ? countingPrototype.hashCode() : 0);
        result = 31 * result + (filters != null ? filters.hashCode() : 0);
        result = 31 * result + (renderer != null ? renderer.hashCode() : 0);
        return result;
    }
}
