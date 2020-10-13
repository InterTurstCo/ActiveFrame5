package ru.intertrust.cm.core.process;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.task.service.delegate.TaskListener;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.workflow.GlobalListeners;


public class GlobalStartProcessListener extends GlobalListener implements ExecutionListener {
    @Autowired
    private GlobalListeners globalListneres;

    @Override
    public void notify(DelegateExecution execution) {
        for (ExecutionListener executionListener
                : globalListneres.getStartProcessListeners(getProcessTargetNamespace(execution))) {
            executionListener.notify(execution);
        }
    }
}
