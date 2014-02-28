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
public class TriggerStatusesConfig implements Dto {

    @ElementList(entry = "status", type = TriggerStatusConfig.class, inline = true, required = false)
    private List<TriggerStatusConfig> triggerStatusesConfig = new ArrayList<TriggerStatusConfig>();

    public List<TriggerStatusConfig> getTriggerStatusesConfig() {
        return triggerStatusesConfig;
    }

    public void setTriggerStatusesConfig(List<TriggerStatusConfig> triggerStatusesConfig) {

        if (triggerStatusesConfig != null) {
            this.triggerStatusesConfig = triggerStatusesConfig;
        } else {
            this.triggerStatusesConfig.clear();
        }

    }
    
}
