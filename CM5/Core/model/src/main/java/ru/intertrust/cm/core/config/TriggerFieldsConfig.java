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
    
}
