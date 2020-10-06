package ru.intertrust.cm.core.process;

import org.flowable.engine.RepositoryService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.tools.SpringClient;

public class GlobalListener extends SpringClient {

    @Autowired
    private RepositoryService repositoryService;

    protected String getProcessTargetNamespace(DelegateTask delegateTask){
        ProcessDefinition processDefinition = repositoryService.getProcessDefinition(
                delegateTask.getProcessDefinitionId());
        return processDefinition.getCategory();
    }

    protected String getProcessTargetNamespace(DelegateExecution delegateExecution){
        ProcessDefinition processDefinition = repositoryService.getProcessDefinition(
                delegateExecution.getProcessDefinitionId());
        return processDefinition.getCategory();
    }

}
