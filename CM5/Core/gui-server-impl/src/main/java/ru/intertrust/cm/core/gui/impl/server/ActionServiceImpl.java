package ru.intertrust.cm.core.gui.impl.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.PermissionService;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.AccessMatrixStatusConfig;
import ru.intertrust.cm.core.config.BaseOperationPermitConfig;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ExecuteActionConfig;
import ru.intertrust.cm.core.config.gui.ActionContextChecker;
import ru.intertrust.cm.core.config.gui.AttrValueContextConfig;
import ru.intertrust.cm.core.config.gui.DomainObjectContextConfig;
import ru.intertrust.cm.core.config.gui.action.*;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.StatusDao;
import ru.intertrust.cm.core.gui.api.server.ActionService;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.impl.server.util.PluginHandlerHelper;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.CompleteTaskActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionContext;
import ru.intertrust.cm.core.model.ActionServiceException;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import java.io.*;
import java.util.*;

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

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private StatusDao statusDao;

    @Override
    public List<ActionContext> getActions(Id domainObjectId) {
        try {
            List<ActionContext> list = new ArrayList<ActionContext>();
            if (domainObjectId != null) {
                DomainObject domainObject = crudservice.find(domainObjectId);

                Collection<ActionContextConfig> actionContextConfigs = configurationExplorer.getConfigs(ActionContextConfig.class);
                for (ActionContextConfig actionContextConfig : actionContextConfigs) {
                    Collection<DomainObjectContextConfig> domainObjectContext = actionContextConfig.getDomainObjectContext();
                    boolean actionAvailable = true;

                    if (domainObjectContext != null) {
                        for (DomainObjectContextConfig domainContextConfig : domainObjectContext) {

                            //Учитываем тип
                            if (domainContextConfig.getDomainObjectType() != null) {
                                actionAvailable = domainContextConfig.getDomainObjectType().equalsIgnoreCase(domainObject.getTypeName());
                            }
                            //Учитываем статус                        
                            if (actionAvailable && domainContextConfig.getStatus() != null) {
                                actionAvailable = domainContextConfig.getStatus().equalsIgnoreCase(statusDao.getStatusNameById(domainObject.getStatus()));
                            }
                            //Учитываем значения атрибутов
                            if (actionAvailable && domainContextConfig.getAttribute() != null && domainContextConfig.getAttribute().size() > 0) {
                                //Должны совпадать все атрибуты
                                for (AttrValueContextConfig attrValueContextConfig : domainContextConfig.getAttribute()) {
                                    Value value = domainObject.getValue(attrValueContextConfig.getName());
                                    if (value instanceof StringValue) {
                                        actionAvailable = attrValueContextConfig.getValue().equals(domainObject.getString(attrValueContextConfig.getName()));
                                    } else if (value instanceof LongValue) {
                                        actionAvailable =
                                                Long.valueOf(attrValueContextConfig.getValue()).equals(domainObject.getLong(attrValueContextConfig.getName()));
                                    } else if (value instanceof BooleanValue) {
                                        actionAvailable =
                                                Boolean.valueOf(attrValueContextConfig.getValue())
                                                        .equals(domainObject.getBoolean(attrValueContextConfig.getName()));
                                    } else {
                                        actionAvailable = false;
                                    }

                                    //Вываливаемся из цикла при первом же несовпадении
                                    if (!actionAvailable)
                                        break;
                                }
                            }
                            //Учитываем вычисления в классах
                            if (actionAvailable && domainContextConfig.getClassName() != null && domainContextConfig.getClassName().size() > 0) {
                                for (String className : domainContextConfig.getClassName()) {
                                    //Инстанцируем класс
                                    Class<?> checkerClass = Class.forName(className);
                                    ActionContextChecker checker = (ActionContextChecker) applicationContext
                                            .getAutowireCapableBeanFactory()
                                            .createBean(
                                                    checkerClass,
                                                    AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE,
                                                    false);
                                    actionAvailable = checker.contextAvailable(domainObject);
                                }
                                //Вываливаемся из цикла при первом же несовпадении
                                if (!actionAvailable)
                                    break;
                            }

                            //Если действие доступно то нет нужды проверять остальные domainContextConfig
                            if (actionAvailable)
                                break;
                        }
                    }
                    //Если доменный объект прошел все проверки добавляем все действия
                    if (actionAvailable) {
                        List<ActionContextActionConfig> actionConfigs = actionContextConfig.getAction();
                        for (ActionContextActionConfig actionContextActionConfig : actionConfigs) {
                            ActionConfig actConfig = getActionConfig(actionContextActionConfig.getName(), ActionConfig.class);

                            final ActionContext actionContext;
                            final boolean hasHandler = applicationContext.containsBean(actConfig.getComponentName());
                            if (hasHandler) {
                                final ActionHandler handler =
                                        (ActionHandler) applicationContext.getBean(actConfig.getComponentName());
                                actionContext = handler.getActionContext(actConfig);
                            } else {
                                actionContext = new ActionContext(actConfig);
                            }
                            actionContext.setRootObjectId(domainObject.getId());

                            //Проверка прав
                            if (userGroupGlobalCache.isPersonSuperUser(currentUserAccessor.getCurrentUserId())
                                    || hasActionPermission(domainObjectId, actConfig.getName())) {
                                list.add(actionContext);
                            }
                        }
                    }
                }

                //Действия по процессам
                List<DomainObject> tasks = processService.getUserDomainObjectTasks(domainObject.getId());
                for (DomainObject task : tasks) {
                    //Проверяем наличие настроенных действий в задаче
                    String taskAction = task.getString("Actions");
                    if (taskAction != null && taskAction.length() > 0) {
                        String[] taskActionArray = taskAction.split(";");
                        for (String taskActionAndName : taskActionArray) {
                            String[] taskActionAndNameArr = taskActionAndName.split("=");
                            String taskActionItem = taskActionAndNameArr[0];
                            String taskActionName = taskActionAndNameArr[1];
                            //Проверка прав на задачи процесса
                            String matrixAction = task.getString("ProcessId") + "." + task.getString("ActivityId") + "." + taskActionItem;
                            if (userGroupGlobalCache.isPersonSuperUser(currentUserAccessor.getCurrentUserId())
                                    || !hasActionInAccessMatrix(domainObject, matrixAction) || hasActionPermission(domainObjectId, matrixAction)) {
                                ActionContext taskActionContext = getCompleteTaskActionContext(matrixAction, taskActionName, domainObjectId);
                                fillCompleteTaskContext(taskActionContext, taskActionItem, taskActionName, task);
                                list.add(taskActionContext);
                            }
                        }
                    } else {
                        //Проверка прав на задачи процесса
                        String matrixAction = task.getString("ProcessId") + "." + task.getString("ActivityId");
                        if (userGroupGlobalCache.isPersonSuperUser(currentUserAccessor.getCurrentUserId())
                                || !hasActionInAccessMatrix(domainObject, matrixAction) || hasActionPermission(domainObjectId, matrixAction)) {
                            ActionContext taskActionContext = getCompleteTaskActionContext(matrixAction, task.getString("Name"), domainObjectId);
                            fillCompleteTaskContext(taskActionContext, null, task.getString("Name"), task);
                            list.add(taskActionContext);
                        }
                    }
                }
            }
            return list;
        } catch (Exception ex) {
            throw new ActionServiceException("Error on getActions", ex);
        }
    }

    private ActionContext getCompleteTaskActionContext(String complateTaskActionName, String description, Id rootDomainObjectId) throws Exception{
        ActionConfig config = getActionConfig(complateTaskActionName, ActionConfig.class);
        
        //Клонируем конфигурацию, так как позднее она будет модифицирована в кодом        
        ActionConfig actConfig = clone(config);
        
        final ActionContext actionContext;
        boolean hasHandler = false;
        if (actConfig == null) {
            actConfig = new SimpleActionConfig("generic.workflow.action");
        }
        if (actConfig.getComponentName() != null) {
            hasHandler = applicationContext.containsBean(actConfig.getComponentName());
        }
        if (hasHandler) {
            final ActionHandler handler =
                    (ActionHandler) applicationContext.getBean(actConfig.getComponentName());
            actionContext = handler.getActionContext(actConfig);
        } else {
            actionContext = new SimpleActionContext(actConfig);
        }
        actionContext.setRootObjectId(rootDomainObjectId);
        return actionContext;
    }

    public ActionConfig clone(ActionConfig config) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        serializeToOutputStream(config, bos);
        byte[] bytes = bos.toByteArray();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
        return (ActionConfig) ois.readObject();
    }

    private void serializeToOutputStream(Serializable ser, OutputStream os) throws IOException {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(os);
            oos.writeObject(ser);
            oos.flush();
        } finally {
            oos.close();
        }
    }

    /**
     * Проверка прав у текущего пользователя на выполнение действия
     * @param domainObjectId
     * @param action
     * @return
     */
    private boolean hasActionPermission(Id domainObjectId, String action) {
        boolean result = false;

        DomainObjectPermission permission = permissionService.getObjectPermission(domainObjectId, currentUserAccessor.getCurrentUserId());
        if (permission != null && permission.getActions() != null) {
            result = permission.getActions().contains(action);
        }
        return result;
    }

    private boolean hasActionInAccessMatrix(DomainObject domainObject, String action) {
        AccessMatrixStatusConfig matrix =
                configurationExplorer.getAccessMatrixByObjectTypeAndStatus(domainObject.getTypeName(), statusDao.getStatusNameById(domainObject.getStatus()));
        boolean result = false;
        if (matrix != null){
            for (BaseOperationPermitConfig permission : matrix.getPermissions()) {
                if (permission instanceof ExecuteActionConfig) {
                    ExecuteActionConfig executeActionConfig = (ExecuteActionConfig) permission;
                    if (executeActionConfig.getName().equals(action)) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

    private void fillCompleteTaskContext(ActionContext actionContext, String action, String name, DomainObject task) {
        if (actionContext instanceof CompleteTaskActionContext) { // todo удалить вместе с CompleteTaskActionContext
            final CompleteTaskActionContext context = (CompleteTaskActionContext) actionContext;
            context.setTaskAction(action);
            context.setTaskId(task.getId());
            context.setActivityId(task.getString("ActivityId"));
        }
        final ActionConfig actionConfig = actionContext.getActionConfig();
        if (actionConfig.getText() == null){
            actionConfig.setText(name);
        }
        actionConfig.getProperties().put(PluginHandlerHelper.WORKFLOW_PROCESS_TYPE_KEY, "complete.task");
        actionConfig.getProperties().put("complete.task.action", action);
        actionConfig.getProperties().put("complete.task.id", task.getId().toStringRepresentation());
        actionConfig.getProperties().put("complete.activity.id", task.getString("ActivityId"));
    }

    @Override
    public List<ActionContext> getActions(String domainObjectType) {
        // TODO Реализовать получение действий для нового объекта. для этого
        // надо статус взять по умолчанию для конфигурации
        return null;
    }

    @Override
    public ToolBarConfig getDefaultToolbarConfig(String pluginName, String currentLocale) {
        return configurationExplorer.getDefaultToolbarConfig(pluginName, currentLocale);
    }

    @Override
    public <T extends BaseActionConfig> T getActionConfig(final String name, Class<T> type) {
        T result = configurationExplorer.getLocalizedConfig(type, name, GuiContext.getUserLocale());
        if (result == null) {
            result = type.cast(configurationExplorer.getLocalizedConfig(SimpleActionConfig.class, name, GuiContext.getUserLocale()));
            return result;
        }
        return type.cast(result);
    }
}
