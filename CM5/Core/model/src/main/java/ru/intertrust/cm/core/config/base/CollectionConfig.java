package ru.intertrust.cm.core.config.base;

import org.simpleframework.xml.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Java модель конфигурации одной коллекции
 * @author atsvetkov
 *
 */
@Root(name = "collection")
@Order(elements={"prototype", "counting-prototype", "generator", "filter"})
public class CollectionConfig implements TopLevelConfig {

    @Attribute(required = true)
    private String name;

    @Attribute(name = "replace", required = false)
    private String replacementPolicy;

    @Attribute(required = true)
    private String idField;

    @Attribute(required = false)
    private boolean useClone = false;

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

    @Element(name = "generator", required = false)
    private CollectionGeneratorConfig generator;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ExtensionPolicy getReplacementPolicy() {
        return ExtensionPolicy.fromString(replacementPolicy);
    }

    @Override
    public ExtensionPolicy getCreationPolicy() {
        return ExtensionPolicy.Runtime;
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

    public CollectionGeneratorConfig getGenerator() {
        return generator;
    }

    public void setGenerator(CollectionGeneratorConfig generator) {
        this.generator = generator;
    }

    public boolean isUseClone() {
        return useClone;
    }

    public void setUseClone(boolean useClone) {
        this.useClone = useClone;
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
        if (replacementPolicy != null ? !replacementPolicy.equals(that.replacementPolicy) : that.replacementPolicy != null) {
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
        if (generator != null ? !generator.equals(that.generator) : that.generator != null) {
            return false;
        }
        if (useClone != that.useClone) {
            return false;
        }
        if (transactionCache != that.transactionCache) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (idField != null ? idField.hashCode() : 0);
        result = 31 * result + (transactionCache == TransactionCacheType.enabled ? 71 : 0);
        return result;
    }
}
