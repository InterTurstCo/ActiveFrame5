package ru.inttrust.workflow.runawfe.extension;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.restclient.client.PlatformClient;
import ru.intertrust.cm.core.restclient.model.WorkflowTaskAddressee;
import ru.intertrust.cm.core.restclient.model.WorkflowTaskData;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.logic.TaskNotifier;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.TemporaryGroup;
import ru.runa.wfe.user.dao.ExecutorDao;
import ru.runa.wfe.var.VariableProvider;

import java.util.Set;

/**
 * Расширение ловит событие о назначение задачи и создает ее в платформе
 */
public class TaskExtension implements TaskNotifier {
    private static final Log log = LogFactory.getLog(TaskExtension.class);

    @Autowired
    private PlatformClient af5Client;

    @Autowired
    private ExecutorDao executorDao;

    @Override
    public void onTaskAssigned(ProcessDefinition processDefinition, VariableProvider variableProvider, Task task, Executor previousExecutor) {
        try {
            WorkflowTaskData taskInfo = new WorkflowTaskData();

            taskInfo.setTaskId(String.valueOf(task.getId()));
            taskInfo.setProcessId(String.valueOf(processDefinition.getId()));
            taskInfo.setActivityId(task.getNodeId());
            taskInfo.setName(task.getName());
            taskInfo.setDescription(task.getDescription());
            taskInfo.setExecutionId(String.valueOf(variableProvider.getProcessId()));

            // CTX_ID
            String ctx = (String)variableProvider.getValue("CTX_ID");
            if (ctx != null) {
                taskInfo.setContext(ctx);
            }

            // исполнители
            Executor owner = task.getExecutor();
            if (owner instanceof TemporaryGroup){
                Set<Actor> actors = executorDao.getGroupActors((TemporaryGroup)owner);
                for (Actor actor : actors) {
                    taskInfo.addAddresseeItem(new WorkflowTaskAddressee().name(actor.getName()).group(false));
                }
            }else if(owner instanceof Group){
                taskInfo.addAddresseeItem(new WorkflowTaskAddressee().name(owner.getName()).group(true));
            }else{
                taskInfo.addAddresseeItem(new WorkflowTaskAddressee().name(owner.getName()).group(false));
            }

            // ACTIONS
            String actions = (String)variableProvider.getValue(task.getNodeId() + "_ACTIONS");
            if (actions != null){
                taskInfo.setActions(actions);
            }

            af5Client.assignTask(taskInfo);
        }catch (Exception ex){
            // Ошибка не должна прекращать рапботу процесса. В случае ошибки задача позднее синхронизируется периодической задачей
            log.error("Error assign task in af5 platform", ex);
        }
    }
}
