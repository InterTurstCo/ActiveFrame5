package ru.intertrust.cm.core.business.impl;

import com.healthmarketscience.rmiio.DirectRemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.IOHelpService;
import ru.intertrust.cm.core.business.api.IdService;
import ru.intertrust.cm.core.business.api.InputStreamProvider;
import ru.intertrust.cm.core.business.api.PersonManagementService;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.DeployedProcess;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.ProcessVariable;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.workflow.ProcessInstanceInfo;
import ru.intertrust.cm.core.business.api.workflow.ProcessTemplateInfo;
import ru.intertrust.cm.core.business.api.workflow.TaskInfo;
import ru.intertrust.cm.core.business.api.workflow.WorkflowEngine;
import ru.intertrust.cm.core.business.api.workflow.WorkflowTaskAddressee;
import ru.intertrust.cm.core.business.api.workflow.WorkflowTaskData;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.model.ProcessException;
import ru.intertrust.cm.core.model.RemoteSuitableException;
import ru.intertrust.cm.core.util.SpringBeanAutowiringInterceptor;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
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
    private AttachmentService attachmentService;

    @Autowired
    private IOHelpService ioHelpService;

    @org.springframework.beans.factory.annotation.Value("${workflow.use.check.sum.to.upload:true}")
    private boolean useCheckSum;

    public void setUseCheckSum(boolean useCheckSum) {
        this.useCheckSum = useCheckSum;
    }

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
    public Id saveProcess(InputStreamProvider processDefinitionProvider, String fileName, boolean deploy) {
        return this.saveProcess(processDefinitionProvider, fileName, deploy ? SaveType.ACTIVATE : SaveType.ONLY_SAVE);
    }

    @Override
    public Id saveProcess(InputStreamProvider processDefinitionProvider, String fileName, SaveType type) {
        ProcessDefinitionData processDefinitionData = getProcessDefinitionData(processDefinitionProvider);
        return saveProcessInner(processDefinitionData.getProcessDefinition(), processDefinitionData.getMd5sum(), fileName, type);
    }

    @Override
    public Id saveProcess(byte[] processDefinition, String fileName, boolean deploy) {
       return this.saveProcess(processDefinition, fileName, deploy ? SaveType.ACTIVATE : SaveType.ONLY_SAVE);
    }

    @Override
    public Id saveProcess(byte[] processDefinition, String fileName, SaveType type) {
        ProcessDefinitionData processDefinitionData = getProcessDefinitionData(() -> new ByteArrayInputStream(processDefinition));
        return saveProcessInner(processDefinitionData.getProcessDefinition(), processDefinitionData.getMd5sum(), fileName, type);
    }

    private Id saveProcessInner(byte[] processDefinition, String hash, String fileName, SaveType type) {

        ProcessTemplateInfo info = workflowEngine.getProcessTemplateInfo(processDefinition);

        // Проверяем что версия не пустая
        if (info.getVersion() == null) {
            throw new FatalException("Version is not defined in process definition " + fileName);
        }

        if (!info.getVersion().matches("\\d+\\.\\d+.\\d+.\\d+")) {
            throw new FatalException("Version has incorrect format in process definition " + fileName + ". Need #.#.#.# format.");
        }

        // Поиск процесса по имени и версии
        Map<String, Value> key = new HashMap<>();
        key.put("process_id", new StringValue(info.getId()));
        key.put("version", new StringValue(info.getVersion()));
        DomainObject processDefinitionObject = crudService.findByUniqueKey("process_definition", key);

        // Если найдено проверяем соответствие хэша шаблона
        if (processDefinitionObject != null) {
            // Проверяем соответствие хэшей, при необходимости
            if (useCheckSum) {
                if (hash == null) {
                    throw new FatalException("MD5 sum must not be null when workflow.use.check.sum.to.upload enabled!" +
                            " ProcessDefinition name is " + fileName);
                }
                final String storedHash = processDefinitionObject.getString("hash");
                // Если изначально использование проверки hash было выключено, а потом включили, то ошибки не будет
                if (!StringUtils.isEmpty(storedHash) && !hash.equals(storedHash)){
                    // Попытка сохранить другой шаблон с повторяющимися именем и версией
                    throw new FatalException("Process definition with name " + fileName +
                            " and version " + info.getVersion() + " already exists.");
                }
            }

        } else {
            final Id lastId = type == SaveType.DEPLOY ? this.getLastProcessDefinitionId(info.getId()) : null;
            String lastUploadVersion = getLastUploadVersion(info.getId(), info.getVersion());
            if(lastUploadVersion != null) {
                String newVersion = info.getVersion();
                if (getPatchFromVersion(lastUploadVersion) >= getPatchFromVersion(newVersion)) {
                    throw new FatalException("Already upload version = " + lastUploadVersion);
                }
            }
            processDefinitionObject = crudService.createDomainObject("process_definition");
            processDefinitionObject.setString("file_name", fileName);
            processDefinitionObject.setString("process_id", info.getId());
            processDefinitionObject.setString("process_name", info.getName());
            processDefinitionObject.setString("version", info.getVersion());
            processDefinitionObject.setString("category", info.getCategory());
            processDefinitionObject.setString("description", info.getDescription());
            processDefinitionObject.setString("hash", hash);

            processDefinitionObject = crudService.save(processDefinitionObject);

            DomainObject attachment = attachmentService.createAttachmentDomainObjectFor(processDefinitionObject.getId(), "process_definition_model");
            attachment.setString("Name", fileName);

            DirectRemoteInputStream directRemoteInputStream =
                    new DirectRemoteInputStream(new ByteArrayInputStream(processDefinition), false);
            attachmentService.saveAttachment(directRemoteInputStream, attachment);

            if (type == SaveType.ACTIVATE || type == SaveType.DEPLOY) {
                deployProcess(processDefinitionObject.getId());
                if (lastId != null) {
                    deployProcess(lastId);
                }
            }
        }

        return processDefinitionObject.getId();
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
                    DomainObject assignee;
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
    public boolean isSupportTemplate(String processName) {
        return workflowEngine.isSupportTemplate(processName);
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
    public List<ProcessInstanceInfo> getProcessInstanceInfos(
            int offset, int limit, String name,
            Date startDateBegin, Date startDateEnd,
            Date finishDateBegin, Date finishDateEnd,
            SortOrder sortOrder){
        return workflowEngine.getProcessInstanceInfos(offset, limit, name, startDateBegin, startDateEnd,
                finishDateBegin, finishDateEnd, sortOrder);
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

    private int getPatchFromVersion(String version) {
        // предполагаем, что версия корректная, иначе нельзя загружать
        return Integer.parseInt(version.substring(version.lastIndexOf(".") + 1 ));
    }

    private String getMainVersion(String version) {
        return version.substring(0, version.lastIndexOf("."));
    }

    private String getLastUploadVersion(final String schemaName, final String version) {

        IdentifiableObjectCollection coll = collectionsService.
                findCollectionByQuery("select version from process_definition " +
                                "where process_id = {0} and version like {1} order by created_date desc",
                        Arrays.asList(new StringValue(schemaName),
                                new StringValue(getMainVersion(version) + "%")), 0, 1);
        return coll.size() == 0 ? null : coll.get(0).getString("version");
    }

    private ProcessDefinitionData getProcessDefinitionData(InputStreamProvider processDefinitionProvider) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String hash = null;
        if (useCheckSum) {
            hash = ioHelpService.copyWithEolControlAndMd5(processDefinitionProvider, os);
        } else {
            ioHelpService.copyWithEolControl(processDefinitionProvider, os);
        }
        return new ProcessDefinitionData(os.toByteArray(), hash);
    }

    private static class ProcessDefinitionData {
        private final byte[] processDefinition;
        private final String md5sum;

        private ProcessDefinitionData(byte[] processDefinition, String md5sum) {

            this.processDefinition = processDefinition;
            this.md5sum = md5sum;
        }

        private byte[] getProcessDefinition() {
            return processDefinition;
        }

        private String getMd5sum() {
            return md5sum;
        }
    }
}

