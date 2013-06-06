package ru.intertrust.cm.core.config;

import java.util.List;

import org.simpleframework.xml.ElementList;

/**
 * 
 * @author atsvetkov
 *
 */
public class CollectionDisplayConfig {

    @ElementList(entry="column", type=CollectionColumnConfig.class, inline=true)
    private List<CollectionColumnConfig> columnConfig;

    public List<CollectionColumnConfig> getColumnConfig() {
        return columnConfig;
    }

    public void setColumnConfig(List<CollectionColumnConfig> columnConfig) {
        this.columnConfig = columnConfig;
    }

}
