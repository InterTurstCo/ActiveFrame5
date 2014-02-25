package ru.intertrust.cm.core.business.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.intertrust.cm.core.business.api.ScheduleService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.schedule.Schedule;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskConfig;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;
import ru.intertrust.cm.core.business.shedule.SheduleTaskLoader;
import ru.intertrust.cm.core.business.shedule.SheduleTaskLoader.SheduleTaskReestrItem;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.StatusDao;
import ru.intertrust.cm.core.model.ScheduleException;

/**
 * Реализация сервиса выполнения периодических заданий
 * @author larin
 * 
 */
@Stateless(name = "ScheduleService")
@Local(ScheduleService.class)
@Remote(ScheduleService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class ScheduleServiceImpl implements ScheduleService {
    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private CollectionsDao collectionsDao;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private SheduleTaskLoader sheduleTaskLoader;

    @Autowired
    private StatusDao statusDao;

    @Override
    public List<DomainObject> getTaskList() {
        List<DomainObject> result = new ArrayList<DomainObject>();
        String query = "select t.id from schedule t";
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
        IdentifiableObjectCollection collection = collectionsDao.findCollectionByQuery(query, 0, 1000, accessToken);
        for (IdentifiableObject identifiableObject : collection) {
            DomainObject task = domainObjectDao.find(identifiableObject.getId(), accessToken);
            result.add(task);
        }
        return result;
    }

    @Override
    public List<String> getTaskClasses() {
        List<SheduleTaskReestrItem> tasksDescriptions = sheduleTaskLoader.getSheduleTaskReestrItems(true);
        List<String> result = new ArrayList<String>();
        for (SheduleTaskReestrItem sheduleTaskReestrItem : tasksDescriptions) {
            result.add(sheduleTaskReestrItem.getScheduleTask().getClass().toString());
        }
        return result;
    }

    @Override
    public Schedule getTaskSchedule(Id taskId) {
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
        DomainObject task = domainObjectDao.find(taskId, accessToken);
        Schedule result = new Schedule();
        result.setDayOfMonth(task.getString(SCHEDULE_DAY_OF_MONTH));
        result.setDayOfWeek(task.getString(SCHEDULE_DAY_OF_WEEK));
        result.setHour(task.getString(SCHEDULE_HOUR));
        result.setMinute(task.getString(SCHEDULE_MINUTE));
        result.setMonth(task.getString(SCHEDULE_MONTH));
        result.setYear(task.getString(SCHEDULE_YEAR));
        return result;
    }

    @Override
    public void setTaskSchedule(Id taskId, Schedule schedule) {
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
        DomainObject task = domainObjectDao.find(taskId, accessToken);
        task.setString(SCHEDULE_DAY_OF_MONTH, schedule.getDayOfMonth());
        task.setString(SCHEDULE_DAY_OF_WEEK, schedule.getDayOfWeek());
        task.setString(SCHEDULE_HOUR, schedule.getHour());
        task.setString(SCHEDULE_MINUTE, schedule.getMinute());
        task.setString(SCHEDULE_MONTH, schedule.getMonth());
        task.setString(SCHEDULE_YEAR, schedule.getYear());
        domainObjectDao.save(task, accessToken);
    }

    @Override
    public ScheduleTaskParameters getTaskParams(Id taskId) {
        ByteArrayInputStream inputStream = null;
        try {
            AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
            DomainObject task = domainObjectDao.find(taskId, accessToken);
            Serializer serializer = new Persister();
            ScheduleTaskParameters result = null;
            if (task.getString(SCHEDULE_PARAMETERS) != null) {
                inputStream =
                        new ByteArrayInputStream(task.getString(SCHEDULE_PARAMETERS).getBytes("utf8"));
                result = ((ScheduleTaskConfig) serializer.read(ScheduleTaskConfig.class, inputStream)).getParameters();
            }
            return result;
        } catch (Exception ex) {
            throw new ScheduleException("Error on get schedule task parameters", ex);
        } finally {
            try {
                if (inputStream != null){
                    inputStream.close();
                }
            } catch (Exception ignoreEx) {
            }
        }
    }

    @Override
    public void setTaskParams(Id taskId, ScheduleTaskParameters parameters) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {

            AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
            DomainObject task = domainObjectDao.find(taskId, accessToken);

            Serializer serializer = new Persister();
            out = new ByteArrayOutputStream();
            
            ScheduleTaskConfig config = new ScheduleTaskConfig();
            config.setParameters(parameters);
            serializer.write(config, out);

            task.setString(SCHEDULE_PARAMETERS, out.toString("utf8"));
            domainObjectDao.save(task, accessToken);

        } catch (Exception ex) {
            throw new ScheduleException(
                    "Error on set schedule task parameters", ex);
        } finally {
            try {
                out.close();
            } catch (Exception ignoreEx) {
            }
        }
    }

    @Override
    public void enableTask(Id taskId) {
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
        DomainObject task = domainObjectDao.find(taskId, accessToken);
        if (!task.getBoolean(SCHEDULE_ACTIVE)) {
            task.setBoolean(SCHEDULE_ACTIVE, true);
            domainObjectDao.save(task, accessToken);
        }
    }

    @Override
    public void disableTask(Id taskId) {
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
        DomainObject task = domainObjectDao.find(taskId, accessToken);
        if (task.getBoolean(SCHEDULE_ACTIVE)) {
            task.setBoolean(SCHEDULE_ACTIVE, false);
            domainObjectDao.save(task, accessToken);
        }
    }

    @Override
    public void run(Id taskId) {
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
        DomainObject task = domainObjectDao.setStatus(taskId, statusDao.getStatusIdByName(SCHEDULE_STATUS_READY), accessToken);
        task.setTimestamp(SCHEDULE_LAST_REDY, new Date());
        domainObjectDao.save(task, accessToken);
    }

    @Override
    public void setPriority(Id taskId, int priority) {
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
        DomainObject task = domainObjectDao.find(taskId, accessToken);
        if (task.getLong(SCHEDULE_PRIORITY) != priority) {
            task.setLong(SCHEDULE_PRIORITY, new Long(priority));
            domainObjectDao.save(task, accessToken);
        }
    }

    @Override
    public void setTimeout(Id taskId, int timeout) {
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
        DomainObject task = domainObjectDao.find(taskId, accessToken);
        if (task.getLong(SCHEDULE_TIMEOUT) != timeout) {
            task.setLong(SCHEDULE_TIMEOUT, new Long(timeout));
            domainObjectDao.save(task, accessToken);
        }
    }

    @Override
    public DomainObject createScheduleTask(String className, String name) {
        return sheduleTaskLoader.createTaskDomainObject(sheduleTaskLoader.getSheduleTaskReestrItem(className), name);
    }
    
    /**
     * Создание нового доменного объекта
     * 
     * @param type
     * @return
     */
    protected DomainObject createDomainObject(String type) {
        GenericDomainObject domainObject = new GenericDomainObject();
        domainObject.setTypeName(type);
        Date currentDate = new Date();
        domainObject.setCreatedDate(currentDate);
        domainObject.setModifiedDate(currentDate);
        return domainObject;
    }    
}
