package ru.intertrust.cm.core.config.model;

import org.simpleframework.xml.ElementList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author vmatsukevich
 *         Date: 5/16/13
 *         Time: 10:52 AM
 */
public class UniqueKeyConfig implements Serializable {

    @ElementList(entry="field", inline=true)
    private List<UniqueKeyFieldConfig> uniqueKeyFieldConfigs;

    public UniqueKeyConfig() {
    }

    public List<UniqueKeyFieldConfig> getUniqueKeyFieldConfigs() {
        if(uniqueKeyFieldConfigs == null) {
            uniqueKeyFieldConfigs = new ArrayList<UniqueKeyFieldConfig>();
        }
        return uniqueKeyFieldConfigs;
    }

    public void setUniqueKeyFieldConfigs(List<UniqueKeyFieldConfig> uniqueKeyFieldConfigs) {
        this.uniqueKeyFieldConfigs = uniqueKeyFieldConfigs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UniqueKeyConfig that = (UniqueKeyConfig) o;

        if (uniqueKeyFieldConfigs != null ? !uniqueKeyFieldConfigs.equals(that.uniqueKeyFieldConfigs) : that.uniqueKeyFieldConfigs != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return uniqueKeyFieldConfigs != null ? uniqueKeyFieldConfigs.hashCode() : 0;
    }
}
