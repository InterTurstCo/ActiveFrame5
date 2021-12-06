package ru.intertrust.cm.core.business.impl.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.IdService;
import ru.intertrust.cm.core.business.api.PersonManagementService;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTask;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskHandle;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;
import ru.intertrust.cm.core.business.api.workflow.WorkflowEngine;
import ru.intertrust.cm.core.business.api.workflow.WorkflowTaskData;
import ru.intertrust.cm.core.model.ProcessException;

import javax.ejb.EJBContext;
import javax.ejb.SessionContext;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ScheduleTask(name = "RunaSyncWorkflowTask",
        minute = "*/1",
        taskTransactionalManagement = true)
public class RunaSyncWorkflowTask implements ScheduleTaskHandle{
    private static final Logger logger = LoggerFactory.getLogger(RunaSyncWorkflowTask.class);

    @Autowired
    private WorkflowEngine workflowEngine;

    @Autowired
    private CrudService crudService;

    @Autowired
    private IdService idService;

    @Autowired
    private PersonManagementService personManagementService;

    @Autowired
    private CollectionsService collectionsService;

    @Autowired
    private ProcessService processService;

    @Override
    public String execute(EJBContext ejbContext, SessionContext sessionContext, ScheduleTaskParameters parameters) throws InterruptedException {
        int countCreate = 0;
        int countTerminate = 0;
        try {
            logger.info("Start RunaSyncWorkflowTask");

            Set<String> taskByIds = new HashSet<>();
            List<WorkflowTaskData> tasks = workflowEngine.getEngeneTasks();
            for (WorkflowTaskData task : tasks) {
                if (task.getContext() == null) {
                    logger.warn("Task with id {} ignored. Context ID is null.", task.getTaskId());
                    continue;
                }

                Id platformTaskId = processService.assignTask(task);
                logger.debug("Create platform task {} from wf task {}", platformTaskId.toStringRepresentation(), task.getTaskId());
                taskByIds.add(task.getTaskId());
            }

            // проверить нет ли в базе AF5 процессов, которые завершены принудительно в движке, устанавливаем у них статус Pretermit
            IdentifiableObjectCollection collection = collectionsService.findCollectionByQuery("select t.id, taskid from person_task t\n" +
                    "join status s on s.id = t.status\n" +
                    "where s.name = 'Send'");
            for (IdentifiableObject row : collection) {
                if (!taskByIds.contains(row.getString("taskid"))){
                    crudService.setStatus(row.getId(), ProcessService.TASK_STATE_PRETERMIT);
                    logger.info("Pretermit task {}", row.getId());
                    countTerminate++;
                }
            }

            return "Create: " + countCreate + "; Terminate: " + countTerminate + ".";
        } catch (Exception ex) {
            throw new ProcessException("Error sync task", ex);
        }
    }
}
