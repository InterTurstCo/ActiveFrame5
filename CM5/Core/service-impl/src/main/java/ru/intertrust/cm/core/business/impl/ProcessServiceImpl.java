package ru.intertrust.cm.core.business.impl;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.IdService;
import ru.intertrust.cm.core.business.api.PersonManagementService;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.DeployedProcess;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.ProcessVariable;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.workflow.ProcessInstanceInfo;
import ru.intertrust.cm.core.business.api.workflow.ProcessTemplateInfo;
import ru.intertrust.cm.core.business.api.workflow.WorkflowEngine;
import ru.intertrust.cm.core.business.api.workflow.WorkflowTaskAddressee;
import ru.intertrust.cm.core.business.api.workflow.WorkflowTaskData;
import ru.intertrust.cm.core.dao.api.MD5Service;
import ru.intertrust.cm.core.model.ProcessException;
import ru.intertrust.cm.core.model.RemoteSuitableException;
import ru.intertrust.cm.core.model.SystemException;
import ru.intertrust.cm.core.util.SpringBeanAutowiringInterceptor;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import java.util.Collections;
import java.util.List;

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
    private IdService idService;

    @Autowired
    private PersonManagementService personManagementService;

    @Autowired
    private MD5Service md5Service;

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
    public String deployProcess(byte[] processDefinition, String processName) {
        try {
            String deploymentId = workflowEngine.deployProcess(processDefinition, processName);
            return deploymentId;
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
    public ProcessTemplateInfo getProcessTemplateInfo(byte[] template) {
        return workflowEngine.getProcessTemplateInfo(template);
    }

    @Override
    public ProcessInstanceInfo getProcessInstanceInfo(String processInstanceId) {
        return workflowEngine.getProcessInstanceInfo(processInstanceId);
    }

    @Override
    public List<ProcessInstanceInfo> getProcessInstanceInfos(int offset, int limit) {
        return workflowEngine.getProcessInstanceInfos(offset, limit);
    }
}

