package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;

public class CompleteTaskActionContext extends SaveActionContext {
    private Id taskId;
    private String taskAction;
    private String activityId;

    public CompleteTaskActionContext() {
    }

    public CompleteTaskActionContext(ActionConfig actionConfig) {
        super(actionConfig);
    }

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

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }
}
