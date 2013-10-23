package ru.intertrust.cm.core.config.model;

import org.simpleframework.xml.Attribute;

public class StartProcessActionConfig implements ActionSettings {

    @Attribute(required = true)
    private String processName;

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

}
