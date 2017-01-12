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
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;
import org.slf4j.LoggerFactory;
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
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskLoader;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;
import ru.intertrust.cm.core.business.api.schedule.SheduleTaskReestrItem;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.SchedulerDao;
import ru.intertrust.cm.core.dao.api.StatusDao;
import ru.intertrust.cm.core.model.ScheduleException;
import ru.intertrust.cm.core.model.SystemException;
import ru.intertrust.cm.core.model.UnexpectedException;

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

    final static org.slf4j.Logger logger = LoggerFactory.getLogger(ScheduleServiceImpl.class);

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private CollectionsDao collectionsDao;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private ScheduleTaskLoader scheduleTaskLoader;

    @Autowired
    private StatusDao statusDao;

    @Autowired
    private SchedulerDao schedulerDao;

    @Override
    public List<DomainObject> getTaskList() {
        try {
            List<DomainObject> result = new ArrayList<DomainObject>();
            String query = "select t.id from schedule t";
            AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
            IdentifiableObjectCollection collection = collectionsDao.findCollectionByQuery(query, 0, 0, accessToken);
            for (IdentifiableObject identifiableObject : collection) {
                DomainObject task = domainObjectDao.find(identifiableObject.getId(), accessToken);
                result.add(task);
            }
            return result;
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in getTaskList", ex);
            throw new UnexpectedException("ScheduleService", "getTaskList", "", ex);
        }
    }

    @Override
    public List<String> getTaskClasses() {
        try {
            List<SheduleTaskReestrItem> tasksDescriptions = scheduleTaskLoader.getSheduleTaskReestrItems(true);
            List<String> result = new ArrayList<String>();
            for (SheduleTaskReestrItem sheduleTaskReestrItem : tasksDescriptions) {
                result.add(sheduleTaskReestrItem.getScheduleTask().getClass().toString());
            }
            return result;
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in getTaskClasses", ex);
            throw new UnexpectedException("ScheduleService", "getTaskClasses", "", ex);
        }
    }

    @Override
    public Schedule getTaskSchedule(Id taskId) {
        try {
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
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in getTaskSchedule", ex);
            throw new UnexpectedException("ScheduleService", "getTaskSchedule", "taskId:" + taskId, ex);
        }
    }

    @Override
    public void setTaskSchedule(Id taskId, Schedule schedule) {
        try {
            AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
            DomainObject task = domainObjectDao.find(taskId, accessToken);
            task.setString(SCHEDULE_DAY_OF_MONTH, schedule.getDayOfMonth());
            task.setString(SCHEDULE_DAY_OF_WEEK, schedule.getDayOfWeek());
            task.setString(SCHEDULE_HOUR, schedule.getHour());
            task.setString(SCHEDULE_MINUTE, schedule.getMinute());
            task.setString(SCHEDULE_MONTH, schedule.getMonth());
            task.setString(SCHEDULE_YEAR, schedule.getYear());
            domainObjectDao.save(task, accessToken);
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in setTaskSchedule", ex);
            throw new UnexpectedException("ScheduleService", "setTaskSchedule",
                    "taskId:" + taskId + " schedule:" + schedule, ex);
        }
    }

    @Override
    public ScheduleTaskParameters getTaskParams(Id taskId) {
        ByteArrayInputStream inputStream = null;
        try {
            AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
            DomainObject task = domainObjectDao.find(taskId, accessToken);
            Strategy strategy = new AnnotationStrategy();
            Serializer serializer = new Persister(strategy);
            ScheduleTaskParameters result = null;
            if (task.getString(SCHEDULE_PARAMETERS) != null) {
                inputStream =
                        new ByteArrayInputStream(task.getString(SCHEDULE_PARAMETERS).getBytes("utf8"));
                result = ((ScheduleTaskConfig) serializer.read(ScheduleTaskConfig.class, inputStream)).getParameters();
            }
            return result;
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ScheduleException("Error on get schedule task parameters", ex);
        } finally {
            try {
                if (inputStream != null) {
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

            Strategy strategy = new AnnotationStrategy();
            Serializer serializer = new Persister(strategy);
            out = new ByteArrayOutputStream();

            ScheduleTaskConfig config = new ScheduleTaskConfig();
            config.setParameters(parameters);
            serializer.write(config, out);

            task.setString(SCHEDULE_PARAMETERS, out.toString("utf8"));
            domainObjectDao.save(task, accessToken);
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ScheduleException("Error on set schedule task parameters", ex);
        } finally {
            try {
                out.close();
            } catch (Exception ignoreEx) {
            }
        }
    }

    @Override
    public void enableTask(Id taskId) {
        try {
            AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
            DomainObject task = domainObjectDao.find(taskId, accessToken);
            if (!task.getBoolean(SCHEDULE_ACTIVE)) {
                task.setBoolean(SCHEDULE_ACTIVE, true);
                domainObjectDao.save(task, accessToken);
            }
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in enableTask", ex);
            throw new UnexpectedException("ScheduleService", "enableTask", "taskId:" + taskId, ex);
        }
    }

    @Override
    public void disableTask(Id taskId) {
        try {
            AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
            DomainObject task = domainObjectDao.find(taskId, accessToken);
            if (task.getBoolean(SCHEDULE_ACTIVE)) {
                task.setBoolean(SCHEDULE_ACTIVE, false);
                domainObjectDao.save(task, accessToken);
            }
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in disableTask", ex);
            throw new UnexpectedException("ScheduleService", "disableTask", "taskId:" + taskId, ex);
        }
    }

    @Override
    public void run(Id taskId) {
        try {
            schedulerDao.createTaskExecution(taskId);
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in run", ex);
            throw new UnexpectedException("ScheduleService", "run", "taskId:" + taskId, ex);
        }
    }

    @Override
    public void setPriority(Id taskId, int priority) {
        try {
            AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
            DomainObject task = domainObjectDao.find(taskId, accessToken);
            if (task.getLong(SCHEDULE_PRIORITY) != priority) {
                task.setLong(SCHEDULE_PRIORITY, new Long(priority));
                domainObjectDao.save(task, accessToken);
            }
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in setPriority", ex);
            throw new UnexpectedException("ScheduleService", "setPriority",
                    "taskId:" + taskId + " priority: " + priority, ex);
        }
    }

    @Override
    public void setTimeout(Id taskId, int timeout) {
        try {
            AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
            DomainObject task = domainObjectDao.find(taskId, accessToken);
            if (task.getLong(SCHEDULE_TIMEOUT) != timeout) {
                task.setLong(SCHEDULE_TIMEOUT, new Long(timeout));
                domainObjectDao.save(task, accessToken);
            }
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in setTimeout", ex);
            throw new UnexpectedException("ScheduleService", "setTimeout",
                    "taskId:" + taskId + " timeout:" + timeout, ex);
        }
    }

    @Override
    public DomainObject createScheduleTask(String className, String name) {
        try {
            return scheduleTaskLoader.createTaskDomainObject(scheduleTaskLoader.getSheduleTaskReestrItem(className), name);
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in createScheduleTask", ex);
            throw new UnexpectedException("ScheduleService", "createScheduleTask",
                    "className:" + className + " name:" + name, ex);
        }
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
