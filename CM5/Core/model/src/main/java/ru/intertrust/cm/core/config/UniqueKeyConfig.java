package ru.intertrust.cm.core.config;

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
}
