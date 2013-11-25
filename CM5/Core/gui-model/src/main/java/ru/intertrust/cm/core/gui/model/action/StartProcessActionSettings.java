package ru.intertrust.cm.core.gui.model.action;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import ru.intertrust.cm.core.config.gui.ActionSettings;

@Root(name="start-process-action-settings")
public class StartProcessActionSettings implements ActionSettings {

    /**
     * имя процесса
     */
    @Attribute(required=true, name="process-name")
    private String processName;

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    @Override
    public Class<?> getActionContextClass() {
        return StartProcessActionContext.class;
    }

}
