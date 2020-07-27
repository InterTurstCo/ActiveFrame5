package ru.intertrust.cm.core.process;

import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.task.service.delegate.TaskListener;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.workflow.TaskListeners;

/**
 * Глобальный слушатель создания задач в WF
 */
public class GlobalCreateTaskListener extends GlobalTaskListener implements TaskListener {

    @Autowired
    private TaskListeners taskListneres;

    @Override
    public void notify(DelegateTask delegateTask) {
        for (TaskListener taskListener : taskListneres.getCreateTaskListeners(getProcessTargetNamespace(delegateTask))) {
            taskListener.notify(delegateTask);
        }
    }
}
