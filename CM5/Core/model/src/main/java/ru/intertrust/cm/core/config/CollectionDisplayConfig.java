package ru.intertrust.cm.core.config;

import org.simpleframework.xml.ElementList;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author atsvetkov
 *
 */
public class CollectionDisplayConfig implements Serializable {

    @ElementList(entry="column", type=CollectionColumnConfig.class, inline=true)
    private List<CollectionColumnConfig> columnConfig;

    public List<CollectionColumnConfig> getColumnConfig() {
        return columnConfig;
    }

    public void setColumnConfig(List<CollectionColumnConfig> columnConfig) {
        this.columnConfig = columnConfig;
    }

}
