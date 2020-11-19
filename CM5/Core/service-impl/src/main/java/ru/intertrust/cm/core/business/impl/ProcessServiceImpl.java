package ru.intertrust.cm.core.business.impl;

import com.healthmarketscience.rmiio.DirectRemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StreamUtils;
import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.IdService;
import ru.intertrust.cm.core.business.api.PersonManagementService;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.DeployedProcess;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.ProcessVariable;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.workflow.ProcessInstanceInfo;
import ru.intertrust.cm.core.business.api.workflow.ProcessTemplateInfo;
import ru.intertrust.cm.core.business.api.workflow.TaskInfo;
import ru.intertrust.cm.core.business.api.workflow.WorkflowEngine;
import ru.intertrust.cm.core.business.api.workflow.WorkflowTaskAddressee;
import ru.intertrust.cm.core.business.api.workflow.WorkflowTaskData;
import ru.intertrust.cm.core.dao.api.MD5Service;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.model.ProcessException;
import ru.intertrust.cm.core.model.RemoteSuitableException;
import ru.intertrust.cm.core.model.SystemException;
import ru.intertrust.cm.core.util.SpringBeanAutowiringInterceptor;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Stateless(name = "ProcessService")
@Local(ProcessService.class)
@Remote(ProcessService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class ProcessServiceImpl implements ProcessService {
    private static final Logger logger = LoggerFactory.getLogger(ProcessServiceImpl.class);

    @Autowired
    private WorkflowEngine workflowEngine;

    @Autowired
    private CrudService crudService;

    @Autowired
    private CollectionsService collectionsService;

    @Autowired
    private IdService idService;

    @Autowired
    private PersonManagementService personManagementService;

    @Autowired
    private MD5Service md5Service;

    @Autowired
    private AttachmentService attachmentService;

    @Override
    public String startProcess(String processName, Id attachedObjectId,
            List<ProcessVariable> variables) {
        try {
            return workflowEngine.startProcess(processName, attachedObjectId, variables);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public void terminateProcess(String processId) {
        try {
            workflowEngine.terminateProcess(processId);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public Id saveProcess(byte[] processDefinition, String fileName, boolean deploy) {

        ProcessTemplateInfo info = workflowEngine.getProcessTemplateInfo(processDefinition);
        Id result = null;

        // Проверяем что версия не пустая
        if (info.getVersion() == null){
            throw new FatalException("Version is not defined in process definition " + fileName);
        }

        if (!info.getVersion().matches("\\d+\\.\\d+.\\d+.\\d+")){
            throw new FatalException("Version has incorrect format in process definition " + fileName + ". Need #.#.#.# format.");
        }

        // Вычисляем хэш сохраняемого шаблона
        String newHash = md5Service.getMD5AsHex(processDefinition);

        // Поиск процесса по имени и версии
        Map<String, Value> key = new HashMap<>();
        key.put("process_id", new StringValue(info.getId()));
        key.put("version", new StringValue(info.getVersion()));
        DomainObject processDefinitionObject = crudService.findByUniqueKey("process_definition", key);

        // Если найдено проверяем соответствие хэша шаблона
        if (processDefinitionObject != null){
            // Проверяем соответствие хэшей
            if (!newHash.equals(processDefinitionObject.getString("hash"))){
                // Попытка сохранить другой шаблон с повторяющимися именем и версией
                throw new FatalException("Process definition with name " + fileName +
                        " and version " + info.getVersion() + " alredy exists.");
            }
        }else {
            // Ничего не найдено, создаем
            processDefinitionObject = crudService.createDomainObject("process_definition");

            processDefinitionObject.setString("file_name", fileName);
            processDefinitionObject.setString("process_id", info.getId());
            processDefinitionObject.setString("process_name", info.getName());
            processDefinitionObject.setString("version", info.getVersion());
            processDefinitionObject.setString("category", info.getCategory());
            processDefinitionObject.setString("description", info.getDescription());
            processDefinitionObject.setString("hash", newHash);

            processDefinitionObject = crudService.save(processDefinitionObject);

            DomainObject attachment = attachmentService.createAttachmentDomainObjectFor(processDefinitionObject.getId(), "process_definition_model");
            attachment.setString("Name", fileName);

            DirectRemoteInputStream directRemoteInputStream =
                    new DirectRemoteInputStream(new ByteArrayInputStream(processDefinition), false);
            attachmentService.saveAttachment(directRemoteInputStream, attachment);

            if (deploy) {
                deployProcess(processDefinitionObject.getId());
            }
        }
        result = processDefinitionObject.getId();

        return result;
    }

    @Override
    public String deployProcess(Id processDefinitionId) {
        try {

            DomainObject processDefinition = crudService.find(processDefinitionId);
            List<DomainObject> attachments = attachmentService.findAttachmentDomainObjectsFor(processDefinitionId);

            // Ожидаем что только одно вложение привязано к доменному объекту
            if (attachments.size() != 1){
                throw new FatalException("Only one attachment need in process_definition objects");
            }

            try (final InputStream stream = RemoteInputStreamClient.wrap(attachmentService.loadAttachment(attachments.get(0).getId()))) {
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                StreamUtils.copy(stream, outStream);

                String deployId = workflowEngine.deployProcess(outStream.toByteArray(), processDefinition.getString("file_name"));

                processDefinition.setString("definition_id", deployId);
                crudService.save(processDefinition);

                crudService.setStatus(processDefinitionId, "Active");
                return deployId;
            }
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public void undeployProcess(String processDefinitionId, boolean cascade) {
        try {
            workflowEngine.undeployProcess(processDefinitionId, cascade);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public List<DomainObject> getUserTasks() {
        try {
            return workflowEngine.getUserTasks();
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public List<DomainObject> getUserDomainObjectTasks(Id attachedObjectId) {
        try {
            return workflowEngine.getUserDomainObjectTasks(attachedObjectId);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public void completeTask(Id taskDomainObjectId,
            List<ProcessVariable> variables, String action) {
        try {
            workflowEngine.completeTask(taskDomainObjectId, variables, action);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public List<DeployedProcess> getDeployedProcesses() {
        try {
            return workflowEngine.getDeployedProcesses();
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public List<DomainObject> getUserTasks(Id personId) {
        try {
            return workflowEngine.getUserTasks(personId);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public List<DomainObject> getUserDomainObjectTasks(Id attachedObjectId,
            Id personId) {
        try {
            return workflowEngine.getUserDomainObjectTasks(attachedObjectId, personId);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public void sendProcessMessage(String processName, Id contextId, String message, List<ProcessVariable> variables) {
        try {
            workflowEngine.sendProcessMessage(processName, contextId, message, variables);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public void sendProcessSignal(String signal) {
        try {
            workflowEngine.sendProcessSignal(signal);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public Id assignTask(WorkflowTaskData task) {
        synchronized (ProcessServiceImpl.class) {
            DomainObject taskDomainObject = crudService.findByUniqueKey(
                    "Person_Task", Collections.singletonMap("TaskId", new StringValue(task.getTaskId())));
            if (taskDomainObject == null) {
                taskDomainObject = crudService.createDomainObject("Person_Task");
                taskDomainObject.setString("TaskId", task.getTaskId());
                taskDomainObject.setString("ActivityId", task.getActivityId());
                taskDomainObject.setString("ProcessId", task.getProcessId());
                taskDomainObject.setString("Name", task.getName());
                taskDomainObject.setString("Description", task.getDescription());
                taskDomainObject.setLong("Priority", 0L);
                taskDomainObject.setString("ExecutionId", task.getExecutionId());
                if (task.getContext() != null) {
                    taskDomainObject.setReference("MainAttachment", idService.createId(task.getContext()));
                } else {
                    throw new ProcessException("Task with id " + task.getTaskId() + " is incorrect. Context ID is null.");
                }
                taskDomainObject.setString("Actions", task.getActions());
                taskDomainObject = crudService.save(taskDomainObject);

                for (WorkflowTaskAddressee wfTaskAddressee : task.getAddressee()) {
                    DomainObject assignee = null;
                    if (wfTaskAddressee.isGroup()) {
                        assignee = crudService.createDomainObject("Assignee_Group");
                        assignee.setReference("UserGroup", personManagementService.getGroupId(wfTaskAddressee.getName()));
                    } else {
                        assignee = crudService.createDomainObject("Assignee_Person");
                        assignee.setReference("Person", personManagementService.getPersonId(wfTaskAddressee.getName()));
                    }
                    assignee.setReference("PersonTask", taskDomainObject.getId());
                    crudService.save(assignee);
                }
            }
            return taskDomainObject.getId();
        }
    }

    @Override
    public boolean isSupportTemplate(byte[] processDefinition, String processName) {
        return workflowEngine.isSupportTemplate(processDefinition, processName);
    }

    @Override
    public String getEngeneName() {
        return workflowEngine.getEngeneName();
    }

    @Override
    public ProcessInstanceInfo getProcessInstanceInfo(String processInstanceId) {
        return workflowEngine.getProcessInstanceInfo(processInstanceId);
    }

    @Override
    public List<ProcessInstanceInfo> getProcessInstanceInfos(int offset, int limit) {
        return workflowEngine.getProcessInstanceInfos(offset, limit);
    }

    @Override
    public List<TaskInfo> getProcessInstanceTasks(String processInstanceId, int offset, int limit) {
        return workflowEngine.getProcessInstanceTasks(processInstanceId, offset, limit);
    }

    @Override
    public Map<String, Object> getProcessInstanceVariables(String processInstanceId, int offset, int limit) {
        return workflowEngine.getProcessInstanceVariables(processInstanceId, offset, limit);
    }

    @Override
    public Id getLastProcessDefinitionId(String processDefinitionKey) {
        Id result = null;

        String processDefinitionId = workflowEngine.getLastProcessDefinitionId(processDefinitionKey);

        if (processDefinitionId != null) {
            IdentifiableObjectCollection collection = collectionsService.findCollectionByQuery(
                    "select id from process_definition where definition_id = {0}",
                    Collections.singletonList(new StringValue(processDefinitionId)));

            if (collection.size() > 0){
                result = collection.get(0).getId();
            }
        }

        return result;
    }

    @Override
    public void suspendProcessInstance(String processInstanceId) {
        workflowEngine.suspendProcessInstance(processInstanceId);
    }

    @Override
    public void activateProcessInstance(String processInstanceId) {
        workflowEngine.activateProcessInstance(processInstanceId);
    }

    @Override
    public void deleteProcessInstance(String processInstanceId) {
        workflowEngine.deleteProcessInstance(processInstanceId);
    }

    @Override
    public byte[] getProcessTemplateModel(Id processDefinitionId) {
        try {

            DomainObject processDefinition = crudService.find(processDefinitionId);
            List<DomainObject> attachments = attachmentService.findAttachmentDomainObjectsFor(processDefinitionId);

            // Ожидаем что только одно вложение привязано к доменному объекту
            if (attachments.size() != 1){
                throw new FatalException("Only one attachment need in process_definition objects");
            }

            try (final InputStream stream = RemoteInputStreamClient.wrap(attachmentService.loadAttachment(attachments.get(0).getId()))) {
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                StreamUtils.copy(stream, outStream);

                return workflowEngine.getProcessTemplateModel(outStream.toByteArray());
            }
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public byte[] getProcessInstanceModel(String processInstanceId) {
        return workflowEngine.getProcessInstanceModel(processInstanceId);
    }
}

