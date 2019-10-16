package ru.intertrust.cm.core.business.impl.schedule.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import ru.intertrust.cm.core.business.api.dto.DateTimeValue;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTask;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskHandle;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.model.ScheduleException;

import javax.ejb.EJBContext;
import javax.ejb.SessionContext;
import javax.transaction.*;
import java.util.*;

/**
 * Периодическое задание для удаления старых логов выполнения всех периодических заданий.<br/>
 * Интервал устаревания логов по умолчанию - см. в {@link ru.intertrust.cm.core.business.impl.schedule.execution.CleanOldScheduleExecutionLogsDefaultParameters}
 * <br/>
 * <br/>
 * Created by Myskin Sergey on 27.09.2017.
 */
@ScheduleTask(name = "CleanOldScheduleExecutionLogs", minute = "0", hour = "0", timeout = 60, configClass = CleanOldScheduleExecutionLogsDefaultParameters.class, taskTransactionalManagement = true, active = true)
public class CleanOldScheduleExecutionLogsScheduleTask implements ScheduleTaskHandle {

    private static Logger log = LoggerFactory.getLogger(CleanOldScheduleExecutionLogsScheduleTask.class);

    private static final String SCHEDULE_EXECUTIONS_TO_DELETE_QUERY = "SELECT se.id FROM schedule_execution se WHERE se.created_date < {0} ORDER BY se.id";

    private static final int FILE_DELETE_BATCH_SIZE = 100;

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private CollectionsDao collectionsDao;

    @Override
    public String execute(EJBContext ejbContext, SessionContext sessionContext, ScheduleTaskParameters parameters) throws InterruptedException {
        log.info("Start deleting of all schedule execution logs");

        List<Id> idsToDelete = getScheduleExecutionIdsToDelete(parameters);
        int deletedLogsCount = deleteOldScheduleExecutions(ejbContext, sessionContext, idsToDelete);

        String resultMessage = "Deleting of all schedule execution logs was completed. " +
                deletedLogsCount + " records were deleted.";
        log.info(resultMessage);

        return resultMessage;
    }

    /**
     * Получает список идентификаторов объектов лога выполнения периодических заданий <br/>
     * (см. доменный объект "schedule_execution") для удаления.<br/>
     * Текущее условие - все объекты старше указанного в параметрах периода.<br/>
     * Либо значение периода по умолчанию (см. {@link ru.intertrust.cm.core.business.impl.schedule.execution.CleanOldScheduleExecutionLogsDefaultParameters}), если параметров нет.
     * <br/>
     * <br/>
     *
     * @param parameters параметры задачи (см. {@link ru.intertrust.cm.core.business.impl.schedule.execution.CleanOldScheduleExecutionLogsParameters})
     * @return список {@link ru.intertrust.cm.core.business.api.dto.Id идентификаторов} для удаления
     */
    private List<Id> getScheduleExecutionIdsToDelete(ScheduleTaskParameters parameters) {
        log.info("Getting schedule execution ids for deleting");

        List<Value> params = new LinkedList<>();

        Date expirationPeriodTime = getExpirationPeriodTime(parameters);
        DateTimeValue dateTimeValue = new DateTimeValue(expirationPeriodTime);
        params.add(dateTimeValue);

        IdentifiableObjectCollection scheduleExecutionsToDelete = collectionsDao.findCollectionByQuery(SCHEDULE_EXECUTIONS_TO_DELETE_QUERY, params, 0, 0, getAccessToken());

        List<Id> idsToDelete = new LinkedList<>();
        for (int i = 0; i < scheduleExecutionsToDelete.size(); i++) {
            Id seId = scheduleExecutionsToDelete.getId(i);
            idsToDelete.add(seId);
        }
        return idsToDelete;
    }

    /**
     * Возвращает дату раньше текущей на период, указанный в параметрах таски.<br/>
     * Если какой-то параметр не установлен или его значение отрицательное - он игнорируется.
     *
     * @param parameters параметры задачи (см. {@link ru.intertrust.cm.core.business.impl.schedule.execution.CleanOldScheduleExecutionLogsParameters})
     * @return {@link java.util.Date}
     */
    private Date getExpirationPeriodTime(ScheduleTaskParameters parameters) {
        log.info("Getting expiration period for deleting.");
        CleanOldScheduleExecutionLogsParameters cleanOldLogsParams = (CleanOldScheduleExecutionLogsParameters) parameters;

        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());

        Integer months = cleanOldLogsParams.getMonths();
        if ((months != null) && (months > 0)) {
            calendar.add(Calendar.MONTH, -months);
        }

        Integer weeks = cleanOldLogsParams.getWeeks();
        if ((weeks != null) && (weeks > 0)) {
            calendar.add(Calendar.WEEK_OF_YEAR, -weeks);
        }

        Integer days = cleanOldLogsParams.getDays();
        if ((days != null) && (days > 0)) {
            calendar.add(Calendar.DAY_OF_YEAR, -days);
        }

        Date expirationPeriodTime = calendar.getTime();
        return expirationPeriodTime;
    }

    /**
     * Удаляет объекты лога выполнения периодических заданий с идентификаторами, переданными в параметре метода.<br/>
     * Если задача была отменена - корректно завершает выполнение, удалив сколько успел к этому моменту.<br/>
     * Возвращает количество удаленных записей.
     *
     * @param context     контекст сессии
     * @param ejbContext  объект контекста
     * @param idsToDelete список {@link Id идентификаторов} объектов для удаления
     * @return количество удаленных объектов
     */
    private int deleteOldScheduleExecutions(EJBContext ejbContext, SessionContext context, List<Id> idsToDelete) {
        log.info("Deleting old schedule executions");
        int totalCount = 0;

        try {
            if (!CollectionUtils.isEmpty(idsToDelete)) {
                if (Status.STATUS_ACTIVE != ejbContext.getUserTransaction().getStatus()) {
                    ejbContext.getUserTransaction().begin();
                }

                List<Id> idsToDeleteBatch;
                while (idsToDelete.size() > 0) {

                    if (idsToDelete.size() > FILE_DELETE_BATCH_SIZE) {
                        idsToDeleteBatch = idsToDelete.subList(0, FILE_DELETE_BATCH_SIZE);
                    } else {
                        idsToDeleteBatch = idsToDelete.subList(0, idsToDelete.size());
                    }

                    AccessToken accessToken = getAccessToken();
                    domainObjectDao.delete(idsToDeleteBatch, accessToken);

                    totalCount += idsToDeleteBatch.size();
                    idsToDeleteBatch.clear();

                    ejbContext.getUserTransaction().commit();

                    if (context.wasCancelCalled()) {
                        return totalCount;
                    } else {
                        ejbContext.getUserTransaction().begin();
                    }
                }

                if (Status.STATUS_ACTIVE == ejbContext.getUserTransaction().getStatus()) {
                    ejbContext.getUserTransaction().commit();
                }
            }
        } catch (NotSupportedException | SystemException | HeuristicRollbackException | HeuristicMixedException | RollbackException e) {
            try {
                ejbContext.getUserTransaction().rollback();
                throw new FatalException("Error deleteOldScheduleExecutions", e);
            } catch (SystemException ex) {
                log.warn("Error rollback transaction", ex);
            }
        }
        return totalCount;
    }

    /**
     * Возвращает универсальный маркер доступа для текущего класса.
     *
     * @return объект {@link ru.intertrust.cm.core.dao.access.AccessToken токена доступа}
     */
    private AccessToken getAccessToken() {
        String className = getClass().getName();
        AccessToken systemAccessToken = accessControlService.createSystemAccessToken(className);
        return systemAccessToken;
    }

}
