package ru.intertrust.cm.core.business.shedule;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import ru.intertrust.cm.core.business.api.ScheduleService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTask;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskConfig;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskDefaultParameters;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskHandle;
import ru.intertrust.cm.core.business.api.schedule.SheduleType;
import ru.intertrust.cm.core.config.module.ModuleConfiguration;
import ru.intertrust.cm.core.config.module.ModuleService;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.model.ScheduleException;

/**
 * Спринг бин загрузчик классов периодических заданий
 * @author larin
 * 
 */
public class SheduleTaskLoader implements ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(SheduleTaskLoader.class);
    private ApplicationContext applicationContext;
    private Hashtable<String, SheduleTaskReestrItem> reestr = new Hashtable<String, SheduleTaskReestrItem>();

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private CollectionsDao collectionsDao;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private ModuleService moduleService;

    /**
     * Установка spring контекста
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        initReestr();
        initStorage();
    }

    /**
     * Инициализация доменных объектов
     */
    private void initStorage() {
        for (SheduleTaskReestrItem item : reestr.values()) {
            //Автоматически создаем только Singleton задачи
            if (item.configuration.type() == SheduleType.Singleton) {
                //Получение доменного объекта по классу
                DomainObject taskDo = getTaskDomainObject(item.getScheduleTask().getClass().getName());
                if (taskDo == null) {
                    createTaskDomainObject(item);
                }
            }
        }
    }

    public DomainObject createTaskDomainObject(SheduleTaskReestrItem item) {
        return createTaskDomainObject(item, null);
    }

    public DomainObject createTaskDomainObject(SheduleTaskReestrItem item, String name) {
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());

        DomainObject task = createDomainObject("schedule");
        if (name == null) {
            task.setString(ScheduleService.SCHEDULE_NAME, item.configuration.name());
        } else {
            task.setString(ScheduleService.SCHEDULE_NAME, name);
        }
        task.setString(ScheduleService.SCHEDULE_TASK_CLASS, item.scheduleTask.getClass().getName());
        task.setLong(ScheduleService.SCHEDULE_TASK_TYPE, item.configuration.type().toLong());
        task.setString(ScheduleService.SCHEDULE_YEAR, item.configuration.year());
        task.setString(ScheduleService.SCHEDULE_MONTH, item.configuration.month());
        task.setString(ScheduleService.SCHEDULE_DAY_OF_MONTH, item.configuration.dayOfMonth());
        task.setString(ScheduleService.SCHEDULE_DAY_OF_WEEK, item.configuration.dayOfWeek());
        task.setString(ScheduleService.SCHEDULE_HOUR, item.configuration.hour());
        task.setString(ScheduleService.SCHEDULE_MINUTE, item.configuration.minute());
        task.setLong(ScheduleService.SCHEDULE_TIMEOUT, item.configuration.timeout());
        task.setLong(ScheduleService.SCHEDULE_PRIORITY, item.configuration.priority());
        task.setString(ScheduleService.SCHEDULE_PARAMETERS, getDefaultParameters(item.configuration));
        task.setLong(ScheduleService.SCHEDULE_ACTIVE, item.configuration.active() ? 1L : 0);
        return domainObjectDao.save(task, accessToken);
    }

    private String getDefaultParameters(ScheduleTask configuration) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            Strategy strategy = new AnnotationStrategy();
            Serializer serializer = new Persister(strategy);
            String result = null;
            Class<? extends ScheduleTaskDefaultParameters> defaultConfigClass = configuration.configClass();
            if (!defaultConfigClass.equals(ScheduleTaskDefaultParameters.class)) {
                ScheduleTaskDefaultParameters configClass = defaultConfigClass.newInstance();
                out = new ByteArrayOutputStream();
                ScheduleTaskConfig config = new ScheduleTaskConfig();
                config.setParameters(configClass.getDefaultParameters());

                serializer.write(config, out);
                result = out.toString("utf8");
            }

            return result;
        } catch (Exception ex) {
            throw new ScheduleException(
                    "Error on get schedule task default parameters", ex);
        } finally {
            try {
                out.close();
            } catch (Exception ignoreEx) {
            }
        }
    }

    /**
     * Создание нового доменного обьекта переданного типа
     * 
     * @param type
     * @return
     */
    private DomainObject createDomainObject(String type) {
        GenericDomainObject domainObject = new GenericDomainObject();
        domainObject.setTypeName(type);
        Date currentDate = new Date();
        domainObject.setCreatedDate(currentDate);
        domainObject.setModifiedDate(currentDate);
        return domainObject;
    }

    private DomainObject getTaskDomainObject(String taskClass) {
        DomainObject result = null;
        String query = "select t.id from schedule t where t.task_class = '" + taskClass + "'";
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
        IdentifiableObjectCollection collection = collectionsDao.findCollectionByQuery(query, 0, 1000, accessToken);
        if (collection.size() > 0) {
            result = domainObjectDao.find(collection.get(0).getId(), accessToken);
        }
        return result;
    }

    /**
     * инициализация реестра переодических задач
     */
    private void initReestr() {
        try {
            //Получение всех пакетов
            List<String> basePackages = getUniqueBasePackages();

            for (String basePackage : basePackages) {

                // Сканирование класспаса
                ClassPathScanningCandidateComponentProvider scanner =
                        new ClassPathScanningCandidateComponentProvider(false);
                scanner.addIncludeFilter(new AnnotationTypeFilter(ScheduleTask.class));
                
                // Цикл по найденным классам
                for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
                    String className = bd.getBeanClassName();
                    // Получение найденного класса
                    Class<?> scheduleTaskClass = Class.forName(className);
                    // Получение анотации ScheduleTask
                    ScheduleTask annatation = (ScheduleTask) scheduleTaskClass
                            .getAnnotation(ScheduleTask.class);

                    // Проверка наличия анотации в классе
                    if (annatation != null) {

                        if (ScheduleTaskHandle.class.isAssignableFrom(scheduleTaskClass)) {

                            // создаем экземпляр класса Добавляем класс как спринговый бин с поддержкой autowire
                            ScheduleTaskHandle scheduleTask = (ScheduleTaskHandle) applicationContext
                                    .getAutowireCapableBeanFactory().createBean(scheduleTaskClass,
                                            AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
                            reestr.put(scheduleTaskClass.getName(), new SheduleTaskReestrItem(scheduleTask, annatation));
                            logger.info("Register ScheduleTaskHandle=" + scheduleTaskClass.getName());
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw new ScheduleException("Error on init schedule task classes", ex);
        }

    }

    /**
     * Получение не повторяющегося списка пакетов из описания всех модулей
     * @return
     */
    private List<String> getUniqueBasePackages() {

        List<String> result = new ArrayList<String>();
        for (ModuleConfiguration moduleConfiguration : moduleService.getModuleList()) {
            if (moduleConfiguration.getExtensionPointsPackages() != null) {
                for (String extensionPointsPackage : moduleConfiguration.getExtensionPointsPackages()) {
                    if (!result.contains(extensionPointsPackage)) {
                        result.add(extensionPointsPackage);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Класс элемента рееста классов периодических заданий
     * @author larin
     * 
     */
    public class SheduleTaskReestrItem {
        private ScheduleTaskHandle scheduleTask;
        private ScheduleTask configuration;

        public SheduleTaskReestrItem(ScheduleTaskHandle scheduleTask, ScheduleTask configuration) {
            this.scheduleTask = scheduleTask;
            this.configuration = configuration;
        }

        public ScheduleTaskHandle getScheduleTask() {
            return scheduleTask;
        }

        public void setScheduleTask(ScheduleTaskHandle scheduleTask) {
            this.scheduleTask = scheduleTask;
        }

        public ScheduleTask getConfiguration() {
            return configuration;
        }

        public void setConfiguration(ScheduleTask configuration) {
            this.configuration = configuration;
        }
    }

    public List<SheduleTaskReestrItem> getSheduleTaskReestrItems(boolean singletonOnly) {
        List<SheduleTaskReestrItem> result = new ArrayList<SheduleTaskReestrItem>();
        for (SheduleTaskReestrItem item : reestr.values()) {
            if (singletonOnly) {
                if (item.configuration.type() == SheduleType.Singleton) {
                    result.add(item);
                }
            } else {
                result.add(item);
            }
        }
        return result;
    }

    public SheduleTaskReestrItem getSheduleTaskReestrItem(String className) {
        return reestr.get(className);
    }

    public ScheduleTaskHandle getSheduleTaskHandle(String className) {
        return reestr.get(className).scheduleTask;
    }

}
