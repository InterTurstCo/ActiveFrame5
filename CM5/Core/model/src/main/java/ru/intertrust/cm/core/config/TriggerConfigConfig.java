package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Element;

public class TriggerConfigConfig {

    @Element(name = "fields", required = false)
    private TriggerFieldsConfig triggerFieldsConfig;

    @Element(name = "statuses", required = false)
    private TriggerStatusesConfig triggerStatusesConfig;

    
}
