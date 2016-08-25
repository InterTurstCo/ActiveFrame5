package ru.intertrust.cm.deployment.tool.task;

/**
 * Created by Alexander Bogatyrenko on 08.08.16.
 * <p>
 * This class represents...
 */
public class TaskContext {

    private String ear;

    public TaskContext(String ear) {
        this.ear = ear;
    }

    public String getEar() {
        return ear;
    }

    public void setEar(String ear) {
        this.ear = ear;
    }
}
