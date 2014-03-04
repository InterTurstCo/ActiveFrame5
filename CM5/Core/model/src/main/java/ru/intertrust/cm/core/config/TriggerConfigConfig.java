package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Element;

/**
 * 
 * @author atsvetkov
 *
 */
public class TriggerConfigConfig {

    @Element(name = "fields", required = false)
    private TriggerFieldsConfig triggerFieldsConfig;

    @Element(name = "statuses", required = false)
    private TriggerStatusesConfig triggerStatusesConfig;

    public TriggerFieldsConfig getTriggerFieldsConfig() {
        return triggerFieldsConfig;
    }

    public void setTriggerFieldsConfig(TriggerFieldsConfig triggerFieldsConfig) {
        this.triggerFieldsConfig = triggerFieldsConfig;
    }

    public TriggerStatusesConfig getTriggerStatusesConfig() {
        return triggerStatusesConfig;
    }

    public void setTriggerStatusesConfig(TriggerStatusesConfig triggerStatusesConfig) {
        this.triggerStatusesConfig = triggerStatusesConfig;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((triggerFieldsConfig == null) ? 0 : triggerFieldsConfig.hashCode());
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
        TriggerConfigConfig other = (TriggerConfigConfig) obj;
        if (triggerFieldsConfig == null) {
            if (other.triggerFieldsConfig != null) {
                return false;
            }
        } else if (!triggerFieldsConfig.equals(other.triggerFieldsConfig)) {
            return false;
        }
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
