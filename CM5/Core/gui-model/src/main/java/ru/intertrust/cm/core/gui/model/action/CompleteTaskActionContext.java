package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.business.api.dto.Id;

public class CompleteTaskActionContext extends ActionContext {
    private Id taskId;
    private String taskAction;

    public Id getTaskId() {
        return taskId;
    }

    public void setTaskId(Id taskId) {
        this.taskId = taskId;
    }

    public String getTaskAction() {
        return taskAction;
    }

    public void setTaskAction(String taskAction) {
        this.taskAction = taskAction;
    }
}
