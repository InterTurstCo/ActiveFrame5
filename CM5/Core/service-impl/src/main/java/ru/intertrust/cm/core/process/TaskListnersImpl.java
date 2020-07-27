package ru.intertrust.cm.core.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.flowable.engine.delegate.TaskListener;
import ru.intertrust.cm.core.business.api.workflow.TaskListeners;

public class TaskListnersImpl implements TaskListeners {

    Map<String, List<TaskListener>> createTaskListeners = new HashMap<>();
    Map<String, List<TaskListener>> deleteTaskListeners = new HashMap<>();

    @Override
    public void addCreateTaskListener(String targetNamespace, TaskListener taskListener) {
        getCreateTaskListeners(targetNamespace).add(taskListener);
    }

    @Override
    public void addDeleteTaskListener(String targetNamespace, TaskListener taskListener) {
        getDeleteTaskListeners(targetNamespace).add(taskListener);
    }

    @Override
    public List<TaskListener> getCreateTaskListeners(String targetNamespace) {
        List<TaskListener> result = createTaskListeners.get(targetNamespace);
        if (result == null){
            result = new ArrayList<>();
            createTaskListeners.put(targetNamespace, result);
        }
        return result;
    }

    @Override
    public List<TaskListener> getDeleteTaskListeners(String targetNamespace) {
        List<TaskListener> result = deleteTaskListeners.get(targetNamespace);
        if (result == null){
            result = new ArrayList<>();
            deleteTaskListeners.put(targetNamespace, result);
        }
        return result;
    }
}
