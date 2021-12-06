package ru.intertrust.cm.core.business.api.workflow;

import java.util.ArrayList;
import java.util.List;

public class WorkflowTaskData {
    private String taskId;
    private String processId;
    private String activityId;
    private String name;
    private String description;
    private long priority;
    private String context;
    private String actions;
    private String result;
    private String executionId;
    private List<WorkflowTaskAddressee> addressee = new ArrayList<>();

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getPriority() {
        return priority;
    }

    public void setPriority(long priority) {
        this.priority = priority;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getActions() {
        return actions;
    }

    public void setActions(String actions) {
        this.actions = actions;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public List<WorkflowTaskAddressee> getAddressee() {
        return addressee;
    }

    public void setAddressee(List<WorkflowTaskAddressee> addressee) {
        this.addressee = addressee;
    }

    public void addAddressee(WorkflowTaskAddressee addressee) {
        this.addressee.add(addressee);
    }
}
