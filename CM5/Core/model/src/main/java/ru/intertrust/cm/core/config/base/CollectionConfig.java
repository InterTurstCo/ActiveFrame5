package ru.intertrust.cm.core.config.base;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * Java модель конфигурации одной коллекции
 * @author atsvetkov
 *
 */
@Root(name = "collection")
public class CollectionConfig implements TopLevelConfig {

    @Attribute(required = true)
    private String name;

    @Attribute(required = true)
    private String idField;

    public enum TransactionCacheType {
        enabled("enabled"),
        disabled("disabled");

        private String value;

        TransactionCacheType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    @Attribute(required = false, name = "transaction-cache")
    private TransactionCacheType transactionCache;

    @Element(name = "prototype", required = false, data=true)
    private String prototype;

    @Element(name = "counting-prototype", required = false, data=true)
    private String countingPrototype;

    @ElementList(entry = "filter", required = false, inline=true)
    private List<CollectionFilterConfig> filters = new ArrayList<CollectionFilterConfig>();

    @Element(name = "renderer", required = false)
    private CollectionRendererConfig renderer;

    @Override
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

    public TransactionCacheType getTransactionCache() {
        return transactionCache;
    }

    public void setTransactionCache(TransactionCacheType transactionCache) {
        this.transactionCache = transactionCache;
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
        if(filters != null) {
            this.filters = filters;
        } else {
            this.filters.clear();
        }
    }

    public CollectionRendererConfig getRenderer() {
        return renderer;
    }

    public void setRenderer(CollectionRendererConfig renderer) {
        this.renderer = renderer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CollectionConfig that = (CollectionConfig) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (idField != null ? !idField.equals(that.idField) : that.idField != null) {
            return false;
        }
        if (countingPrototype != null ? !countingPrototype.equals(that.countingPrototype) : that.countingPrototype != null) {
            return false;
        }
        if (filters != null ? !filters.equals(that.filters) : that.filters != null) {
            return false;
        }
        if (prototype != null ? !prototype.equals(that.prototype) : that.prototype != null) {
            return false;
        }
        if (renderer != null ? !renderer.equals(that.renderer) : that.renderer != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (idField != null ? idField.hashCode() : 0);
        return result;
    }
}
