package ru.intertrust.cm.core.gui.model.action;

import org.simpleframework.xml.Attribute;
import ru.intertrust.cm.core.config.gui.action.ActionSettings;

public class CompleteTaskActionSettings implements ActionSettings {

    /**
     * имя класса экшена
     */
    @Attribute(required=true, name="class-name")
    private String className;

    /**
     * имя процесса
     */
    @Attribute(required=true, name="process-name")
    private String processName;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    @Override
    public Class<?> getActionContextClass() {
        return CompleteTaskActionContext.class;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompleteTaskActionSettings that = (CompleteTaskActionSettings) o;

        if (className != null ? !className.equals(that.className) : that.className != null) return false;
        if (processName != null ? !processName.equals(that.processName) : that.processName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return className != null ? className.hashCode() : 0;
    }
}