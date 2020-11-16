package ru.intertrust.cm.core.business.impl.workflow;

import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.ValuedDataObject;
import org.flowable.common.engine.impl.util.io.BytesStreamSource;
import org.flowable.engine.FormService;
import org.flowable.engine.HistoryService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngines;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.DeploymentBuilder;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.DeployedProcess;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.ProcessVariable;
import ru.intertrust.cm.core.business.api.workflow.ProcessInstanceInfo;
import ru.intertrust.cm.core.business.api.workflow.ProcessTemplateInfo;
import ru.intertrust.cm.core.business.api.workflow.TaskInfo;
import ru.intertrust.cm.core.business.api.workflow.WorkflowTaskData;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.model.ProcessException;
import ru.intertrust.cm.core.tools.DomainObjectAccessor;
import ru.intertrust.cm.core.tools.Session;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FlowableWorkflowEngineImpl extends AbstactWorkflowEngine {
    private static final Logger logger = LoggerFactory.getLogger(FlowableWorkflowEngineImpl.class);
    public static final String ENGENE_NAME = "flowable";

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

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

            ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery().deploymentId(depl.getId());
            List<ProcessDefinition> processDefinitions =  processDefinitionQuery.list();

            if (processDefinitions.size() == 0){
                throw new FatalException("Process definition not created. Check model and process name. Name need bpmn extensions");
            }

            return processDefinitions.get(0).getId();
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
    public boolean createOrUpdateGroup(String name, Set<String> persons) {
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

    @Override
    public ProcessTemplateInfo getProcessTemplateInfo(byte[] tempale) {
        try {
            BpmnXMLConverter converter = new BpmnXMLConverter();
            BpmnModel model = converter.convertToBpmnModel(new BytesStreamSource(tempale), true, false);

            ProcessTemplateInfo result = new ProcessTemplateInfo();
            result.setName(model.getMainProcess().getName());
            result.setDescription(model.getMainProcess().getDocumentation());
            result.setCategory(model.getTargetNamespace());

            List<ValuedDataObject> dataObjects = model.getMainProcess().getDataObjects();
            for (ValuedDataObject dataObject : dataObjects) {
                if (dataObject.getName().equalsIgnoreCase("version")
                        || dataObject.getId().equalsIgnoreCase("version")){
                    result.setVersion((String)dataObject.getValue());
                }
            }


            return result;
        } catch (Exception ex) {
            throw new ProcessException("Error get process template info", ex);
        }
    }

    @Override
    public ProcessInstanceInfo getProcessInstanceInfo(String processInstanceId) {
        List<HistoricProcessInstance> processInstances = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId).list();
        ProcessInstanceInfo result = null;
        if (processInstances.size() > 0){
            HistoricProcessInstance processInstance = processInstances.get(0);
            result = new ProcessInstanceInfo();
            result.setId(processInstance.getId());
            result.setName(processInstance.getProcessDefinitionKey());
            result.setStart(processInstance.getStartTime());
            result.setFinish(processInstance.getEndTime());
            //result.setTasks(getProcessInstanceTasks(processInstanceId));
            //result.setVariables(getProcessInstanceVariables(processInstanceId));
        }
        return result;
    }

    @Override
    public List<ProcessInstanceInfo> getProcessInstanceInfos(int offset, int limit) {
        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();
        List<HistoricProcessInstance> processInstances = query.listPage(offset, limit);

        List<ProcessInstanceInfo> result = new ArrayList<>();

        for (HistoricProcessInstance processInstance : processInstances) {
            ProcessInstanceInfo info = new ProcessInstanceInfo();
            info.setId(processInstance.getId());
            info.setName(processInstance.getProcessDefinitionKey());
            info.setStart(processInstance.getStartTime());
            info.setFinish(processInstance.getEndTime());
            result.add(info);
        }

        return result;
    }

    @Override
    public List<TaskInfo> getProcessInstanceTasks(String processInstanceId, int offset, int limit) {
        List<TaskInfo> result = new ArrayList<>();
        try{
            List<HistoricTaskInstance> tasks = null;
            if (limit > 0) {
                tasks = historyService.createHistoricTaskInstanceQuery().
                        processInstanceId(processInstanceId).listPage(offset, limit);
            }else{
                tasks = historyService.createHistoricTaskInstanceQuery().
                        processInstanceId(processInstanceId).list();

            }
            for (HistoricTaskInstance task :tasks) {
                TaskInfo taskInfo = new TaskInfo();
                taskInfo.setStartDate(task.getCreateTime());
                taskInfo.setFinishDate(task.getEndTime());
                taskInfo.setName(task.getName());
                taskInfo.setAssignee(task.getAssignee());
                taskInfo.setId(task.getId());
                result.add(taskInfo);
            }

        }catch(Exception ignoreEx){
            // При обновлении версии могут возникнуть пролемы десериализации на старых экземплярах процессов, игнорируем такие ошибки
            logger.warn("Error get variables", ignoreEx);
        }
        return result;
    }

    @Override
    public Map<String, Object> getProcessInstanceVariables(String processInstanceId, int offset, int limit) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<HistoricVariableInstance> variables = null;
            if (limit > 0) {
                variables = historyService.createHistoricVariableInstanceQuery().
                        processInstanceId(processInstanceId).listPage(offset, limit);
            }else{
                variables = historyService.createHistoricVariableInstanceQuery().
                        processInstanceId(processInstanceId).list();
            }
            for (HistoricVariableInstance variable : variables) {
                result.put(variable.getVariableName(), variable.getValue());
            }
        }catch(Exception ignoreEx){
            // При обновлении версии могут возникнуть пролемы десериализации на старых экземплярах процессов, игнорируем такие ошибки
            logger.warn("Error get variables", ignoreEx);
        }
        return result;
    }
}
