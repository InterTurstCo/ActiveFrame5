package ru.intertrust.cm.core.process;

import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.task.service.delegate.TaskListener;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.workflow.GlobalListeners;

/**
 * Глобальный слушатель создания задач в WF
 */
public class GlobalCreateTaskListener extends GlobalListener implements TaskListener {

    @Autowired
    private GlobalListeners globalListeners;

    @Override
    public void notify(DelegateTask delegateTask) {
        for (TaskListener taskListener : globalListeners.getCreateTaskListeners(getProcessTargetNamespace(delegateTask))) {
            taskListener.notify(delegateTask);
        }
    }
}
