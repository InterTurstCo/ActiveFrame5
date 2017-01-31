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
    public Future<String> startAsync(Id taskExecutionId) {
        String result = null;
        try {

            DomainObject taskExecution = null;
            boolean error = false;
            AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
            String taskName = null;
            DomainObject task = null;
            boolean inTransactionTask = false;
            try {
                //Отделяем транзакцию работы со статусами и транзакцию в которой выполняется задача
                ejbContext.getUserTransaction().begin();
                //Установка статуса
                taskExecution = domainObjectDao.setStatus(
                        taskExecutionId, statusDao.getStatusIdByName(ScheduleService.SCHEDULE_STATUS_RUN), accessToken);
                taskExecution.setTimestamp(ScheduleService.SCHEDULE_RUN, new Date());
                taskExecution = domainObjectDao.save(taskExecution, accessToken);

                //Получение задачи
                task = domainObjectDao.find(
                        taskExecution.getReference(ScheduleService.SCHEDULE_EXECUTION_SCHEDULE), accessToken);
                taskName = task.getString(ScheduleService.SCHEDULE_NAME);
                String taskClass = task.getString(ScheduleService.SCHEDULE_TASK_CLASS);
                ejbContext.getUserTransaction().commit();

                //Запуск транзакции задачи
                inTransactionTask = scheduleTaskLoader.taskNeedsTransaction(taskClass);
                if (inTransactionTask) {
                    ejbContext.getUserTransaction().begin();
                }

                //Получение класса исполнителя задачи и запуск ее выполнения
                ScheduleTaskHandle handle = scheduleTaskLoader.getSheduleTaskHandle(taskClass);
                result = handle.execute(ejbContext, sessionContext, scheduleService.getTaskParams(task.getId()));

                if (inTransactionTask) {
                    if (sessionContext.wasCancelCalled()) {
                        ejbContext.getUserTransaction().rollback();
                    } else {
                        ejbContext.getUserTransaction().commit();
                    }
                }
            } catch (InterruptedException ex) {
                logger.error("Task " + taskName + " abort by InterruptedException", ex);
                if (inTransactionTask) {
                    ejbContext.getUserTransaction().rollback();
                }
            } catch (Throwable ex) {
                logger.error("Error on exec task " + taskName, ex);
                ByteArrayOutputStream err = new ByteArrayOutputStream();
                ex.printStackTrace(new PrintStream(err, true));
                result = err.toString("utf8");
                error = true;
                if (inTransactionTask) {
                    ejbContext.getUserTransaction().rollback();
                }
            }

            ejbContext.getUserTransaction().begin();
            //Сброс кэша для доменного объекта задача
            domainObjectCacheService.evict(taskExecutionId);

            //Сохранение результата
            //Проверяем был ли прерван процесс по таймауту
            if (sessionContext.wasCancelCalled()) {
                taskExecution = domainObjectDao.setStatus(taskExecutionId,
                        statusDao.getStatusIdByName(ScheduleService.SCHEDULE_STATUS_COMPLETE),
                        accessToken);
                taskExecution.setTimestamp(ScheduleService.SCHEDULE_COMPLETE, new Date());
                taskExecution.setLong(ScheduleService.SCHEDULE_RESULT, ScheduleResult.Timeout.toLong());
                taskExecution.setString(ScheduleService.SCHEDULE_RESULT_DESCRIPTION,
                        "Schedule task cancal by timeout");
            } else {
                //Если процесс завершился штатным образом без таймаута
                taskExecution = domainObjectDao.setStatus(taskExecutionId,
                        statusDao.getStatusIdByName(ScheduleService.SCHEDULE_STATUS_COMPLETE),
                        accessToken);

                taskExecution.setTimestamp(ScheduleService.SCHEDULE_COMPLETE, new Date());
                taskExecution.setString(ScheduleService.SCHEDULE_RESULT_DESCRIPTION, result);

                if (error) {
                    taskExecution.setLong(ScheduleService.SCHEDULE_RESULT, ScheduleResult.Error.toLong());
                } else {
                    taskExecution.setLong(ScheduleService.SCHEDULE_RESULT, ScheduleResult.Complete.toLong());
                }
            }
            domainObjectDao.save(taskExecution, accessToken);
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
/*
    private boolean inTransactionTask(DomainObject task){
        return task.getBoolean(ScheduleService.SCHEDULE_TASK_TRANSACTIONAL_MANAGEMENT) == null 
                || !task.getBoolean(ScheduleService.SCHEDULE_TASK_TRANSACTIONAL_MANAGEMENT);
    }
*/
}
