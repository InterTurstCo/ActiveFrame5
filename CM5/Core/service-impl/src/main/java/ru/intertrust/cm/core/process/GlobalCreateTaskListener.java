package ru.intertrust.cm.core.process;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.activiti.engine.FormService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.TaskFormData;
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
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.tools.SpringClient;

/**
 * Глобальный слушатель создания UserTask. Создает соответствующий доменный
 * объект и выполняет отсылку по электронной почте
 */
public class GlobalCreateTaskListener extends SpringClient implements
        TaskListener, ExecutionListener {

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

    /**
     * Входная точка слушителя. вызывается при создание пользовательской задачи
     */
    @Override
    public void notify(DelegateTask delegateTask) {
        // Создание доменного обьекта Task
        DomainObject taskDomainObject = createDomainObject("Person_Task");
        taskDomainObject.setString("TaskId", delegateTask.getId());
        taskDomainObject.setString("ActivityId", delegateTask.getTaskDefinitionKey());
        taskDomainObject.setString("Name", delegateTask.getName());
        taskDomainObject
                .setString("Description", delegateTask.getDescription());
        taskDomainObject.setLong("Priority", (long) delegateTask.getPriority());
        taskDomainObject.setString("ExecutionId", delegateTask.getExecutionId());

        // TODO установка статуса отправлено. Временно сделано через просто
        // поле в дальнейшем надо
        // переделать на системное поле state доменного объекта
        taskDomainObject.setLong("State", ProcessService.TASK_STATE_SEND);

        String mainAttachmentId = ((Id) delegateTask.getVariable("MAIN_ATTACHMENT_ID")).toStringRepresentation();
        if (mainAttachmentId != null) {
            taskDomainObject.setString("MainAttachment", mainAttachmentId);
        }
        // Получение полей формы задачи ACTIONS и сохранение его в обьект
        // задача
        TaskFormData taskData = formService.getTaskFormData(delegateTask.getId());
        List<FormProperty> formProperties = taskData.getFormProperties();
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
        }

        // Сохранение доменного объекта
        taskDomainObject = domainObjectDao.save(taskDomainObject);
        Id assigneeId = idService.createId(delegateTask.getAssignee());
        AccessToken accessToken = accessControlService
                .createSystemAccessToken("GlobalCreateTaskListener");
        DomainObject assignee = domainObjectDao.find(assigneeId, accessToken);

        // Создание связанного AssigneePerson или AssigneeGroup
        if (assignee.getTypeName().equals("UserGroup")) {
            DomainObject assigneePersonDomainObject = createDomainObject("Assignee_Group");

            assigneePersonDomainObject.setReference("PersonTask",
                    taskDomainObject);
            assigneePersonDomainObject.setReference("UserGroup",
                    idService.createId(delegateTask.getAssignee()));

            domainObjectDao.save(assigneePersonDomainObject);
        } else {
            DomainObject assigneePersonDomainObject = createDomainObject("Assignee_Person");

            assigneePersonDomainObject.setReference("PersonTask",
                    taskDomainObject);
            assigneePersonDomainObject.setReference("Person",
                    idService.createId(delegateTask.getAssignee()));

            domainObjectDao.save(assigneePersonDomainObject);
        }

        // Отправка почтовых сообщений
        // TODO Отправка почтовых сообщений
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

    /**
     * Точка входа при выходе из задачи. переопределяется чтобы установить
     * статус у задач, которые были завершены с помощью event
     */
    @Override
    public void notify(DelegateExecution execution) throws Exception {
        List<DomainObject> tasks = getNotCompleteTasks(execution.getId(), execution.getCurrentActivityId());

        for (DomainObject domainObjectTask : tasks) {
            domainObjectTask.setLong("State", ProcessService.TASK_STATE_PRETERMIT);
            domainObjectDao.save(domainObjectTask);
        }
    }

    /**
     * Получение всех не завершенных задач и установка у них статуса прервана
     * @param executionId
     * @param taskId
     * @return
     */
    private List<DomainObject> getNotCompleteTasks(String executionId, String taskId) {
        // TODO Костыль, нужно избавлятся от использования идентификатора как
        // int или
        // long
        /*
         * int personIdAsint =
         * Integer.parseInt(personId.toStringRepresentation()
         * .substring(personId.toStringRepresentation().indexOf('|') + 1));
         */
        // поиск задач отправленных пользователю, или любой группе в которую
        // входит пользователь

        List<Filter> filters = new ArrayList<>();
        Filter filter = new Filter();
        filter.setFilter("byExecutionIdAndTaskId");
        StringValue sv = new StringValue(executionId);
        filter.addCriterion(0, sv);
        sv = new StringValue(taskId);
        filter.addCriterion(1, sv);
        filters.add(filter);

        /*
         * AccessToken accessToken = accessControlService
         * .createCollectionAccessToken(personIdAsint);
         */
        // TODO пока права не работают работаю от имени админа
        AccessToken accessToken = accessControlService
                .createSystemAccessToken("ProcessService");

        IdentifiableObjectCollection collection = collectionsDao
                .findCollection("NotCompleteTask", filters, null, 0, 0, accessToken);

        List<DomainObject> result = new ArrayList<DomainObject>();
        for (IdentifiableObject item : collection) {
            DomainObject group = domainObjectDao
                    .find(item.getId(), accessToken);
            result.add(group);
        }
        return result;
    }

}
