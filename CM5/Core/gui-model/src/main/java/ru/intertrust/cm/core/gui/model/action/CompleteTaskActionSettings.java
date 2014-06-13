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

}