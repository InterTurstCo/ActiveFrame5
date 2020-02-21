package ru.intertrust.cm.core.business.impl.workflow;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTask;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskHandle;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;
import ru.intertrust.cm.core.business.api.workflow.WorkflowEngine;

import javax.ejb.EJBContext;
import javax.ejb.SessionContext;

@ScheduleTask(name = "RunaSyncExecutors",
        dayOfMonth = "*/1",
        hour = "0",
        minute = "0",
        taskTransactionalManagement = true)
public class RunaSyncExecutors implements ScheduleTaskHandle {

    @Autowired
    private WorkflowEngine workflowEngine;

    @Autowired
    private CollectionsService collectionsService;

    @Override
    public String execute(EJBContext ejbContext, SessionContext sessionContext, ScheduleTaskParameters parameters) throws InterruptedException {
        long personsCount = syncPersons();
        long groupsCount = syncGroups();
        return "Sync: " + personsCount + " persons; " + groupsCount + " groups.";
    }

    private long syncGroups() {
        long count = 0;
        IdentifiableObjectCollection collection =
                collectionsService.findCollectionByQuery("select id, group_name from user_group where object_id is null");
        for (IdentifiableObject row : collection) {
            if (workflowEngine.createGroup(row.getString("group_name"))){
                count++;
            }
        }
        return count;
    }

    private long syncPersons() {
        long count = 0;
        IdentifiableObjectCollection collection =
                collectionsService.findCollectionByQuery("select id, login from person");
        for (IdentifiableObject row : collection) {
            if (workflowEngine.createOrUpdateUser(row.getString("login"), true)){
                count++;
            }
        }
        return count;
    }
}
