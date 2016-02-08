package ru.intertrust.cm.core.business.shedule;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.concurrent.Future;
//import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Resource;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.Local;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.transaction.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.intertrust.cm.core.business.api.ScheduleService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.schedule.ScheduleProcessor;
import ru.intertrust.cm.core.business.api.schedule.ScheduleResult;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskHandle;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskLoader;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.DomainObjectCacheService;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.StatusDao;

@Stateless
@Local(ScheduleProcessor.class)
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class ScheduleProcessorImpl implements ScheduleProcessor {
    private static final Logger logger = LoggerFactory.getLogger(SchedulerBean.class);

    @Autowired
    private ScheduleTaskLoader scheduleTaskLoader;

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private StatusDao statusDao;

    @EJB
    private ScheduleService scheduleService;

    @Resource
    private EJBContext ejbContext;
    
    @Resource 
    SessionContext sessionContext;

    @Autowired
    private DomainObjectCacheService domainObjectCacheService;

    //@Resource
    //private AtomicBoolean cancalFlag;

    /**
     * Метод который непосредственно выполняет задачу
     */
    @Asynchronous
    @Override
    public Future<String> startAsync(Id taskId) {
        String result = null;
        try {

            DomainObject task = null;
            boolean error = false;
            AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
            try {
                //Отделяем транзакцию работы со статусами и транзакцию в которой выполняется задача
                ejbContext.getUserTransaction().begin();
                //Установка статуса
                task = domainObjectDao.setStatus(taskId,
                        statusDao.getStatusIdByName(ScheduleService.SCHEDULE_STATUS_RUN),
                        accessToken);
                task.setTimestamp(ScheduleService.SCHEDULE_LAST_RUN, new Date());
                domainObjectDao.save(task, accessToken);
                ejbContext.getUserTransaction().commit();

                //Запуск транзакции задачи
                ejbContext.getUserTransaction().begin();
                //Получение задачи и запуск ее выполнения
                ScheduleTaskHandle handle =
                        scheduleTaskLoader.getSheduleTaskHandle(task.getString(ScheduleService.SCHEDULE_TASK_CLASS));
                result = handle.execute(ejbContext, sessionContext, scheduleService.getTaskParams(taskId));
            } catch (InterruptedException ex) {
                logger.error("Task " + taskId + " abort by InterruptedException", ex);
                ejbContext.getUserTransaction().rollback();
            } catch (Throwable ex) {
                logger.error("Error on exec task " + taskId, ex);
                ByteArrayOutputStream err = new ByteArrayOutputStream();
                ex.printStackTrace(new PrintStream(err, true));
                result = err.toString("utf8");
                error = true;
                ejbContext.getUserTransaction().rollback();
            }

            //Запуск транзакции если транзакция запущенная перед выполнением задачи абортнулась
            if (ejbContext.getUserTransaction() != null && ejbContext.getUserTransaction().getStatus() == Status.STATUS_NO_TRANSACTION) {
                ejbContext.getUserTransaction().begin();
            }
            //Сброс кэша для доменного объекта задача
            domainObjectCacheService.evict(taskId);

            //Сохранение результата
            //Проверяем был ли прерван процесс по таймауту
            if (sessionContext.wasCancelCalled()) {
                task = domainObjectDao.setStatus(taskId,
                        statusDao.getStatusIdByName(ScheduleService.SCHEDULE_STATUS_SLEEP),
                        accessToken);
                task.setTimestamp(ScheduleService.SCHEDULE_LAST_END, new Date());
                task.setLong(ScheduleService.SCHEDULE_LAST_RESULT, ScheduleResult.Timeout.toLong());
                task.setString(ScheduleService.SCHEDULE_LAST_RESULT_DESCRIPTION,
                        "Schedule task cancal by timeout");
            } else {
                //Если процесс завершился штатным образом без таймаута
                task = domainObjectDao.setStatus(taskId,
                        statusDao.getStatusIdByName(ScheduleService.SCHEDULE_STATUS_SLEEP),
                        accessToken);

                task.setTimestamp(ScheduleService.SCHEDULE_LAST_END, new Date());
                task.setString(ScheduleService.SCHEDULE_LAST_RESULT_DESCRIPTION, result);

                if (error) {
                    task.setLong(ScheduleService.SCHEDULE_LAST_RESULT, ScheduleResult.Error.toLong());
                } else {
                    task.setLong(ScheduleService.SCHEDULE_LAST_RESULT, ScheduleResult.Complete.toLong());
                }
            }
            domainObjectDao.save(task, accessToken);
            ejbContext.getUserTransaction().commit();

        } catch (Exception ex) {
            logger.error("Error on acync start schedule task", ex);
            result += "\n" + ex.toString();
            try {
                if (ejbContext.getUserTransaction().getStatus() == Status.STATUS_ACTIVE) {
                    ejbContext.getUserTransaction().rollback();
                }
            } catch (Exception ignoreEx) {
            }
        }
        return new AsyncResult<String>(result);

    }
}
