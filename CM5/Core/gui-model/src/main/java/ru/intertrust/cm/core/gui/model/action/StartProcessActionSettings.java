package ru.intertrust.cm.core.gui.model.action;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.gui.action.ActionSettings;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StartProcessActionSettings that = (StartProcessActionSettings) o;

        if (processName != null ? !processName.equals(that.processName) : that.processName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return processName != null ? processName.hashCode() : 0;
    }
}
