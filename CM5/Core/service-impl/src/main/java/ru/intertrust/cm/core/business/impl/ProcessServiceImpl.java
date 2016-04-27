package ru.intertrust.cm.core.business.impl;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.activiti.engine.FormService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.runtime.Execution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.intertrust.cm.core.business.api.IdService;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.DeployedProcess;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.ProcessVariable;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.PersonServiceDao;
import ru.intertrust.cm.core.dao.api.StatusDao;
import ru.intertrust.cm.core.model.AccessException;
import ru.intertrust.cm.core.model.ProcessException;
import ru.intertrust.cm.core.model.SystemException;
import ru.intertrust.cm.core.model.UnexpectedException;
import ru.intertrust.cm.core.tools.DomainObjectAccessor;
import ru.intertrust.cm.core.tools.Session;

@Stateless(name = "ProcessService")
@Local(ProcessService.class)
@Remote(ProcessService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class ProcessServiceImpl implements ProcessService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessServiceImpl.class);

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Resource
    private SessionContext context;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private PersonServiceDao personService;

    @Autowired
    private CollectionsDao collectionsDao;

    @Autowired
    private IdService idService;

    @Autowired
    FormService formService;

    @Autowired
    private StatusDao statusDao;

    /*
     * @PostConstruct public void init() throws JAXBException, SAXException,
     * IOException { // TODO Эти строчки можно будет удалить после того, как
     * будет реализован // autowire для удаленных ejb.
     * AutowiredAnnotationBeanPostProcessor bpp = new
     * AutowiredAnnotationBeanPostProcessor();
     * bpp.setBeanFactory(SpringApplicationContext.getContext()
     * .getAutowireCapableBeanFactory()); bpp.processInjection(this); }
     */

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
        } catch (SystemException ex) {
            throw ex;            
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in startProcess", ex);
            throw new UnexpectedException("ProcessService", "startProcess",
                    "processName:" + processName + " attachedObjectId:" + attachedObjectId
                            + " variables: " + (variables == null ? "null" : Arrays.toString(variables.toArray())), ex);
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
        } catch (SystemException ex) {
            throw ex;            
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in terminateProcess", ex);
            throw new UnexpectedException("ProcessService", "terminateProcess",
                    "processId:" + processId, ex);
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
        } catch (SystemException ex) {
            throw ex;            
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in deployProcess", ex);
            throw new ProcessException("Error on deploy process", ex);
        }
    }

    @Override
    public void undeployProcess(String processDefinitionId, boolean cascade) {
        try {
            repositoryService.deleteDeployment(processDefinitionId, cascade);
        } catch (SystemException ex) {
            throw ex;            
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in undeployProcess", ex);
            throw new UnexpectedException("ProcessService", "undeployProcess",
                    "processDefinitionId:" + processDefinitionId + " cascade:" + cascade, ex);
        }
    }

    @Override
    public List<DomainObject> getUserTasks() {
        try {
            List<DomainObject> result = new ArrayList<DomainObject>();
            String personLogin = context.getCallerPrincipal().getName();
            Id personId = personService.findPersonByLogin(personLogin).getId();
            result = getUserTasks(personId);
            return result;
        } catch (SystemException ex) {
            throw ex;            
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in getUserTasks", ex);
            throw new UnexpectedException("ProcessService", "getUserTasks", "", ex);
        }

    }

    @Override
    public List<DomainObject> getUserDomainObjectTasks(Id attachedObjectId) {
        try {
            List<DomainObject> result = new ArrayList<DomainObject>();
            String personLogin = context.getCallerPrincipal().getName();
            DomainObject personByLogin = personService.findPersonByLogin(personLogin);
            Id personId = personByLogin.getId();
            if (personId != null) {
                result = getUserDomainObjectTasks(attachedObjectId, personId);
            }
            return result;
        } catch (SystemException ex) {
            throw ex;            
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in getUserDomainObjectTasks", ex);
            throw new UnexpectedException("ProcessService", "getUserDomainObjectTasks",
                    "attachedObjectId:" + attachedObjectId, ex);
        }
    }

    @Override
    public void completeTask(Id taskDomainObjectId,
            List<ProcessVariable> variables, String action) {

        try {
            String personLogin = context.getCallerPrincipal().getName();
            Id personId = personService.findPersonByLogin(personLogin).getId();

            //Проверка на то что задача дейцствительно есть у текущего пользователя
            if (!hasUserTask(personId, taskDomainObjectId)) {
                throw new ProcessException("Person " + personLogin + " does not have task with id=" + taskDomainObjectId.toStringRepresentation());
            }

            /**
             * AccessToken accessToken = accessControlService.createAccessToken(
             * personIdAsInt, taskDomainObjectId, DomainObjectAccessType.READ);
             */
            // TODO Переделать правила доступа после реализации прав
            AccessToken accessToken = accessControlService
                    .createSystemAccessToken("ProcessService");

            DomainObject taskDomainObject = domainObjectDao.find(
                    taskDomainObjectId, accessToken);
            //taskDomainObject.setLong("State", ProcessService.TASK_STATE_COMPLETE);
            //domainObjectDao.save(taskDomainObject, accessToken);
            taskDomainObject = domainObjectDao.setStatus(taskDomainObject.getId(),
                    statusDao.getStatusIdByName(ProcessService.TASK_STATE_COMPLETE), accessToken);

            String taskId = taskDomainObject.getString("TaskId");

            Map<String, String> params = new Hashtable<String, String>();
            if (variables != null) {
                for (ProcessVariable processVariable : variables) {
                    params.put(processVariable.getName(), processVariable
                            .getValue().toString());
                }
            }
            if (action != null) {
                params.put("ACTIONS", action);
            }

            formService.submitTaskFormData(taskId, params);

            // taskService.complete(taskId, createHashMap(variables));
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in completeTask", ex);
            throw new UnexpectedException("ProcessService", "completeTask",
                    "taskDomainObjectId:" + taskDomainObjectId
                            + " variables: " + (variables == null ? "null" : Arrays.toString(variables.toArray()))
                            + " action:" + action, ex);
        }
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
        } catch (SystemException ex) {
            throw ex;            
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in getDeployedProcesses", ex);
            throw new UnexpectedException("ProcessService", "getDeployedProcesses", "", ex);
        }
    }

    @Override
    public List<DomainObject> getUserTasks(Id personId) {
        // поиск задач отправленных пользователю, или любой группе в которую
        // входит пользователь
        try {
            Filter filter = new Filter();
            filter.setFilter("byPerson");
            ReferenceValue rv = new ReferenceValue(personId);
            filter.addCriterion(0, rv);
            List<Filter> filters = new ArrayList<>();
            filters.add(filter);

            /*
             * AccessToken accessToken = accessControlService
             * .createCollectionAccessToken(personIdAsint);
             */
            // TODO пока права не работают работаю от имени админа
            AccessToken accessToken = accessControlService
                    .createSystemAccessToken("ProcessService");

            IdentifiableObjectCollection collection1 = collectionsDao
                    .findCollection("PersonTask", filters, null, 0, 0, accessToken);
            IdentifiableObjectCollection collection2 = collectionsDao
                    .findCollection("PersonGroupTask", filters, null, 0, 0,
                            accessToken);

            List<DomainObject> result = new ArrayList<DomainObject>();
            for (IdentifiableObject item : collection1) {
                DomainObject task = domainObjectDao
                        .find(item.getId(), accessToken);
                result.add(task);
            }
            for (IdentifiableObject item : collection2) {
                DomainObject task = domainObjectDao
                        .find(item.getId(), accessToken);
                result.add(task);
            }
            return result;
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in getUserTasks", ex);
            throw new UnexpectedException("ProcessService", "getUserTasks", "personId: " + personId, ex);
        }
    }

    @Override
    public List<DomainObject> getUserDomainObjectTasks(Id attachedObjectId,
            Id personId) {
        // поиск задач отправленных пользователю, или любой группе в которую
        // входит пользователь

        try {
            Filter filter = new Filter();
            filter.setFilter("byPerson");
            Value rv = new ReferenceValue(personId);
            filter.addCriterion(0, rv);
            List<Filter> filters = new ArrayList<>();
            filters.add(filter);
            filter = new Filter();
            filter.setFilter("byAttachment");
            rv = new ReferenceValue(attachedObjectId);
            filter.addCriterion(0, rv);
            filters.add(filter);

            /*
             * AccessToken accessToken = accessControlService
             * .createCollectionAccessToken(personIdAsint);
             */
            // TODO пока права не работают работаю от имени процесса
            AccessToken accessToken = accessControlService
                    .createSystemAccessToken("ProcessService");

            IdentifiableObjectCollection collection1 = collectionsDao
                    .findCollection("PersonTask", filters, null, 0, 0, accessToken);
            IdentifiableObjectCollection collection2 = collectionsDao
                    .findCollection("PersonGroupTask", filters, null, 0, 0,
                            accessToken);

            List<DomainObject> result = new ArrayList<DomainObject>();
            List<Id> taskIds = new ArrayList<Id>();
            for (IdentifiableObject item : collection1) {
                //Добавляем только уникальные записи
                if (!taskIds.contains(item.getId())){
                    DomainObject task = domainObjectDao
                            .find(item.getId(), accessToken);
                    result.add(task);
                    taskIds.add(item.getId());
                }
            }
            for (IdentifiableObject item : collection2) {
                //Добавляем только уникальные записи
                if (!taskIds.contains(item.getId())){
                    DomainObject task = domainObjectDao
                            .find(item.getId(), accessToken);
                    result.add(task);
                    taskIds.add(item.getId());
                }
            }
            return result;
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in getUserDomainObjectTasks", ex);
            throw new UnexpectedException("ProcessService", "getUserDomainObjectTasks",
                    "attachedObjectId: " + attachedObjectId + " personId: " + personId, ex);
        }
    }

    private boolean hasUserTask(Id personId, Id taskId) {
        // поиск задач отправленных пользователю, или любой группе в которую
        // входит пользователь

        Filter filter = new Filter();
        filter.setFilter("byPerson");
        Value rv = new ReferenceValue(personId);
        filter.addCriterion(0, rv);
        List<Filter> filters = new ArrayList<>();
        filters.add(filter);
        filter = new Filter();
        filter.setFilter("byTask");
        rv = new ReferenceValue(taskId);
        filter.addCriterion(0, rv);
        filters.add(filter);

        /*
         * AccessToken accessToken = accessControlService
         * .createCollectionAccessToken(personIdAsint);
         */
        // TODO пока права не работают работаю от имени процесса
        AccessToken accessToken = accessControlService
                .createSystemAccessToken("ProcessService");

        IdentifiableObjectCollection collection1 = collectionsDao
                .findCollection("PersonTask", filters, null, 0, 0, accessToken);
        IdentifiableObjectCollection collection2 = collectionsDao
                .findCollection("PersonGroupTask", filters, null, 0, 0,
                        accessToken);

        List<DomainObject> result = new ArrayList<DomainObject>();
        for (IdentifiableObject item : collection1) {
            DomainObject task = domainObjectDao
                    .find(item.getId(), accessToken);
            result.add(task);
        }
        for (IdentifiableObject item : collection2) {
            DomainObject task = domainObjectDao
                    .find(item.getId(), accessToken);
            result.add(task);
        }
        return result.size() > 0;
    }

    @Override
    public void sendProcessMessage(String processName, Id contextId, String message, List<ProcessVariable> variables) {
        //Находим нужный нам процесс
        List<Execution> executions =
                runtimeService.createExecutionQuery().
                        processDefinitionKey(processName).
                        processVariableValueEquals(CTX_ID, contextId.toStringRepresentation()).
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

}
