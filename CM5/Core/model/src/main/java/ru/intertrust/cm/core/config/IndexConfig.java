package ru.intertrust.cm.core.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;

/**
 * @author vmatsukevich
 *         Date: 5/16/13
 *         Time: 10:52 AM
 */
public class IndexConfig implements Serializable {

    public enum IndexType {BTREE("btree");

        private String value;

        IndexType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    @Attribute(name = "type", required = false)
    private String type;

    @ElementListUnion({
            @ElementList(entry = "field", type = IndexFieldConfig.class, inline = true, required = false),
            @ElementList(entry = "expr", type = IndexExpressionConfig.class, inline = true, required = false)
    })
    private List<BaseIndexExpressionConfig> indexExspressionConfigs = new ArrayList<>();

    public IndexConfig() {
    }
    
    public String getType() {
        return type;
    }

    public List<BaseIndexExpressionConfig> getIndexFieldConfigs() {
        return indexExspressionConfigs;
    }

    public void setIndexFieldConfigs(List<BaseIndexExpressionConfig> indexFieldConfigs) {
        if (indexFieldConfigs != null) {
            this.indexExspressionConfigs = indexFieldConfigs;
        } else {
            this.indexExspressionConfigs.clear();
        }
    }

    
    @Override
    public String toString() {
        return "IndexConfig [type=" + type + ", indexExspressionConfigs=" + indexExspressionConfigs + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        IndexConfig that = (IndexConfig) o;

        if (that.indexExspressionConfigs == null) {
            return indexExspressionConfigs == null;
        }

        if (indexExspressionConfigs == null) {
            return false;
        }

        if (!(indexExspressionConfigs.size() == that.indexExspressionConfigs.size() && indexExspressionConfigs.containsAll(that.indexExspressionConfigs))) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return indexExspressionConfigs != null ? indexExspressionConfigs.hashCode() : 0;
    }
    
    
}
