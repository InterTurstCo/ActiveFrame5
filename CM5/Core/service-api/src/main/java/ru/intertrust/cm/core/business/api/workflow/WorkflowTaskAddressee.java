package ru.intertrust.cm.core.business.api.workflow;

public class WorkflowTaskAddressee {
    private String name;
    private boolean group;

    public WorkflowTaskAddressee() {
    }

    public WorkflowTaskAddressee(String name, boolean group) {
        this.name = name;
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isGroup() {
        return group;
    }

    public void setGroup(boolean group) {
        this.group = group;
    }
}
