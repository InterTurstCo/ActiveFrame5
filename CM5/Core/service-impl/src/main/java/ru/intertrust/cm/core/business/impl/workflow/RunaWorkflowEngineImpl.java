package ru.intertrust.cm.core.business.impl.workflow;

import org.aspectj.weaver.ast.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.DeployedProcess;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.ProcessVariable;
import ru.intertrust.cm.core.business.api.workflow.WorkflowTaskAddressee;
import ru.intertrust.cm.core.business.api.workflow.WorkflowTaskData;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.model.ProcessException;
import ru.runa.wfe.webservice.Actor;
import ru.runa.wfe.webservice.AuthenticationAPI;
import ru.runa.wfe.webservice.AuthenticationWebService;
import ru.runa.wfe.webservice.AuthorizationAPI;
import ru.runa.wfe.webservice.AuthorizationWebService;
import ru.runa.wfe.webservice.BatchPresentation;
import ru.runa.wfe.webservice.DefinitionAPI;
import ru.runa.wfe.webservice.DefinitionWebService;
import ru.runa.wfe.webservice.ExecutionAPI;
import ru.runa.wfe.webservice.ExecutionWebService;
import ru.runa.wfe.webservice.ExecutorAPI;
import ru.runa.wfe.webservice.ExecutorWebService;
import ru.runa.wfe.webservice.Group;
import ru.runa.wfe.webservice.TaskAPI;
import ru.runa.wfe.webservice.TaskWebService;
import ru.runa.wfe.webservice.User;
import ru.runa.wfe.webservice.Variable;
import ru.runa.wfe.webservice.WfDefinition;
import ru.runa.wfe.webservice.WfExecutor;
import ru.runa.wfe.webservice.WfTask;
import ru.runa.wfe.webservice.WfVariable;

import javax.xml.ws.Service;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RunaWorkflowEngineImpl extends AbstactWorkflowEngine {
    private static final Logger logger = LoggerFactory.getLogger(RunaWorkflowEngineImpl.class);
    public static final String PROCESS_TYPE = "AF5";
    public static final String ENGENE_NAME = "runa";

    @Value("${runa.wfe.url}")
    private String url;
    @Value("${runa.wfe.system.username}")
    private String systemUsername;
    @Value("${runa.wfe.system.password}")
    private String systemPassword;
    @Value("${runa.wfe.user.password}")
    private String userPassword;

    @Autowired
    private CurrentUserAccessor currentUserAccessor;

    @Override
    public String startProcess(String processName, Id attachedObjectId, List<ProcessVariable> variables) {
        try {
            // Подключаемся к серверу
            AuthenticationAPI authenticationAPI = new AuthenticationWebService(getServiceUrl(AuthenticationWebService.class)).getAuthenticationAPIPort();
            User user = authenticationAPI.authenticateByLoginPassword(systemUsername, systemPassword);
            ExecutionAPI executionAPI = new ExecutionWebService(getServiceUrl(ExecutionWebService.class)).getExecutionAPIPort();

            List<Variable> runaProcessVariables = new ArrayList<>();

            if (variables != null) {
                for (ProcessVariable variable : variables) {
                    runaProcessVariables.add(createVariable(variable.getName(), variable.getValue().toString()));
                }
            }

            if (attachedObjectId != null) {
                runaProcessVariables.add(createVariable(ProcessService.CTX_ID,
                        attachedObjectId.toStringRepresentation()));
            }

            Long processId = executionAPI.startProcessWS(user, processName, runaProcessVariables);
            return String.valueOf(processId);
        } catch (Exception ex) {
            throw new ProcessException("Error start process", ex);
        }
    }

    private Variable createVariable(String name, String value) {
        Variable usersListVariable = new Variable();
        usersListVariable.setName(name);
        usersListVariable.setValue(value);
        return usersListVariable;
    }

    @Override
    public void terminateProcess(String processId) {
        try {
            // Подключаемся к серверу
            AuthenticationAPI authenticationAPI = new AuthenticationWebService(getServiceUrl(AuthenticationWebService.class)).getAuthenticationAPIPort();
            User user = authenticationAPI.authenticateByLoginPassword(systemUsername, systemPassword);
            ExecutionAPI executionAPI = new ExecutionWebService(getServiceUrl(ExecutionWebService.class)).getExecutionAPIPort();

            executionAPI.cancelProcess(user, Long.parseLong(processId));
        } catch (Exception ex) {
            throw new ProcessException("Error start process", ex);
        }
    }

    @Override
    public String deployProcess(byte[] processDefinition, String processName) {
        try {
            logger.debug("Deploy process", processName);

            // Подключаемся к серверу
            AuthenticationAPI authenticationAPI = new AuthenticationWebService(getServiceUrl(AuthenticationWebService.class)).getAuthenticationAPIPort();
            User user = authenticationAPI.authenticateByLoginPassword(systemUsername, systemPassword);
            DefinitionAPI definitionAPI = new DefinitionWebService(getServiceUrl(DefinitionWebService.class)).getDefinitionAPIPort();
            AuthorizationAPI authorizationAPI = new AuthorizationWebService(getServiceUrl(AuthorizationWebService.class)).getAuthorizationAPIPort();
            ExecutorAPI executorAPI = new ExecutorWebService(getServiceUrl(ExecutorWebService.class)).getExecutorAPIPort();

            // Получение установленноого процесса
            List<WfDefinition> definitions = definitionAPI.getProcessDefinitionHistory(user, processName);

            // Установка процесса
            WfDefinition wfDefinition = null;
            if (definitions.size() == 0) {
                logger.debug("Process definition {} not found, deploy as new", processName);
                wfDefinition = definitionAPI.deployProcessDefinition(user, processDefinition, Collections.singletonList(PROCESS_TYPE));
            } else {
                logger.debug("Process definition {} found, update it", processName);
                // TODO проверка идентичности устанавливаемой и ранее установленной версии

                wfDefinition = definitionAPI.getLatestProcessDefinition(user, processName);
                wfDefinition = definitionAPI.redeployProcessDefinition(user, wfDefinition.getId(), processDefinition, Collections.singletonList(PROCESS_TYPE));
            }

            // Разрешаем запускать всем
            /* Не работает из за ошибки в Runa org.apache.cxf.binding.soap.SoapFault: Unmarshalling Error: Unable to create an instance of ru.runa.wfe.security.SecuredObject
            WfExecutor allPersonsGroup = executorAPI.getExecutorByName(user, "AllPersons");
            authorizationAPI.setPermissions(user, allPersonsGroup.getId(), Collections.singletonList("START"), wfDefinition);
            */


            // TODO сохранение данных об установленной версии

            logger.debug("Process definition {} deploed. ID={}", processName, wfDefinition.getId());
            return String.valueOf(wfDefinition.getId());
        } catch (Exception ex) {
            throw new ProcessException("Error deploy process", ex);
        }
    }

    /**
     * Получение адреса сервиса
     *
     * @param clazz
     * @return
     * @throws MalformedURLException
     */
    private URL getServiceUrl(Class<? extends Service> clazz) throws MalformedURLException {
        String baseUrl = url;
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }

        URL result = null;
        if (clazz.equals(AuthenticationWebService.class)) {
            result = new URL(baseUrl + "AuthenticationWebService/AuthenticationAPI?wsdl");
        } else if (clazz.equals(DefinitionWebService.class)) {
            result = new URL(baseUrl + "DefinitionWebService/DefinitionAPI?wsdl");
        } else if (clazz.equals(ExecutionWebService.class)) {
            result = new URL(baseUrl + "ExecutionWebService/ExecutionAPI?wsdl");
        } else if (clazz.equals(TaskWebService.class)) {
            result = new URL(baseUrl + "TaskWebService/TaskAPI?wsdl");
        } else if (clazz.equals(ExecutorWebService.class)) {
            result = new URL(baseUrl + "ExecutorWebService/ExecutorAPI?wsdl");
        } else if (clazz.equals(AuthorizationWebService.class)) {
            result = new URL(baseUrl + "AuthorizationWebService/AuthorizationAPI?wsdl");
        } else {
            throw new UnsupportedOperationException("Not support service " + clazz.getName());
        }
        return result;
    }

    @Override
    public void undeployProcess(String processDefinitionId, boolean cascade) {
        try {
            logger.debug("Undeploy process {}", processDefinitionId);

            // Подключаемся к серверу
            AuthenticationAPI authenticationAPI = new AuthenticationWebService(getServiceUrl(AuthenticationWebService.class)).getAuthenticationAPIPort();
            User user = authenticationAPI.authenticateByLoginPassword(systemUsername, systemPassword);
            DefinitionAPI definitionAPI = new DefinitionWebService(getServiceUrl(DefinitionWebService.class)).getDefinitionAPIPort();

            // Получение установленноого процесса
            List<WfDefinition> definitions = definitionAPI.getProcessDefinitionHistory(user, processDefinitionId);

            for (WfDefinition definition : definitions) {
                logger.debug("Undeploy process {}, version", processDefinitionId, definition.getVersion());
                definitionAPI.undeployProcessDefinition(user, processDefinitionId, definition.getVersion());
            }

            // TODO удаление данных об установленной версии
        } catch (Exception ex) {
            throw new ProcessException("Error undeploy process", ex);
        }
    }

    @Override
    protected void onCompleteTask(DomainObject taskDomainObject, Map<String, String> params) {
        try {
            // Подключаемся к серверу
            AuthenticationAPI authenticationAPI = new AuthenticationWebService(getServiceUrl(AuthenticationWebService.class)).getAuthenticationAPIPort();
            User user = authenticationAPI.authenticateByLoginPassword(currentUserAccessor.getCurrentUser(), userPassword); // TODO пароль взять из настройки authentication_info
            TaskAPI taskAPI = new TaskWebService(getServiceUrl(TaskWebService.class)).getTaskAPIPort();

            String taskId = taskDomainObject.getString("TaskId");

            List<Variable> vars = new ArrayList<>();
            for (String name : params.keySet()) {
                Variable var = new Variable();

                String varName = name;
                if (name.equalsIgnoreCase("ACTIONS")){
                    varName = taskDomainObject.getString("ActivityId") + "_RESULT";
                }
                var.setName(varName);
                var.setScriptingName(varName);

                var.setFormat("string");
                var.setValue(params.get(name));
                vars.add(var);
            }

            taskAPI.completeTaskWS(user, Long.parseLong(taskId), vars, null);
        } catch (Exception ex) {
            throw new ProcessException("Error complete process", ex);
        }
    }

    @Override
    public List<DeployedProcess> getDeployedProcesses() {
        try {
            // Подключаемся к серверу
            AuthenticationAPI authenticationAPI = new AuthenticationWebService(getServiceUrl(AuthenticationWebService.class)).getAuthenticationAPIPort();
            User user = authenticationAPI.authenticateByLoginPassword(systemUsername, systemPassword);
            DefinitionAPI definitionAPI = new DefinitionWebService(getServiceUrl(DefinitionWebService.class)).getDefinitionAPIPort();

            List<WfDefinition> definitions = definitionAPI.getProcessDefinitions(user, null, false);
            List<DeployedProcess> result = new ArrayList<>();
            for (WfDefinition definition : definitions) {
                DeployedProcess item = new DeployedProcess();
                item.setId(String.valueOf(definition.getId()));
                item.setName(definition.getName());
                item.setDeployedTime(definition.getUpdateDate().toGregorianCalendar().getTime());
                item.setCategory(definition.getCategories().get(0));
                result.add(item);
            }

            return result;
        } catch (Exception ex) {
            throw new ProcessException("Error get deployed processes", ex);
        }
    }

    @Override
    public void sendProcessMessage(String processName, Id contextId, String message, List<ProcessVariable> variables) {
        throw new UnsupportedOperationException("Not support by Runa WFE");
    }

    @Override
    public void sendProcessSignal(String signal) {
        throw new UnsupportedOperationException("Not support by Runa WFE");
    }

    @Override
    public List<WorkflowTaskData> getEngeneTasks() {
        try {
            // Подключаемся к серверу
            AuthenticationAPI authenticationAPI = new AuthenticationWebService(getServiceUrl(AuthenticationWebService.class)).getAuthenticationAPIPort();
            User user = authenticationAPI.authenticateByLoginPassword(systemUsername, systemPassword);
            TaskAPI taskAPI = new TaskWebService(getServiceUrl(TaskWebService.class)).getTaskAPIPort();
            ExecutionAPI executionAPI = new ExecutionWebService(getServiceUrl(ExecutionWebService.class)).getExecutionAPIPort();
            ExecutorAPI executorAPI = new ExecutorWebService(getServiceUrl(ExecutorWebService.class)).getExecutorAPIPort();

            // Получение задач пользователей у движка процессов
            List<WfTask> tasks = taskAPI.getTasks(user, null);

            // Формируем результат
            List<WorkflowTaskData> result = new ArrayList<>();
            for (WfTask task : tasks) {
                WorkflowTaskData taskInfo = new WorkflowTaskData();
                taskInfo.setTaskId(String.valueOf(task.getId()));
                taskInfo.setProcessId(String.valueOf(task.getDefinitionId()));
                taskInfo.setActivityId(task.getNodeId());
                taskInfo.setName(task.getName());
                taskInfo.setDescription(task.getDescription());
                taskInfo.setExecutionId(String.valueOf(task.getProcessId()));

                Variable ctx = executionAPI.getVariableWS(user, task.getProcessId(), ProcessService.CTX_ID);
                if (ctx != null) {
                    taskInfo.setContext(trimVariable(ctx.getValue()));
                }

                WfExecutor owner = task.getOwner();
                if (owner.getExecutorClassName().equals("ru.runa.wfe.user.TemporaryGroup")){
                    Group group = new Group();
                    group.setId(owner.getId());
                    group.setName(owner.getName());
                    List<Actor> actors = executorAPI.getGroupActors(user, group);
                    for (Actor actor : actors) {
                        taskInfo.addAddressee(new WorkflowTaskAddressee(actor.getName(), false));
                    }
                }else if(owner.getExecutorClassName().equals("ru.runa.wfe.user.Group")){
                    taskInfo.addAddressee(new WorkflowTaskAddressee(owner.getName(), true));
                }else{
                    taskInfo.addAddressee(new WorkflowTaskAddressee(owner.getName(), false));
                }

                // ACTIONS
                Variable actions = executionAPI.getVariableWS(user, task.getProcessId(), task.getNodeId() + "_ACTIONS");
                if (actions != null){
                    taskInfo.setActions(trimVariable(actions.getValue()));
                }

                result.add(taskInfo);
            }

            return result;
        } catch (Exception ex) {
            throw new ProcessException("Error get engene tasks", ex);
        }
    }

    @Override
    public boolean createOrUpdateGroup(String name, Set<String> persons) {
        try{
            // Подключаемся к серверу
            AuthenticationAPI authenticationAPI = new AuthenticationWebService(getServiceUrl(AuthenticationWebService.class)).getAuthenticationAPIPort();
            User user = authenticationAPI.authenticateByLoginPassword(systemUsername, systemPassword);
            ExecutorAPI executorAPI = new ExecutorWebService(getServiceUrl(ExecutorWebService.class)).getExecutorAPIPort();

            boolean result = false;
            // Создание группы если нет
            WfExecutor executor = null;
            if (executorAPI.isExecutorExist(user, name)){
                executor = executorAPI.getExecutorByName(user, name);
            }else{
                executor = new WfExecutor();
                executor.setName(name);
                executor.setExecutorClassName("ru.runa.wfe.user.Group");
                executor.setDescription("Created by AF5");
                executor = executorAPI.create(user, executor);
                result = true;
            }

            // Синхронизация пользователей
            Group group = new Group();
            group.setId(executor.getId());
            group.setName(executor.getName());
            List<WfExecutor> currentMembers = executorAPI.getAllExecutorsFromGroup(user, group);
            // Перекладываем в Map
            Map<String, Long> currentMembersMap = new HashMap();
            for (WfExecutor currentMember: currentMembers){
                currentMembersMap.put(currentMember.getName(), currentMember.getId());
            }
            // Поиск пользователей которых нет в группе
            List<Long> addExecutors = new ArrayList<>();
            for (String person : persons) {
                if (!currentMembersMap.containsKey(person)){
                    WfExecutor addWfExecutor = executorAPI.getExecutorByName(user, person);
                    addExecutors.add(addWfExecutor.getId());
                }
            }
            if (addExecutors.size() > 0) {
                executorAPI.addExecutorsToGroup(user, addExecutors, group.getId());
                result = true;
            }

            // Поиск пользователей которые удалены из группы
            List<Long> deleteExecutors = new ArrayList<>();
            for (String currentMember : currentMembersMap.keySet()) {
                if (!persons.contains(currentMember)){
                    deleteExecutors.add(currentMembersMap.get(currentMember));
                }
            }
            if (deleteExecutors.size() > 0) {
                executorAPI.removeExecutorsFromGroup(user, deleteExecutors, group.getId());
                result = true;
            }

            return result;
        } catch (Exception ex) {
            throw new ProcessException("Error create group", ex);
        }
    }

    @Override
    public boolean createOrUpdateUser(String login, boolean active) {
        try{
            // Подключаемся к серверу
            AuthenticationAPI authenticationAPI = new AuthenticationWebService(getServiceUrl(AuthenticationWebService.class)).getAuthenticationAPIPort();
            User user = authenticationAPI.authenticateByLoginPassword(systemUsername, systemPassword);
            ExecutorAPI executorAPI = new ExecutorWebService(getServiceUrl(ExecutorWebService.class)).getExecutorAPIPort();

            boolean result = false;
            if (executorAPI.isExecutorExist(user, login)){
                // Получение
                WfExecutor executor = executorAPI.getExecutorByName(user, login);

                // Установка активности
                Actor actor = executorAPI.getActorCaseInsensitive(executor.getName());
                if (actor.isActive() != active){
                    executorAPI.setStatus(user, actor, active);
                    result = true;
                }
            }else{
                // Созданеи
                WfExecutor executor = new WfExecutor();
                executor.setName(login);
                executor.setDescription("Created by AF5");
                executor.setExecutorClassName("ru.runa.wfe.user.Actor");
                executor = executorAPI.create(user, executor);

                // Установка пароля
                Actor actor = executorAPI.getActorCaseInsensitive(executor.getName());
                executorAPI.setPassword(user, actor, userPassword);

                result = true;
            }
            return result;

        } catch (Exception ex) {
            throw new ProcessException("Error create user", ex);
        }
    }

    @Override
    public boolean isSupportTemplate(byte[] processDefinition, String processName) {
        return processName.toLowerCase().endsWith("par");
    }

    /**
     * Пееременные приходят со значением в кавычках, избавляемся от них
     * @param value
     * @return
     */
    private String trimVariable(String value){
        String result = value;
        if (value.startsWith("\"") && value.endsWith("\"")){
            result = value.substring(1, value.length() - 1);
        }
        return result;
    }

    private Object getProcessvariable(List<WfVariable> variables, String name){
        Object result = null;
        for (WfVariable variable : variables) {
            if (variable.getDefinition().getName().equalsIgnoreCase(name)){
                result = variable.getValue();
                break;
            }
        }
        return result;
    }

    @Override
    public String getEngeneName() {
        return ENGENE_NAME;
    }
}
