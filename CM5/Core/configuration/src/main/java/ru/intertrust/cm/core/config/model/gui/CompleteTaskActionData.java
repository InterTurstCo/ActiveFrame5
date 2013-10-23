package ru.intertrust.cm.core.config.model.gui;

import org.simpleframework.xml.Attribute;

public class CompleteTaskActionData implements ActionSettings {

    @Attribute(required = true)
    private String processName;

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

}
