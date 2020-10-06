package ru.intertrust.cm.core.business.api.workflow;

import java.util.List;
import org.flowable.engine.delegate.ExecutionListener;
import org.flowable.engine.delegate.TaskListener;

public interface GlobalListeners {
    public static final String NBR_PROCESS_TARGET_NAMESPACE = "http://inttrust.ru/nbr";

    void addCreateTaskListener(String targetNamespace, TaskListener taskListener);

    void addDeleteTaskListener(String targetNamespace, TaskListener taskListener);

    void addStartProcessListener(String targetNamespace, ExecutionListener executionListener);

    void addEndProcessListener(String targetNamespace, ExecutionListener executionListener);

    List<TaskListener> getCreateTaskListeners(String targetNamespace);

    List<TaskListener> getDeleteTaskListeners(String targetNamespace);

    List<ExecutionListener> getStartProcessListeners(String targetNamespace);

    List<ExecutionListener> getEndProcessListeners(String targetNamespace);
}
