package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.SchedulerDao;
import ru.intertrust.cm.core.dao.impl.utils.IdentifiableObjectConverter;

import java.util.Collections;
import java.util.List;

/**
 * Реализация DAO для работы с расписанием задач
 *
 */
public class SchedulerDaoImpl implements SchedulerDao {

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private CollectionsDao collectionsDao;

    @Autowired
    private IdentifiableObjectConverter identifiableObjectConverter;

    public void setAccessControlService(AccessControlService accessControlService) {
        this.accessControlService = accessControlService;
    }


    /**
     * Получение всех задач у которых статус отличен от ScheduleService.SCHEDULE_STATUS_SLEEP
     * @return
     */
    @Override
    public List<DomainObject> getNonSleepTasks() {
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());

        Filter filter = new Filter();
        filter.setFilter("notInStatus");
        filter.addCriterion(0, new StringValue(SCHEDULE_STATUS_SLEEP));

        return identifiableObjectConverter.convertToDomainObjectList(
                collectionsDao.findCollection("ScheduleTasks", Collections.singletonList(filter), null, 0, 0, accessToken));
    }

    /**
     * Получение задач в определенном статусе
     *
     * @param status
     * @return
     */
    @Override
    public List<DomainObject> getTasksByStatus(String status, boolean activeOnly) {
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());

        Filter filter = new Filter();
        if (activeOnly) {
            filter.setFilter("activeByStatus");
        } else {
            filter.setFilter("byStatus");
        }
        filter.addCriterion(0, new StringValue(status));

        return identifiableObjectConverter.convertToDomainObjectList(
                collectionsDao.findCollection("ScheduleTasks", Collections.singletonList(filter), null, 0, 0, accessToken));
    }
}
