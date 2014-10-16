package ru.intertrust.cm.core.gui.impl.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.PermissionService;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.DomainObjectPermission;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.AccessMatrixStatusConfig;
import ru.intertrust.cm.core.config.BaseOperationPermitConfig;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ExecuteActionConfig;
import ru.intertrust.cm.core.config.gui.DomainObjectContextConfig;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.action.ActionContextActionConfig;
import ru.intertrust.cm.core.config.gui.action.ActionContextConfig;
import ru.intertrust.cm.core.config.gui.action.SimpleActionConfig;
import ru.intertrust.cm.core.config.gui.action.ToolBarConfig;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.gui.api.server.ActionService;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.CompleteTaskActionContext;
import ru.intertrust.cm.core.model.ActionServiceException;

@Stateless
@Local(ActionService.class)
@Remote(ActionService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class ActionServiceImpl implements ActionService, ActionService.Remote {

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @EJB
    private CrudService crudservice;

    @EJB
    private CollectionsService collectionService;

    @EJB
    private ProcessService processService;

    @EJB
    private PermissionService permissionService;

    @Autowired
    private UserGroupGlobalCache userGroupGlobalCache;

    @Autowired
    private CurrentUserAccessor currentUserAccessor;

    @Override
    public List<ActionContext> getActions(Id domainObjectId) {
        try {
            List<ActionContext> list = new ArrayList<ActionContext>();
            if (domainObjectId != null) {
                DomainObject domainObject = crudservice.find(domainObjectId);

                Collection<ActionContextConfig> actionContextConfigs = configurationExplorer.getConfigs(ActionContextConfig.class);
                for (ActionContextConfig actionContextConfig : actionContextConfigs) {
                    Collection<DomainObjectContextConfig> domainObjectContext = actionContextConfig.getDomainObjectContext();
                    for (DomainObjectContextConfig domainContextConfig : domainObjectContext) {
                        List<String> domainObjectTypes = domainContextConfig.getDomainObjectType();
                        List<Id> domainObjectStatusIds = getStatusNames(domainContextConfig.getStatus());

                        if (domainObjectTypes.contains(domainObject.getTypeName())
                                && domainObjectStatusIds.contains(domainObject.getStatus())) {
                            List<ActionContextActionConfig> actionConfigs = actionContextConfig.getAction();
                            for (ActionContextActionConfig actionContextActionConfig : actionConfigs) {
                                ActionConfig actConfig = configurationExplorer.getConfig(ActionConfig.class, actionContextActionConfig.getName());

                                ActionContext actionContext = null;
                                if (actConfig.getActionSettings() != null && actConfig.getActionSettings().getActionContextClass() != null) {
                                    actionContext = (ActionContext) actConfig.getActionSettings().getActionContextClass().newInstance();
                                } else {
                                    actionContext = new ActionContext();
                                }

                                actionContext.setActionConfig(actConfig);
                                actionContext.setRootObjectId(domainObject.getId());

                                //Проверка прав
                                if (userGroupGlobalCache.isPersonSuperUser(currentUserAccessor.getCurrentUserId()) || hasActionPermission(domainObjectId, actConfig.getName())){
                                    list.add(actionContext);
                                }
                            }
                        }

                    }
                }

                List<DomainObject> tasks = processService.getUserDomainObjectTasks(domainObject.getId());
                for (DomainObject task : tasks) {
                    ActionConfig actConfig = configurationExplorer.getConfig(ActionConfig.class, task.getString("ActivityId"));
                    CompleteTaskActionContext actionContext = new CompleteTaskActionContext();
                    actionContext.setRootObjectId(domainObject.getId());

                    String taskAction = task.getString("Actions");
                    if (taskAction != null && taskAction.length() > 0) {
                        String[] taskActionArray = taskAction.split(";");
                        for (String taskActionAndName : taskActionArray) {
                            String[] taskActionAndNameArr = taskActionAndName.split("=");
                            String taskActionItem = taskActionAndNameArr[0];
                            String taskActionName = taskActionAndNameArr[1];
                            //Проверка прав на задачи процесса
                            String matrixAction = task.getString("ProcessId") + "." + task.getString("ActivityId") + "." + taskActionItem;
                            if (userGroupGlobalCache.isPersonSuperUser(currentUserAccessor.getCurrentUserId()) || !hasActionInAccessMatrix(domainObject, matrixAction) || hasActionPermission(domainObjectId, matrixAction)){
                                list.add(getCompleteTaskActionContext(taskActionItem, taskActionName, domainObject.getId(), actConfig, task));
                            }
                        }
                    } else {
                        //Проверка прав на задачи процесса
                        String matrixAction = task.getString("ProcessId") + "." + task.getString("ActivityId");
                        if (userGroupGlobalCache.isPersonSuperUser(currentUserAccessor.getCurrentUserId()) || !hasActionInAccessMatrix(domainObject, matrixAction) || hasActionPermission(domainObjectId, matrixAction)){
                            list.add(getCompleteTaskActionContext(null, task.getString("Name"), domainObject.getId(), actConfig, task));
                        }
                    }

                }
            }
            return list;
        } catch (Exception ex) {
            throw new ActionServiceException("Error on getActions", ex);
        }
    }

    /**
     * Проверка прав у текущего пользователя на выполнение действия
     * @param domainObjectId
     * @param action
     * @return
     */
    private boolean hasActionPermission(Id domainObjectId, String action){
        boolean result = false;
        DomainObjectPermission permission = permissionService.getObjectPermission(domainObjectId);
        if (permission != null && permission.getActions() != null){
            result = permission.getActions().contains(action);
        }
        return result;
    }

    private boolean hasActionInAccessMatrix(DomainObject domainObject, String action){
        AccessMatrixStatusConfig matrix = configurationExplorer.getAccessMatrixByObjectTypeAndStatus(domainObject.getTypeName(), getStatusName(domainObject.getStatus()));
        boolean result = false;
        for (BaseOperationPermitConfig permission : matrix.getPermissions()) {
            if (permission instanceof ExecuteActionConfig){
                ExecuteActionConfig executeActionConfig = (ExecuteActionConfig)permission;
                if (executeActionConfig.getName().equals(action)){
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    private String getStatusName(Id statusId) {
        String query = "select t.name from " + GenericDomainObject.STATUS_DO + " t where t.id = {0}";
        List<Value> params = new ArrayList<Value>();
        params.add(new ReferenceValue(statusId));

        IdentifiableObjectCollection collection = collectionService.findCollectionByQuery(query, params);
        String result = null;
        if (collection.size() > 0){
            result = collection.get(0).getString("name");
        }
        return result;

    }

    private List<Id> getStatusNames(List<String> statuses) {
        String query = "select t.id from " + GenericDomainObject.STATUS_DO + " t where t.name in (";

        boolean first = true;
        for (String statusName : statuses) {
            if (first) {
                first = false;
            } else {
                query += ",";
            }
            query += "'" + statusName + "'";
        }

        query += ")";

        IdentifiableObjectCollection collection = collectionService.findCollectionByQuery(query);

        List<Id> result = new ArrayList<Id>();

        for (IdentifiableObject item : collection) {
            result.add(item.getId());

        }

        return result;
    }

    private ActionContext getCompleteTaskActionContext(String action, String name, Id mainAttachmentId, ActionConfig actConfig, DomainObject task) {
        CompleteTaskActionContext actionContext = new CompleteTaskActionContext();
        actionContext.setRootObjectId(mainAttachmentId);

        actionContext.setTaskAction(action);
        actionContext.setTaskId(task.getId());
        actionContext.setActivityId(task.getString("ActivityId"));

        if (actConfig != null) {
            actionContext.setActionConfig(actConfig);
        } else {
            actConfig = new ActionConfig("complete.task.action");
            actConfig.setText(name);
            actionContext.setActionConfig(actConfig);
        }
        return actionContext;
    }

    @Override
    public List<ActionContext> getActions(String domainObjectType) {
        // TODO Реализовать получение действий для нового объекта. для этого
        // надо статус взять по умолчанию для конфигурации
        return null;
    }

    @Override
    public ToolBarConfig getDefaultToolbarConfig(String pluginName) {
        return configurationExplorer.getDefaultToolbarConfig(pluginName);
    }

    @Override
    public ActionConfig getActionConfig(final String name) {
        ActionConfig result = configurationExplorer.getConfig(ActionConfig.class, name);
        if (result == null) {
            result = configurationExplorer.getConfig(SimpleActionConfig.class, name);
        }
        return result;
    }
}
