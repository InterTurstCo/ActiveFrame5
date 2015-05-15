package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.DomainObjectCacheService;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.api.EventLogService;
import ru.intertrust.cm.core.dao.api.SchedulerDao;
import ru.intertrust.cm.core.dao.impl.utils.MultipleObjectRowMapper;

import java.util.List;
import java.util.Map;

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
    @Qualifier("switchableNamedParameterJdbcTemplate")
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
                schedulerQueryHelper.generateFindNotInStatusTasksQuery(typeName, accessToken), SCHEDULE_STATUS_SLEEP);
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
                schedulerQueryHelper.generateFindTasksByStatusQuery(typeName, accessToken, activeOnly), status);

    }

    private List<DomainObject> getMultipleTasks(String typeName, AccessToken accessToken, String query, String statusName) {
        Map<String, Object> parameters = schedulerQueryHelper.initializeParameters(accessToken);
        parameters.put("status", statusName);

        List<DomainObject> result = jdbcTemplate.query(query, parameters,
                new MultipleObjectRowMapper(typeName, configurationExplorer, domainObjectTypeIdCache));

        domainObjectCacheService.putObjectsToCache(result, accessToken);

        eventLogService.logAccessDomainObjectEventByDo(result, EventLogService.ACCESS_OBJECT_READ, true);

        return result;
    }
}
