package ru.intertrust.cm.core.process;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.workflow.TaskListeners;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.StatusDao;

public class PlatformDeleteTaskListener implements TaskListener {
    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private CollectionsDao collectionsDao;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private StatusDao statusDao;

    @Autowired
    private TaskListeners taskListeners;

    @PostConstruct
    public void init(){
        taskListeners.addDeleteTaskListener(TaskListeners.NBR_PROCESS_TARGET_NAMESPACE, this);
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        List<DomainObject> tasks = getNotCompleteTasks(delegateTask.getId());
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
        for (DomainObject domainObjectTask : tasks) {
            domainObjectDao.setStatus(domainObjectTask.getId(),
                    statusDao.getStatusIdByName(ProcessService.TASK_STATE_PRETERMIT), accessToken);
        }
    }

    /**
     * Получение всех не завершенных задач и установка у них статуса прервана
     * @param taskId
     * @return
     */
    private List<DomainObject> getNotCompleteTasks(String taskId) {

        List<Filter> filters = new ArrayList<>();
        Filter filter = new Filter();
        filter.setFilter("byTaskId");
        StringValue sv = new StringValue(taskId);
        filter.addCriterion(0, sv);
        filters.add(filter);

        AccessToken accessToken = accessControlService
                .createSystemAccessToken("ProcessService");

        IdentifiableObjectCollection collection = collectionsDao
                .findCollection("NotCompleteTask", filters, null, 0, 0, accessToken);

        List<DomainObject> result = new ArrayList<DomainObject>();
        for (IdentifiableObject item : collection) {
            DomainObject group = domainObjectDao.find(item.getId(), accessToken);
            result.add(group);
        }
        return result;
    }

}
