package ru.intertrust.cm.core.business.impl.workflow;

import org.activiti.engine.FormService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.runtime.Execution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.DeployedProcess;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.ProcessVariable;
import ru.intertrust.cm.core.business.api.workflow.WorkflowTaskData;
import ru.intertrust.cm.core.model.ProcessException;
import ru.intertrust.cm.core.tools.DomainObjectAccessor;
import ru.intertrust.cm.core.tools.Session;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivitiWorkflowEngineImpl extends AbstactWorkflowEngine {
    private static final Logger logger = LoggerFactory.getLogger(ActivitiWorkflowEngineImpl.class);
    public static final String ENGENE_NAME = "activiti";

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    FormService formService;

    @Override
    public String startProcess(String processName, Id attachedObjectId,
                               List<ProcessVariable> variables) {
        try {
            String idProcess = null;
            HashMap<String, Object> variablesHM = createProcessVariables(variables);

            if (attachedObjectId != null) {
                variablesHM.put(ProcessService.MAIN_ATTACHMENT_ID,
                        attachedObjectId.toStringRepresentation());
                variablesHM.put(ProcessService.CTX_ID,
                        attachedObjectId.toStringRepresentation());
                variablesHM.put(ProcessService.MAIN_ATTACHMENT,
                        new DomainObjectAccessor(attachedObjectId));
                variablesHM.put(ProcessService.CTX,
                        new DomainObjectAccessor(attachedObjectId));
            }

            variablesHM.put(ProcessService.SESSION, new Session());

            idProcess = runtimeService.startProcessInstanceByKey(processName,
                    variablesHM).getId();
            return idProcess;
        } catch (Exception ex) {
            throw new ProcessException("Error start process", ex);
        }
    }

    /**
     * Формирование Map для передачи его процессу
     *
     * @param variables
     * @return
     */
    private HashMap<String, Object> createProcessVariables(
            List<ProcessVariable> variables) {

        HashMap<String, Object> newHashMap = new HashMap<String, Object>();
        if (variables != null) {
            for (ProcessVariable parameter : variables) {
                newHashMap.put(parameter.getName(), parameter.getValue());
            }
        }

        return newHashMap;
    }

    @Override
    public void terminateProcess(String processId) {
        try {
            runtimeService.deleteProcessInstance(processId, null);
        } catch (Exception ex) {
            throw new ProcessException("Error terminate process", ex);
        }
    }

    @Override
    public String deployProcess(byte[] processDefinition, String processName) {
        try {
            ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
            RepositoryService repositoryService = processEngine.getRepositoryService();
            DeploymentBuilder db = repositoryService.createDeployment();
            db.enableDuplicateFiltering();
            final String text = new String(processDefinition, Charset.forName("UTF-8"));
            db.addString(processName, text);
            db.name(processName);
            Deployment depl = db.deploy();
            return depl.getId();
        } catch (Exception ex) {
            throw new ProcessException("Error deploy process", ex);
        }
    }

    @Override
    public void undeployProcess(String processDefinitionId, boolean cascade) {
        try {
            repositoryService.deleteDeployment(processDefinitionId, cascade);
        } catch (Exception ex) {
            throw new ProcessException("Error undeploy process", ex);
        }
    }

    @Override
    protected void onCompleteTask(DomainObject taskDomainObject, Map<String, String> params) {
        String taskId = taskDomainObject.getString("TaskId");
        formService.submitTaskFormData(taskId, params);
    }

    @Override
    public List<DeployedProcess> getDeployedProcesses() {
        try {
            List<Deployment> deployList = repositoryService.createDeploymentQuery()
                    .list();
            List<DeployedProcess> result = new ArrayList<DeployedProcess>();
            for (Deployment deployment : deployList) {
                DeployedProcess resItem = new DeployedProcess();
                resItem.setCategory(deployment.getCategory());
                resItem.setDeployedTime(deployment.getDeploymentTime());
                resItem.setId(deployment.getId());
                resItem.setName(deployment.getName());
                result.add(resItem);
            }
            return result;
        } catch (Exception ex) {
            throw new ProcessException("Error get deployed processes", ex);
        }
    }

    @Override
    public void sendProcessMessage(String processName, Id contextId, String message, List<ProcessVariable> variables) {
        //Находим нужный нам процесс
        List<Execution> executions =
                runtimeService.createExecutionQuery().
                        processDefinitionKey(processName).
                        processVariableValueEquals(ProcessService.CTX_ID, contextId.toStringRepresentation()).
                        messageEventSubscriptionName(message).
                        list();

        HashMap<String, Object> variablesHM = createProcessVariables(variables);

        //По идее должен быть только один процесс, но на всякий случай проходим в цикле
        for (Execution execution : executions) {
            runtimeService.messageEventReceived(message, execution.getId(), variablesHM);
        }
    }

    @Override
    public void sendProcessSignal(String signal) {
        runtimeService.signalEventReceived(signal);
    }

    @Override
    public List<WorkflowTaskData> getEngeneTasks() {
        // Этат метод не нужен в activiti. так как задачи получаем при их создание в движке классом GlobalCreateTaskListener
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean createGroup(String name) {
        // Этат метод не нужен в activiti, так в качестве адресата может быть произвольная строка
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean createOrUpdateUser(String login, boolean active) {
        // Этат метод не нужен в activiti, так в качестве адресата может быть произвольная строка
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSupportTemplate(byte[] processDefinition, String processName) {
        return processName.toLowerCase().endsWith("bpmn");
    }

    @Override
    public String getEngeneName() {
        return ENGENE_NAME;
    }
}
