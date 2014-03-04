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
        
}
