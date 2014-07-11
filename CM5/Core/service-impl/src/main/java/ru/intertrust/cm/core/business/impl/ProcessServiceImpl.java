package ru.intertrust.cm.core.business.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.activiti.engine.FormService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
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
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.*;
import ru.intertrust.cm.core.model.AccessException;
import ru.intertrust.cm.core.model.ProcessException;
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
            HashMap<String, Object> variablesHM = createHashMap(variables);

            if (attachedObjectId != null) {
                variablesHM.put(ProcessService.MAIN_ATTACHMENT_ID,
                        attachedObjectId);
                variablesHM.put(ProcessService.MAIN_ATTACHMENT,
                        new DomainObjectAccessor(attachedObjectId));
            }

            variablesHM.put(ProcessService.SESSION, new Session());

            idProcess = runtimeService.startProcessInstanceByKey(processName,
                    variablesHM).getId();
            return idProcess;
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
    private HashMap<String, Object> createHashMap(
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
            logger.error("Unexpected exception caught in terminateProcess", ex);
            throw new UnexpectedException("ProcessService", "terminateProcess",
                    "processId:" + processId, ex);
        }
    }

    @Override
    public String deployProcess(byte[] processDefinition, String processName) {
        InputStream inputStream = null;
        try {
            inputStream = new ByteArrayInputStream(processDefinition);

            DeploymentBuilder db = repositoryService.createDeployment();
            db.enableDuplicateFiltering();
            db.addInputStream(processName, inputStream);
            Deployment depl = db.deploy();
            return depl.getId();
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in deployProcess", ex);
            throw new ProcessException("Error on deploy process", ex);
        } finally {
            try {
                inputStream.close();
            } catch (IOException ignoreEx) {
            }
        }
    }

    @Override
    public void undeployProcess(String processDefinitionId, boolean cascade) {
        try {
            repositoryService.deleteDeployment(processDefinitionId, cascade);
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
            if (personId != null){
                result = getUserDomainObjectTasks(attachedObjectId, personId);
            }
            return result;
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
            // Костыль, нужно избавлятся от использования идентификатора как int или
            // long
        /*
         * int personIdAsInt =
         * Integer.parseInt(personId.toStringRepresentation()
         * .substring(personId.toStringRepresentation().indexOf('|') + 1));
         */

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
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in getDeployedProcesses", ex);
            throw new UnexpectedException("ProcessService", "getDeployedProcesses", "", ex);
        }
    }

	@Override
	public List<DomainObject> getUserTasks(Id personId) {
        // Костыль, нужно избавлятся от использования идентификатора как int или
        // long
        /*
         * int personIdAsint =
         * Integer.parseInt(personId.toStringRepresentation()
         * .substring(personId.toStringRepresentation().indexOf('|') + 1));
         */
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
                DomainObject group = domainObjectDao
                        .find(item.getId(), accessToken);
                result.add(group);
            }
            for (IdentifiableObject item : collection2) {
                DomainObject group = domainObjectDao
                        .find(item.getId(), accessToken);
                result.add(group);
            }
            return result;
        } catch (AccessException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in getUserTasks", ex);
            throw new UnexpectedException("ProcessService", "getUserTasks", "personId: " + personId, ex);
        }
    }

	@Override
	public List<DomainObject> getUserDomainObjectTasks(Id attachedObjectId,
			Id personId) {
		 // Костыль, нужно избавлятся от использования идентификатора как int или
        // long
        /*
         * int personIdAsint =
         * Integer.parseInt(personId.toStringRepresentation()
         * .substring(personId.toStringRepresentation().indexOf('|') + 1));
         */
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
            for (IdentifiableObject item : collection1) {
                DomainObject group = domainObjectDao
                        .find(item.getId(), accessToken);
                result.add(group);
            }
            for (IdentifiableObject item : collection2) {
                DomainObject group = domainObjectDao
                        .find(item.getId(), accessToken);
                result.add(group);
            }
            return result;
        } catch (AccessException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in getUserDomainObjectTasks", ex);
            throw new UnexpectedException("ProcessService", "getUserDomainObjectTasks",
                    "attachedObjectId: " + attachedObjectId + " personId: " + personId, ex);
        }
    }

}
