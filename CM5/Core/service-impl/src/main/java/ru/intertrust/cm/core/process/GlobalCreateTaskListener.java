package ru.intertrust.cm.core.process;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.flowable.engine.FormService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngines;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.flowable.task.service.delegate.TaskListener;
import org.flowable.engine.form.FormProperty;
import org.flowable.engine.form.TaskFormData;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.IdService;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.PermissionServiceDao;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.PersonManagementServiceDao;
import ru.intertrust.cm.core.dao.api.StatusDao;
import ru.intertrust.cm.core.model.ProcessException;
import ru.intertrust.cm.core.tools.SpringClient;

/**
 * Глобальный слушатель создания UserTask. Создает соответствующий доменный
 * объект и выполняет отсылку по электронной почте
 */
public class GlobalCreateTaskListener extends SpringClient implements TaskListener {
    public static final String PERSON_ASSIGNEE_PREFIX = "PERSON";
    public static final String GROUP_ASSIGNEE_PREFIX = "GROUP";
    public static final String DYNAMIC_GROUP_ASSIGNEE_PREFIX = "DYNAMIC_GROUP";
    public static final String CONTEXT_ROLE_ASSIGNEE_PREFIX = "CONTEXT_ROLE";

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private IdService idService;

    @Autowired
    FormService formService;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private CollectionsDao collectionsDao;

    @Autowired
    private StatusDao statusDao;

    @Autowired
    private PersonManagementServiceDao personManagementService;
    
    @Autowired
    private UserGroupGlobalCache userGroupGlobalCache;

    @Autowired
    private PermissionServiceDao permissionServiceDao;

    /**
     * Входная точка слушителя. вызывается при создание пользовательской задачи
     */
    @Override
    public void notify(DelegateTask delegateTask) {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();

        // Создание доменного обьекта Task
        DomainObject taskDomainObject = createDomainObject("Person_Task");
        taskDomainObject.setString("TaskId", delegateTask.getId());
        taskDomainObject.setString("ActivityId", delegateTask.getTaskDefinitionKey());
        taskDomainObject.setString("ProcessId", repositoryService.getProcessDefinition(delegateTask.getProcessDefinitionId()).getKey());
        taskDomainObject.setString("Name", delegateTask.getName());
        taskDomainObject
                .setString("Description", delegateTask.getDescription());
        taskDomainObject.setLong("Priority", (long) delegateTask.getPriority());
        taskDomainObject.setString("ExecutionId", delegateTask.getExecutionId());
        // У нового объекта автоматом установится статус Send

        // Получение полей формы задачи ACTIONS и сохранение его в обьект
        // задача
        TaskFormData taskData = formService.getTaskFormData(delegateTask.getId());
        List<FormProperty> formProperties = taskData.getFormProperties();
        String mainAttachmentId = null;
        for (FormProperty formProperty : formProperties) {
            if (formProperty.getId().equals("ACTIONS")) {
                Map<String, String> values = (Map<String, String>) formProperty.getType().getInformation("values");
                StringBuilder actions = new StringBuilder();
                boolean firstItem = true;
                for (String key : values.keySet()) {
                    if (firstItem) {
                        firstItem = false;
                    } else {
                        actions.append(";");
                    }
                    actions.append(key);
                    actions.append("=");
                    actions.append(values.get(key));
                }
                taskDomainObject.setString("Actions", actions.toString());
            }
            else if (formProperty.getId().equals(ProcessService.CTX_ID)) {
                mainAttachmentId = formProperty.getValue();
            }
            //Для совместимости
            else if (formProperty.getId().equals(ProcessService.MAIN_ATTACHMENT_ID)) {
                mainAttachmentId = formProperty.getValue();
            }
        }
        if (mainAttachmentId == null) {
            throw new ProcessException("CTX_ID is requred");
        }
        taskDomainObject.setReference("MainAttachment", idService.createId(mainAttachmentId));

        AccessToken accessToken = accessControlService
                .createSystemAccessToken("GlobalCreateTaskListener");
        // Сохранение доменного объекта
        taskDomainObject = domainObjectDao.save(taskDomainObject, accessToken);
        //Получение адресатов
        List<DomainObject> assigneeList = getAssigneeList(idService.createId(mainAttachmentId), delegateTask.getAssignee(), accessToken);
        for (DomainObject assignee : assigneeList) {
            // Создание связанного AssigneePerson или AssigneeGroup
            if (assignee.getTypeName().equals("User_Group")) {
                DomainObject assigneePersonDomainObject = createDomainObject("Assignee_Group");

                assigneePersonDomainObject.setReference("PersonTask",
                        taskDomainObject);
                assigneePersonDomainObject.setReference("UserGroup", assignee);

                domainObjectDao.save(assigneePersonDomainObject, accessToken);
            } else {
                DomainObject assigneePersonDomainObject = createDomainObject("Assignee_Person");

                assigneePersonDomainObject.setReference("PersonTask",
                        taskDomainObject);
                assigneePersonDomainObject.setReference("Person", assignee);

                domainObjectDao.save(assigneePersonDomainObject, accessToken);
            }
        }
        // Отправка почтовых сообщений
        // TODO Отправка почтовых сообщений
    }

    /**
     * Получение адресатов
     * @param assigneeAsString
     * @return
     */
    private List<DomainObject> getAssigneeList(Id mainDomainObjectId, String assigneeAsString, AccessToken accessToken) {

        List<DomainObject> result = new ArrayList<DomainObject>();

        //Разделяем по запятой
        String[] assigneeArray = assigneeAsString.split(",");

        for (String assigneeExpression : assigneeArray) {
            // Отрабатываем конструкции PERSON:admin, GROUP:admins, CONTEXT_ROLE:admins, DYNAMIC_GROUP:admins
            if (assigneeExpression.startsWith(PERSON_ASSIGNEE_PREFIX)) {
                //Получаем персону по логину
                String login = assigneeExpression.split(":")[1].trim();
                result.add(domainObjectDao.find(userGroupGlobalCache.getUserIdByLogin(login), accessToken));
            } else if (assigneeExpression.startsWith(GROUP_ASSIGNEE_PREFIX)) {
                //Получене имени группы
                String groupName = assigneeExpression.split(":")[1].trim();
                //Получение объекта группы
                Id groupId = personManagementService.getGroupId(groupName);
                result.add(domainObjectDao.find(groupId, accessToken));
            } else if (assigneeExpression.startsWith(DYNAMIC_GROUP_ASSIGNEE_PREFIX)) {
                //Получение имени группы
                String groupName = assigneeExpression.split(":")[1].trim();
                DomainObject dynGroup = personManagementService.findDynamicGroup(groupName, mainDomainObjectId);
                result.add(dynGroup);                
            } else if (assigneeExpression.startsWith(CONTEXT_ROLE_ASSIGNEE_PREFIX)) {
                //Получение имени контекстной ролиы
                String roleName = assigneeExpression.split(":")[1].trim();
                List<Id> groups = permissionServiceDao.getGroups(mainDomainObjectId, roleName);
                for (Id groupId : groups) {
                    result.add(domainObjectDao.find(groupId, accessToken));
                }
            } else {
                //По умолчанию ожидаем здесь идентификаторы в формате Id.toStringRepresentation()
                Id assigneeId = idService.createId(assigneeExpression);
                DomainObject assignee = domainObjectDao.find(assigneeId, accessToken);
                result.add(assignee);
            }
        }

        return result;
    }

    /**
     * Создание нового доменного обьекта переданного типа
     * 
     * @param type
     * @return
     */
    private DomainObject createDomainObject(String type) {
        GenericDomainObject taskDomainObject = new GenericDomainObject();
        taskDomainObject.setTypeName(type);
        Date currentDate = new Date();
        taskDomainObject.setCreatedDate(currentDate);
        taskDomainObject.setModifiedDate(currentDate);
        return taskDomainObject;
    }
}
