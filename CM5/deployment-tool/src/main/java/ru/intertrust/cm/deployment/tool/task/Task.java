package ru.intertrust.cm.deployment.tool.task;

import java.util.concurrent.Callable;

/**
 * Created by Alexander Bogatyrenko on 08.08.16.
 * <p>
 * This class represents...
 */
public interface Task extends Callable<TaskResult> {

    TaskContext getContext();
    void setContext(TaskContext context);
}
