package ru.intertrust.cm.core.business.shedule;

import static ru.intertrust.cm.core.business.api.ScheduleService.SCHEDULE_DAY_OF_MONTH;
import static ru.intertrust.cm.core.business.api.ScheduleService.SCHEDULE_DAY_OF_WEEK;
import static ru.intertrust.cm.core.business.api.ScheduleService.SCHEDULE_HOUR;
import static ru.intertrust.cm.core.business.api.ScheduleService.SCHEDULE_MINUTE;
import static ru.intertrust.cm.core.business.api.ScheduleService.SCHEDULE_MONTH;
import static ru.intertrust.cm.core.business.api.ScheduleService.SCHEDULE_YEAR;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.annotation.security.RunAs;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.ClusterManager;
import ru.intertrust.cm.core.business.api.NotificationService;
import ru.intertrust.cm.core.business.api.ScheduleService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddressee;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddresseeGroup;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationContext;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationPriority;
import ru.intertrust.cm.core.business.api.schedule.ScheduleProcessor;
import ru.intertrust.cm.core.business.api.schedule.ScheduleResult;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskLoader;
import ru.intertrust.cm.core.business.impl.ConfigurationLoader;
import ru.intertrust.cm.core.config.server.ServerStatus;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.PersonManagementServiceDao;
import ru.intertrust.cm.core.dao.api.SchedulerDao;
import ru.intertrust.cm.core.dao.api.StatusDao;
import ru.intertrust.cm.core.model.RemoteSuitableException;
import ru.intertrust.cm.core.tools.DomainObjectAccessor;
import ru.intertrust.cm.core.util.SpringBeanAutowiringInterceptor;

@Singleton(name = "SchedulerBean")
@Interceptors(SpringBeanAutowiringInterceptor.class)
@TransactionManagement(TransactionManagementType.BEAN)
@RunAs("system")
@Startup
public class SchedulerBean {

    public static final String ADMIN_GROUP = "Administrators";
    public static final String DISABLE_TASK_NOTIFICATION_TYPE = "DISABLE_TASK_NOTIFICATION_TYPE";

    private static final Logger logger = LoggerFactory.getLogger(SchedulerBean.class);

    @EJB
    private ScheduleProcessor processor;

    @EJB
    private ScheduleTaskLoader scheduleTaskLoader;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private StatusDao statusDao;

    @EJB
    private NotificationService notificationService;

    @Resource
    private EJBContext ejbContext;

    @Autowired
    private ConfigurationLoader configurationLoader;

    @Autowired
    private PersonManagementServiceDao personManagementService;

    @Autowired
    private SchedulerDao schedulerDao;

    @Autowired
    private ClusterManager clusterManager;


    @org.springframework.beans.factory.annotation.Value("${excluded.task.list:}")
    private String excludedTaskList;
    
    private Set<String> excludedTask;
    
    private List<StartedTask> startedTasks = new ArrayList<StartedTask>();
    
    

    /**
     * Входная функция сервиса периодических заданий. Вызывается контейнером раз
     * в минуту
     */
    @Schedule(dayOfWeek = "*", hour = "*", minute = "*/1", second = "0", year = "*", persistent = false)
    public void backgroundProcessing() {
        try {
            if (configurationLoader.isConfigurationLoaded() && scheduleTaskLoader.isLoaded() && scheduleTaskLoader.isEnable()) {

                //Проверка, является ли нода менеджером периодических заданий
                if (clusterManager.hasRole(ScheduleService.SCHEDULE_MANAGER_ROLE_NAME)) {

                    //Проверка подвисших задач
                    checkDeadTask();

                    if (logger.isDebugEnabled()) {
                        logger.debug("Start schedule task runner");
                    }

                    //Получение всех активных периодических задач
                    IdentifiableObjectCollection tasks = schedulerDao.getActiveTask();

                    //Проверка прохождения фильтра по расписанию
                    for (IdentifiableObject task : tasks) {
                        if (isScheduleComplete(task) && !schedulerDao.isRunningTask(task.getId())) {
                            ejbContext.getUserTransaction().begin();
                            schedulerDao.createTaskExecution(task.getId());
                            ejbContext.getUserTransaction().commit();
                        }
                    }
                }

            } else {
                if (!scheduleTaskLoader.isEnable()) {
                    logger.warn("Can not run scheduler. Service is disabled.");
                } else {
                    if (ServerStatus.isEnable()) {
                        logger.warn("Can not run scheduler. Configuration is not loaded.");
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Error on run shedule task", ex);
            try {
                if (ejbContext.getUserTransaction().getStatus() == Status.STATUS_ACTIVE) {
                    ejbContext.getUserTransaction().rollback();
                }
            } catch (Exception ignoreEx) {
            }
        }
    }

    /**
     * Проверка записей о задачах на тех нодах, которые уже не активны
     * @throws SystemException
     * @throws HeuristicRollbackException
     * @throws HeuristicMixedException
     * @throws RollbackException
     * @throws IllegalStateException
     * @throws SecurityException
     * @throws NotSupportedException
     */
    private void checkDeadTask() throws SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException,
            SystemException, NotSupportedException {
        ejbContext.getUserTransaction().begin();

        IdentifiableObjectCollection deadTasks = schedulerDao.getDeadScheduleExecution();
        for (IdentifiableObject identifiableObject : deadTasks) {
            DomainObject deadTask = domainObjectDao.find(identifiableObject.getId(), getAccessToken());
            deadTask.setLong(ScheduleService.SCHEDULE_RESULT, ScheduleResult.Emergency.toLong());
            deadTask.setString(ScheduleService.SCHEDULE_RESULT_DESCRIPTION, "Emergency shutdown server");
            domainObjectDao.save(deadTask, getAccessToken());

            domainObjectDao.setStatus(deadTask.getId(),
                    statusDao.getStatusIdByName(ScheduleService.SCHEDULE_STATUS_COMPLETE),
                    getAccessToken());
        }

        ejbContext.getUserTransaction().commit();
    }

    /**
     * Непосредственно запук астнхронных исполнителей, выполняется со сдвигом 5
     * сек относительно старта задач
     */
    @Schedule(dayOfWeek = "*", hour = "*", minute = "*/1", second = "5", year = "*", persistent = false)
    public void startTasks() {
        executeTasks();
    }

    /**
     * Входная функция проверки превышения времени работы периодического
     * задания. Вызывается раз в минуту со сдвигом 10 секунд относительно старта
     * задач.
     * 
     */
    @Schedule(dayOfWeek = "*", hour = "*", minute = "*/1", second = "10", year = "*", persistent = false)
    public void checkTimeoutBackgroundProcessing() {
        checkTimeout();
    }

    private void checkTimeout() {
        try {
            AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
            ejbContext.getUserTransaction().begin();
            //Перебираем задачи в обратном порядке, чтоб можно было "на лету" удалять из списка
            for (int i = startedTasks.size() - 1; i >= 0; i--) {
                StartedTask startedTask = startedTasks.get(i);

                //Проверка завершена ли задача
                if (startedTask.future.isDone()) {
                    //Удаляем из наблюдаемых задач
                    startedTasks.remove(i);
                    //Проверяем в правильном ли состоянии у нас задача. Статус должен быть Complete, если не так то устанавливаем его с WARNING в лог
                    DomainObject taskDomainObject = domainObjectDao.find(startedTask.taskId, getAccessToken());
                    if (!taskDomainObject.getStatus().equals(statusDao.getStatusIdByName(ScheduleService.SCHEDULE_STATUS_COMPLETE))){
                        domainObjectDao.setStatus(
                                taskDomainObject.getId(), 
                                statusDao.getStatusIdByName(ScheduleService.SCHEDULE_STATUS_COMPLETE), 
                                getAccessToken());
                        logger.warn("Schedule task " + taskDomainObject.getId()
                                + " is not complete in async processor. Complete it in SceduleBean");
                    }
                } else if (startedTask.isCancaled) {
                    //Задача была прервана, но тем не менее она не завершена до сих пор, значит флаг isInterrupted не учитывался при построение класса задачи
                    //Данную задачу делаем не активной, помечаем как плохая и посылаем уведомление администратору.
                    DomainObject task = domainObjectDao.find(startedTask.taskId, accessToken);
                    task.setBoolean(ScheduleService.SCHEDULE_ACTIVE, false);
                    task.setBoolean(ScheduleService.SCHEDULE_BAD_TASK, true);
                    domainObjectDao.save(task, accessToken);

                    //Отправка уведомления администратору (Нужен сервис отправки уведомлений + нужна группа администраторов)
                    List<NotificationAddressee> addresseeList = new ArrayList<>();
                    addresseeList.add(new NotificationAddresseeGroup(personManagementService.getGroupId(ADMIN_GROUP)));
                    //Прикрепляем задачу
                    NotificationContext context = new NotificationContext();
                    context.addContextObject("task", new DomainObjectAccessor(task));
                    //Отправляем уведомление
                    notificationService.sendOnTransactionSuccess(DISABLE_TASK_NOTIFICATION_TYPE,
                            (Id) null,
                            addresseeList,
                            NotificationPriority.HIGH,
                            context);
                } else {
                    //Проверка времени работы
                    if ((startedTask.startTime + startedTask.timeout * 60000) < System.currentTimeMillis()) {
                        //прерываем исполнение, в потоке взводится флаг Thread.isInterrupted() который должны проверять разработчики классов задач 
                        startedTask.future.cancel(true);
                        startedTask.isCancaled = true;

                        DomainObject task = domainObjectDao.find(startedTask.taskId, accessToken);

                        if (logger.isDebugEnabled()) {
                            logger.debug("Task " + task.getString("name") + " is cancaled by timeout of "
                                    + startedTask.timeout);
                        }
                    }
                }
            }
            ejbContext.getUserTransaction().commit();
        } catch (Exception ex) {
            logger.error("Error on check Timeout of schedule task", ex);
            try {
                if (ejbContext.getUserTransaction().getStatus() == Status.STATUS_ACTIVE) {
                    ejbContext.getUserTransaction().rollback();
                }
            } catch (Exception ignoreEx) {
            }
        }
    }

    private void executeTasks() {
        try {
            //Проверка, является ли нода исполнителем периодических заданий
            if (clusterManager.hasRole(ScheduleService.SCHEDULE_EXECUTOR_ROLE_NAME)) {
                AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
                //Получение всех задач в статусе Ready и предназначенных для выполнения на текущей ноде
                IdentifiableObjectCollection taskExecutions = schedulerDao.getReadyScheduleExecution(clusterManager.getNodeId());

                //Запуск задач путем асинхронного вызова ScheduleProcessor
                for (IdentifiableObject taskExecution : taskExecutions) {
                    //Проверка на исключения. Запрет выполняться определенным задачам на данном узле кластера
                    //Нужно чтоб особо тяжелые и часто выполняющиеся задачи не запускались на узле где работают пользователи

                    //Проверка на то что задачу можно запускать на данной ноде
                    if (isExcludedTask(taskExecution.getString("name"))){
                        //Если нельзя то сразу меняем статус на Complete
                        ejbContext.getUserTransaction().begin();
                        DomainObject savedTask = domainObjectDao.setStatus(taskExecution.getId(),
                                statusDao.getStatusIdByName(ScheduleService.SCHEDULE_STATUS_COMPLETE), accessToken);
                        savedTask.setTimestamp(ScheduleService.SCHEDULE_COMPLETE, new Date());
                        savedTask.setString(ScheduleService.SCHEDULE_RESULT_DESCRIPTION, "Schedule task " + taskExecution.getString("name") + " is excluded on this node.");

                        domainObjectDao.save(savedTask, accessToken);
                        ejbContext.getUserTransaction().commit();

                        logger.warn("Schedule task " + taskExecution.getString("name") + " is excluded on this node.");
                        continue;
                    }
                    
                    //Установка статуса
                    ejbContext.getUserTransaction().begin();
                    DomainObject savedTask = domainObjectDao.setStatus(taskExecution.getId(),
                            statusDao.getStatusIdByName(ScheduleService.SCHEDULE_STATUS_WAIT), accessToken);
                    savedTask.setTimestamp(ScheduleService.SCHEDULE_WAIT, new Date());
                    domainObjectDao.save(savedTask, accessToken);
                    ejbContext.getUserTransaction().commit();

                    //Запуск процесса задачи
                    Future<String> result = processor.startAsync(taskExecution.getId());
                    //Сохраняем объект future для возможности прервать процесс
                    StartedTask startedTask = new StartedTask();
                    startedTask.future = result;
                    startedTask.startTime = System.currentTimeMillis();
                    DomainObject task = domainObjectDao.find(taskExecution.getReference("schedule"), accessToken);
                    startedTask.timeout = task.getLong(ScheduleService.SCHEDULE_TIMEOUT);
                    startedTask.taskId = task.getId();
                    startedTasks.add(startedTask);
                }
            }
        } catch (Exception ex) {
            logger.error("Error on run shedule task", ex);
            try {
                if (ejbContext.getUserTransaction().getStatus() == Status.STATUS_ACTIVE) {
                    ejbContext.getUserTransaction().rollback();
                }
            } catch (Exception ignoreEx) {
            }
        }
    }

    private boolean isExcludedTask(String taskName){
        if (excludedTask == null){
            excludedTask = new HashSet<String>();
            if (excludedTaskList != null && !excludedTaskList.isEmpty()){
                String[] excludedTaskArray = excludedTaskList.split("[,; ]");
                for (String excludedTaskItem : excludedTaskArray) {
                    excludedTask.add(excludedTaskItem);
                }
            }
        }
        
        return excludedTask.contains(taskName);
    }
    
    /**
     * Проверка расписания на соответствие текущему времени
     * @param task
     * @return
     */
    private boolean isScheduleComplete(IdentifiableObject task) {
        boolean result = false;

        // Получаем рассписание в базе
        ru.intertrust.cm.core.business.api.schedule.Schedule schedule = getScheduleFromTask(task);

        //Получаем текущие значения даты и времени
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        //Проверка каждого элемента. Если хоть один элемент не проходит проверку результат false
        result = isScheduleFieldComplete(schedule.getYear(), year);
        result = result && isScheduleFieldComplete(schedule.getMonth(), month);
        result = result && isScheduleFieldComplete(schedule.getDayOfWeek(), dayOfWeek);
        result = result && isScheduleFieldComplete(schedule.getDayOfMonth(), dayOfMonth);
        result = result && isScheduleFieldComplete(schedule.getHour(), hour);
        result = result && isScheduleFieldComplete(schedule.getMinute(), minute);

        return result;
    }

    private ru.intertrust.cm.core.business.api.schedule.Schedule getScheduleFromTask(IdentifiableObject task) {
        try {
            ru.intertrust.cm.core.business.api.schedule.Schedule result = new ru.intertrust.cm.core.business.api.schedule.Schedule();
            result.setDayOfMonth(task.getString(SCHEDULE_DAY_OF_MONTH));
            result.setDayOfWeek(task.getString(SCHEDULE_DAY_OF_WEEK));
            result.setHour(task.getString(SCHEDULE_HOUR));
            result.setMinute(task.getString(SCHEDULE_MINUTE));
            result.setMonth(task.getString(SCHEDULE_MONTH));
            result.setYear(task.getString(SCHEDULE_YEAR));
            return result;
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    /**
     * Проверка одного поля расписания
     * @param year
     * @param year2
     * @return
     */
    private boolean isScheduleFieldComplete(String field, int now) {
        boolean result = false;

        if (field.equals("*")) {
            result = true;
        } else if (field.matches("\\d+")) {
            result = Integer.parseInt(field) == now;
        } else if (field.matches("\\*/\\d+")) {
            result = now % Integer.parseInt(field.substring(2)) == 0;
        }

        return result;
    }

    /**
     * Завершение задач при выключение сервера
     */
    @PreDestroy
    public void shutdown() {
        logger.debug("Cancal all tasks by shutdown event");
        for (int i = startedTasks.size() - 1; i >= 0; i--) {
            StartedTask startedTask = startedTasks.get(i);
            //Проверка завершена ли задача
            if (!startedTask.future.isDone()) {
                //Прерываем задачу
                startedTask.future.cancel(true);
            }
        }
    }

    @PostConstruct
    public void init() {
        clusterManager.regRole(ScheduleService.SCHEDULE_MANAGER_ROLE_NAME, true);
        clusterManager.regRole(ScheduleService.SCHEDULE_EXECUTOR_ROLE_NAME, false);
    }

    private class StartedTask {
        private long startTime;
        private Future<String> future;
        private long timeout;
        private Id taskId;
        private boolean isCancaled;
    }

    private AccessToken getAccessToken() {
        return accessControlService.createSystemAccessToken(this.getClass().getName());
    }

}
