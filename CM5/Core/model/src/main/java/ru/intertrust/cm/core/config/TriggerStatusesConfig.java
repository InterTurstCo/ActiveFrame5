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

    public List<TriggerStatusConfig> getStatuses() {
        return triggerStatusesConfig;
    }

    public void setStatuses(List<TriggerStatusConfig> triggerStatusesConfig) {

        if (triggerStatusesConfig != null) {
            this.triggerStatusesConfig = triggerStatusesConfig;
        } else {
            this.triggerStatusesConfig.clear();
        }

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((triggerStatusesConfig == null) ? 0 : triggerStatusesConfig.hashCode());
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
        TriggerStatusesConfig other = (TriggerStatusesConfig) obj;
        if (triggerStatusesConfig == null) {
            if (other.triggerStatusesConfig != null) {
                return false;
            }
        } else if (!triggerStatusesConfig.equals(other.triggerStatusesConfig)) {
            return false;
        }
        return true;
    }        
    
}
