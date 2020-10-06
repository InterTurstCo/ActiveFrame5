package ru.intertrust.cm.core.process;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.workflow.GlobalListeners;

public class GlobalEndProcessListener extends GlobalListener implements ExecutionListener {
    @Autowired
    private GlobalListeners globalListneres;

    @Override
    public void notify(DelegateExecution execution) {
        for (ExecutionListener executionListener
                : globalListneres.getEndProcessListeners(getProcessTargetNamespace(execution))) {
            executionListener.notify(execution);
        }
    }

}