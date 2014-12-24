package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.*;
import ru.intertrust.cm.core.dao.impl.utils.MultipleObjectRowMapper;

import java.util.*;

/**
 * Реализация DAO для работы с расписанием задач
 *
 */
public class SchedulerDaoImpl implements SchedulerDao {

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private EventLogService eventLogService;

    @Autowired
    private DomainObjectTypeIdCache domainObjectTypeIdCache;

    @Autowired
    private SchedulerQueryHelper schedulerQueryHelper;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private NamedParameterJdbcOperations jdbcTemplate;

    @Autowired
    private DomainObjectCacheService domainObjectCacheService;

    public void setAccessControlService(AccessControlService accessControlService) {
        this.accessControlService = accessControlService;
    }


    /**
     * Получение всех задач у которых статус отличен от ScheduleService.SCHEDULE_STATUS_SLEEP
     * @return
     */
    @Override
    public List<DomainObject> getNonSleepTasks() {
        String typeName = "schedule";
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
        return getMultipleTasks(typeName, accessToken,
                schedulerQueryHelper.generateFindNotInStatusTasksQuery(typeName, accessToken));
    }

    /**
     * Получение задач в определенном статусе
     * @param status
     * @return
     */
    @Override
    public List<DomainObject> getTasksByStatus(String status, boolean activeOnly) {
        String typeName = "schedule";
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
        return getMultipleTasks(typeName, accessToken,
                schedulerQueryHelper.generateFindTasksByStatusQuery(typeName, accessToken, activeOnly));

    }

    private List<DomainObject> getMultipleTasks(String typeName, AccessToken accessToken, String query) {
        Map<String, Object> parameters = schedulerQueryHelper.initializeParameters(accessToken);
        parameters.put("status", SCHEDULE_STATUS_SLEEP);

        List<DomainObject> result = jdbcTemplate.query(query, parameters,
                new MultipleObjectRowMapper(typeName, configurationExplorer, domainObjectTypeIdCache));

        domainObjectCacheService.putObjectsToCache(result, accessToken);

        eventLogService.logAccessDomainObjectEventByDo(result, EventLogService.ACCESS_OBJECT_READ, true);

        return result;
    }
}
