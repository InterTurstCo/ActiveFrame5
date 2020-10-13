package ru.intertrust.cm.core.process;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.flowable.engine.delegate.ExecutionListener;
import org.flowable.engine.delegate.TaskListener;
import ru.intertrust.cm.core.business.api.workflow.GlobalListeners;

public class GlobalListnersImpl implements GlobalListeners {

    Map<String, List<Serializable>> globalListeners = new HashMap<>();

    @Override
    public void addCreateTaskListener(String targetNamespace, TaskListener taskListener) {
        getGlobalListeners(targetNamespace, TaskListener.EVENTNAME_CREATE).add(taskListener);
    }

    @Override
    public void addDeleteTaskListener(String targetNamespace, TaskListener taskListener) {
        getGlobalListeners(targetNamespace, TaskListener.EVENTNAME_DELETE).add(taskListener);
    }

    @Override
    public void addStartProcessListener(String targetNamespace, ExecutionListener executionListener) {
        getGlobalListeners(targetNamespace, ExecutionListener.EVENTNAME_START).add(executionListener);
    }

    @Override
    public void addEndProcessListener(String targetNamespace, ExecutionListener executionListener) {
        getGlobalListeners(targetNamespace, ExecutionListener.EVENTNAME_END).add(executionListener);
    }

    private <T> List<T> getGlobalListeners(String targetNamespace, String event) {
        List<Serializable> result = globalListeners.get(getGlobalListenerKey(targetNamespace, event));
        if (result == null){
            result = new ArrayList<>();
            globalListeners.put(getGlobalListenerKey(targetNamespace, event), result);
        }
        return (List<T>)result;
    }

    private String getGlobalListenerKey(String targetNamespace, String event){
        return targetNamespace + "&" + event;
    }

    @Override
    public List<TaskListener> getCreateTaskListeners(String targetNamespace) {
        return getGlobalListeners(targetNamespace, TaskListener.EVENTNAME_CREATE);
    }

    @Override
    public List<TaskListener> getDeleteTaskListeners(String targetNamespace) {
        return getGlobalListeners(targetNamespace, TaskListener.EVENTNAME_DELETE);
    }

    @Override
    public List<ExecutionListener> getStartProcessListeners(String targetNamespace) {
        return getGlobalListeners(targetNamespace, ExecutionListener.EVENTNAME_START);
    }

    @Override
    public List<ExecutionListener> getEndProcessListeners(String targetNamespace) {
        return getGlobalListeners(targetNamespace, ExecutionListener.EVENTNAME_END);
    }
}
