package ru.intertrust.cm.core.gui.impl.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.gui.ActionConfig;
import ru.intertrust.cm.core.config.model.gui.ActionContextActionConfig;
import ru.intertrust.cm.core.config.model.gui.ActionContextConfig;
import ru.intertrust.cm.core.config.model.gui.DomainObjectContextConfig;
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

                                list.add(actionContext);
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
                            list.add(getCompleteTaskActionContext(taskActionItem, taskActionName, domainObject.getId(), actConfig, task));
                        }
                    } else {
                        list.add(getCompleteTaskActionContext(null, task.getString("Name"), domainObject.getId(), actConfig, task));
                    }

                }
            }
            return list;
        } catch (Exception ex) {
            throw new ActionServiceException("Error on getActions", ex);
        }
    }

    private List<Id> getStatusNames(List<String> statuses) {
        String query = "select t.id, t.type_id from status t where t.name in (";

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

        if (actConfig != null) {
            actionContext.setActionConfig(actConfig);
        } else {
            actConfig = new ActionConfig();
            actConfig.setComponent("complete.task.action");
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

}
