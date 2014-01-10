package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author vmatsukevich
 *         Date: 5/16/13
 *         Time: 10:52 AM
 */
public class IndexConfig implements Serializable {

    public enum IndexType {BTREE("b-tree"), HASH("hash");

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

    @ElementList(entry="field", inline=true)
    private List<IndexFieldConfig> indexFieldConfigs = new ArrayList<>();

    public IndexConfig() {
    }
    
    public String getType() {
        return type;
    }

    public List<IndexFieldConfig> getIndexFieldConfigs() {
        return indexFieldConfigs;
    }

    public void setIndexFieldConfigs(List<IndexFieldConfig> indexFieldConfigs) {
        if(indexFieldConfigs != null) {
            this.indexFieldConfigs = indexFieldConfigs;
        } else {
            this.indexFieldConfigs.clear();
        }
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

        if (indexFieldConfigs != null ? !indexFieldConfigs.equals(that.indexFieldConfigs) : that.indexFieldConfigs != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return indexFieldConfigs != null ? indexFieldConfigs.hashCode() : 0;
    }
}
