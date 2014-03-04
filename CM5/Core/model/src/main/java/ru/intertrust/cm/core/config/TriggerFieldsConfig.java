package ru.intertrust.cm.core.config;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.ElementList;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * 
 * @author atsvetkov
 *
 */
public class TriggerFieldsConfig implements Dto {

    @ElementList(entry="field", type=TriggerFieldConfig.class, inline=true, required = false)
    private List<TriggerFieldConfig> triggerFieldsConfig = new ArrayList<TriggerFieldConfig>();

    public List<TriggerFieldConfig> getFields() {
        return triggerFieldsConfig;
    }

    public void setFields(List<TriggerFieldConfig> triggerFieldsConfig) {

        if (triggerFieldsConfig != null) {
            this.triggerFieldsConfig = triggerFieldsConfig;
        } else {
            this.triggerFieldsConfig.clear();
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((triggerFieldsConfig == null) ? 0 : triggerFieldsConfig.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TriggerFieldsConfig other = (TriggerFieldsConfig) obj;
        if (triggerFieldsConfig == null) {
            if (other.triggerFieldsConfig != null) {
                return false;
            }
        } else if (!triggerFieldsConfig.equals(other.triggerFieldsConfig)) {
            return false;
        }
        return true;
    }
    
    
}
