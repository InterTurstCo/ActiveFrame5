package ru.intertrust.cm.core.business.shedule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.intertrust.cm.core.business.api.ScheduleService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.schedule.ScheduleProcessor;
import ru.intertrust.cm.core.business.api.schedule.ScheduleResult;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.StatusDao;

@Stateless(name = "SchedulerBean")
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class SchedulerBean {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerBean.class);

    @EJB
    private ScheduleProcessor processor;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private CollectionsDao collectionsDao;
    
    @Autowired
    private StatusDao statusDao;

    @EJB
    private ScheduleService scheduleService;
    
    private List<StartedTask> startedTasks = new ArrayList<StartedTask>(); 
    /**
     * Входная функция сервиса периодических заданий. Вызывается контейнером раз в минуту
     */
    @Schedule(dayOfWeek = "*", hour = "*", minute = "*/1", second = "0", year = "*", persistent = false)
    public void backgroundProcessing()
    {
        try {
            if (logger.isDebugEnabled()){
                logger.debug("Start schedule task runner");
            }
            AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
            //Проверка превышения таймаута задачами
            checkTimeout();
            
            //Получение всех периодических заданий находящихся в статусе SLEEP
            List<DomainObject> tasks = getTasksByStatus(ScheduleService.SCHEDULE_STATUS_SLEEP, true);

            //Проверка прохождения фильтра по расписанию
            for (DomainObject task : tasks) {
                if (isScheduleComplete(task)){
                    //Устанавливаем статус ready
                    DomainObject savedTask = domainObjectDao.setStatus(task.getId(), statusDao.getStatusIdByName(ScheduleService.SCHEDULE_STATUS_READY), accessToken);
                    savedTask.setTimestamp(ScheduleService.SCHEDULE_LAST_REDY, new Date());
                    savedTask.setTimestamp(ScheduleService.SCHEDULE_LAST_WAIT, null);
                    savedTask.setTimestamp(ScheduleService.SCHEDULE_LAST_RUN, null);
                    savedTask.setTimestamp(ScheduleService.SCHEDULE_LAST_END, null);
                    domainObjectDao.save(savedTask, accessToken);                    
                }
            }

            executeTasks();
        } catch (Exception ex) {
            logger.error("Error on run shedule task", ex);
        }
    }

    private void checkTimeout() {
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
        //Перебираем задачи в обратном прядке, чтоб можно было "на лету" удалять из списка
        for (int i = startedTasks.size() - 1; i >= 0; i--) {
            StartedTask startedTask = startedTasks.get(i);
            //Проверка времени работы
            if (!startedTask.future.isDone() && (startedTask.startTime + startedTask.timeout * 60000) < System.currentTimeMillis()){
                //прерываем исполнение
                startedTask.future.cancel(false);
                //устанавливаем статус
                DomainObject savedTask = domainObjectDao.setStatus(startedTask.taskId, statusDao.getStatusIdByName(ScheduleService.SCHEDULE_STATUS_SLEEP), accessToken);
                savedTask.setTimestamp(ScheduleService.SCHEDULE_LAST_END, new Date());
                savedTask.setLong(ScheduleService.SCHEDULE_LAST_RESULT, ScheduleResult.Timeout.toLong());
                savedTask.setString(ScheduleService.SCHEDULE_LAST_RESULT_DESCRIPTION, "Schedule task cancal by timeout");
                domainObjectDao.save(savedTask, accessToken);                                    
            }
        }        
    }

    private void executeTasks() {
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
        //Получение всех задач в статусе Ready
        List<DomainObject> tasks = getTasksByStatus(ScheduleService.SCHEDULE_STATUS_READY, false);

        //Запуск задач путем асинхронного вызова ScheduleProcessor
        for (DomainObject task : tasks) {
            //Установка статуса
            DomainObject savedTask = domainObjectDao.setStatus(task.getId(), statusDao.getStatusIdByName(ScheduleService.SCHEDULE_STATUS_WAIT), accessToken);
            savedTask.setTimestamp(ScheduleService.SCHEDULE_LAST_WAIT, new Date());
            domainObjectDao.save(savedTask, accessToken);                                
            
            //Запуск процесса задачи
            Future<String> result = processor.startAsync(task.getId());
            //Сохраняем объект future для возможности прервать процесс
            StartedTask startedTask = new StartedTask();
            startedTask.future = result;
            startedTask.startTime = System.currentTimeMillis();
            startedTask.timeout = task.getLong(ScheduleService.SCHEDULE_TIMEOUT);
            startedTask.taskId = task.getId();
            startedTasks.add(startedTask);
        }
    }

    /**
     * Проверка расписания на соответствие текущему времени
     * @param task
     * @return
     */
    private boolean isScheduleComplete(DomainObject task) {
        boolean result = false;
        
        //Получаем рассписание в базе
        ru.intertrust.cm.core.business.api.schedule.Schedule schedule = scheduleService.getTaskSchedule(task.getId());
        
        //Получаем текущие значения даты и времени
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR);
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

    /**
     * Проверка одного поля расписания
     * @param year
     * @param year2
     * @return
     */
    private boolean isScheduleFieldComplete(String field, int now) {
        boolean result = false;
        
        if (field.equals("*")){
            result = true;
        }else if(field.matches("\\d+")){
            result = Integer.parseInt(field) == now;
        }else if(field.matches("\\*/\\d+")){
            result = now % Integer.parseInt(field.substring(2)) == 0;
        }
        
        return result;
    }

    /**
     * Получение задач в определенном статусе
     * @param status
     * @return
     */
    private List<DomainObject> getTasksByStatus(String status, boolean activeOnly) {
        List<DomainObject> result = new ArrayList<DomainObject>();
        String query = "select t.id from schedule t inner join status s on t.status = s.id where s.name = '" + status + "' ";
        if (activeOnly){
            query += "and active = 1 ";
        }
        query += "order by t.priority";
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
        IdentifiableObjectCollection collection = collectionsDao.findCollectionByQuery(query, 0, 1000, accessToken);
        for (IdentifiableObject identifiableObject : collection) {
            DomainObject task = domainObjectDao.find(identifiableObject.getId(), accessToken);
            result.add(task);
        }
        return result;
    }
    
    class StartedTask{
        private long startTime;
        private Future<String> future;
        private long timeout;
        private Id taskId;
    }

}
