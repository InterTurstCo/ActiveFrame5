package ru.intertrust.cm.core.business.impl.workflow;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.ProcessVariable;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.workflow.WorkflowEngine;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.PersonServiceDao;
import ru.intertrust.cm.core.dao.api.StatusDao;
import ru.intertrust.cm.core.model.ProcessException;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public abstract class AbstactWorkflowEngine implements WorkflowEngine {
    @Autowired
    protected CurrentUserAccessor currentUserAccessor;

    @Autowired
    protected PersonServiceDao personService;

    @Autowired
    protected DomainObjectDao domainObjectDao;

    @Autowired
    protected CollectionsDao collectionsDao;

    @Autowired
    protected AccessControlService accessControlService;

    @Autowired
    private StatusDao statusDao;

    @Override
    public List<DomainObject> getUserTasks() {
        try {
            List<DomainObject> result = new ArrayList<DomainObject>();
            String personLogin = currentUserAccessor.getCurrentUser();
            Id personId = personService.findPersonByLogin(personLogin).getId();
            result = getUserTasks(personId);
            return result;
        } catch (Exception ex) {
            throw new ProcessException("Error get user tasks", ex);
        }
    }

    @Override
    public List<DomainObject> getUserDomainObjectTasks(Id attachedObjectId) {
        try {
            List<DomainObject> result = new ArrayList<DomainObject>();
            String personLogin = currentUserAccessor.getCurrentUser();
            DomainObject personByLogin = personService.findPersonByLogin(personLogin);
            Id personId = personByLogin.getId();
            if (personId != null) {
                result = getUserDomainObjectTasks(attachedObjectId, personId);
            }
            return result;
        } catch (Exception ex) {
            throw new ProcessException("Error get user domain object tasks", ex);
        }
    }

    @Override
    public void completeTask(Id taskDomainObjectId,
                             List<ProcessVariable> variables, String action) {
        try {
            String personLogin = currentUserAccessor.getCurrentUser();
            Id personId = personService.findPersonByLogin(personLogin).getId();

            //Проверка на то что задача дейцствительно есть у текущего пользователя
            if (!hasUserTask(personId, taskDomainObjectId)) {
                throw new ProcessException("Person " + personLogin + " does not have task with id=" + taskDomainObjectId.toStringRepresentation());
            }

            // TODO Переделать правила доступа после реализации прав
            AccessToken accessToken = accessControlService
                    .createSystemAccessToken("ProcessService");

            DomainObject taskDomainObject = domainObjectDao.find(
                    taskDomainObjectId, accessToken);
            taskDomainObject = domainObjectDao.setStatus(taskDomainObject.getId(),
                    statusDao.getStatusIdByName(ProcessService.TASK_STATE_COMPLETE), accessToken);

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

            onCompleteTask(taskDomainObject, params);
        } catch (Exception ex) {
            throw new ProcessException("Error complete task", ex);
        }

    }

    protected abstract void onCompleteTask(DomainObject taskDomainObject, Map<String, String> params);


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
        } catch (Exception ex) {
            throw new ProcessException("Error get user domain object tasks", ex);
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
        } catch (Exception ex) {
            throw new ProcessException("Error get user tasks", ex);
        }
    }

    /**
     * Проверка имеет ли пользователь задачи
     * @param personId
     * @param taskId
     * @return
     */
    protected boolean hasUserTask(Id personId, Id taskId) {
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
}
