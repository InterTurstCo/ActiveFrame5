package ru.intertrust.cm.core.process;

import javax.annotation.PostConstruct;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.workflow.GlobalListeners;

public class PlatformEndProcessListener implements ExecutionListener {
    private Logger logger = LoggerFactory.getLogger(PlatformStartProcessListener.class);

    @Autowired
    private GlobalListeners globalListeners;

    @PostConstruct
    public void init(){
        globalListeners.addEndProcessListener(GlobalListeners.NBR_PROCESS_TARGET_NAMESPACE, this);
    }

    @Override
    public void notify(DelegateExecution execution) {
        logger.debug("End process {}", execution.getProcessDefinitionId());
    }

}
