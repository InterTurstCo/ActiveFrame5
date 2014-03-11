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
public class NotificationTriggersConfig implements Dto {

    @ElementList(entry="trigger", type=TriggerConfig.class, inline=true, required = false)
    private List<TriggerConfig> triggers = new ArrayList<TriggerConfig>();

    public List<TriggerConfig> getTriggers() {
        return triggers;
    }

    public void setTriggers(List<TriggerConfig> triggers) {
        this.triggers = triggers;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((triggers == null) ? 0 : triggers.hashCode());
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
        NotificationTriggersConfig other = (NotificationTriggersConfig) obj;
        if (triggers == null) {
            if (other.triggers != null) {
                return false;
            }
        } else if (!triggers.equals(other.triggers)) {
            return false;
        }
        return true;
    }    
    
}
