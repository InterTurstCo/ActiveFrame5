package ru.intertrust.cm.core.process;

import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.task.service.delegate.TaskListener;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.workflow.GlobalListeners;

/**
 * Глобальный слушатель удаления задач в WF
 */
public class GlobalDeleteTaskListener extends GlobalListener implements TaskListener {
    @Autowired
    private GlobalListeners globalListneres;

    @Override
    public void notify(DelegateTask delegateTask) {
        for (TaskListener taskListener : globalListneres.getDeleteTaskListeners(getProcessTargetNamespace(delegateTask))) {
            taskListener.notify(delegateTask);
        }
    }
}
