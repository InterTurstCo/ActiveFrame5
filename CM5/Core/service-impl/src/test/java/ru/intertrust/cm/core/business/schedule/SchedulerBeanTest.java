package ru.intertrust.cm.core.business.schedule;

import java.util.Calendar;
import java.util.Iterator;
import javax.ejb.EJBContext;
import javax.transaction.UserTransaction;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.intertrust.cm.core.business.api.ClusterManager;
import ru.intertrust.cm.core.business.api.NotificationService;
import ru.intertrust.cm.core.business.api.ScheduleService;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.business.api.schedule.ScheduleProcessor;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskLoader;
import ru.intertrust.cm.core.business.impl.ConfigurationLoader;
import ru.intertrust.cm.core.business.shedule.SchedulerBean;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.PersonManagementServiceDao;
import ru.intertrust.cm.core.dao.api.SchedulerDao;
import ru.intertrust.cm.core.dao.api.StatusDao;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.intertrust.cm.core.business.api.ScheduleService.SCHEDULE_DAY_OF_MONTH;
import static ru.intertrust.cm.core.business.api.ScheduleService.SCHEDULE_DAY_OF_WEEK;
import static ru.intertrust.cm.core.business.api.ScheduleService.SCHEDULE_HOUR;
import static ru.intertrust.cm.core.business.api.ScheduleService.SCHEDULE_MINUTE;
import static ru.intertrust.cm.core.business.api.ScheduleService.SCHEDULE_MONTH;
import static ru.intertrust.cm.core.business.api.ScheduleService.SCHEDULE_YEAR;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SchedulerBeanTest.JavaConfig.class)
@ActiveProfiles("SchedulerBeanTest")
public class SchedulerBeanTest {

    @Profile("SchedulerBeanTest")
    @Configuration
    public static class JavaConfig {

        @Bean
        public SchedulerBeanWithCustomCalendar schedulerBean() {
            return new SchedulerBeanWithCustomCalendar();
        }

        @Bean
        public EJBContext ejbContext() {
            return mock(EJBContext.class);
        }

        @Bean
        public ConfigurationLoader configurationLoader() {
            return mock(ConfigurationLoader.class);
        }

        @Bean
        public ClusterManager clusterManager() {
            return mock(ClusterManager.class);
        }

        @Bean
        public ScheduleTaskLoader scheduleTaskLoader() {
            return mock(ScheduleTaskLoader.class);
        }

        @Bean
        public SchedulerDao schedulerDao() {
            return mock(SchedulerDao.class);
        }

        @Bean
        public ScheduleProcessor scheduleProcessor() {
            return mock(ScheduleProcessor.class);
        }

        @Bean
        public NotificationService notificationService() {
            return mock(NotificationService.class);
        }

        @Bean
        public PersonManagementServiceDao personManagementServiceDao() {
            return mock(PersonManagementServiceDao.class);
        }

        @Bean
        public AccessControlService accessControlService() {
            return mock(AccessControlService.class);
        }

        @Bean
        public DomainObjectDao domainObjectDao() {
            return mock(DomainObjectDao.class);
        }

        @Bean
        public StatusDao statusDao() {
            return mock(StatusDao.class);
        }

    }

    private static class SchedulerBeanWithCustomCalendar extends SchedulerBean {

        private Calendar calendar;

        @Override
        protected Calendar getCalendarInstance() {
            return calendar;
        }

        private void setCalendar(Calendar calendar) {
            this.calendar = calendar;
        }
    };

    @Autowired
    private SchedulerBeanWithCustomCalendar schedulerBean;

    @Autowired
    private ConfigurationLoader configurationLoader;

    @Autowired
    private ScheduleTaskLoader scheduleTaskLoader;

    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private SchedulerDao schedulerDao;

    @Autowired
    private EJBContext ejbContext;

    private final Id identifiableObjectId = new RdbmsId(0, 0);

    private IdentifiableObject identifiableObject;

    @Before
    public void init() {
        when(configurationLoader.isConfigurationLoaded()).thenReturn(true);
        when(scheduleTaskLoader.isLoaded()).thenReturn(true);
        when(scheduleTaskLoader.isEnable()).thenReturn(true);
        when(clusterManager.hasRole(ScheduleService.SCHEDULE_MANAGER_ROLE_NAME)).thenReturn(true);

        IdentifiableObjectCollection deadExecutions = mock(IdentifiableObjectCollection.class);
        when(schedulerDao.getDeadScheduleExecution()).thenReturn(deadExecutions);
        Iterator<IdentifiableObject> deadIterator = mock(Iterator.class);
        when(deadIterator.hasNext()).thenReturn(false);
        when(deadExecutions.iterator()).thenReturn(deadIterator);

        this.identifiableObject = mock(IdentifiableObject.class);

        IdentifiableObjectCollection tasks = mock(IdentifiableObjectCollection.class);
        Iterator<IdentifiableObject> tasksIterator = mock(Iterator.class);
        when(tasksIterator.hasNext()).thenReturn(true).thenReturn(false);
        when(tasksIterator.next()).thenReturn(this.identifiableObject);
        when(tasks.iterator()).thenReturn(tasksIterator);
        when(schedulerDao.getActiveTask()).thenReturn(tasks);
        when(this.identifiableObject.getId()).thenReturn(identifiableObjectId);

        UserTransaction userTransaction = mock(UserTransaction.class);
        when(ejbContext.getUserTransaction()).thenReturn(userTransaction);
    }

    @Test
    public void testBackgroundProcessing_simpleDigit() {
        configureCron("*", "*", "1", "*", "*", "*");
        configureCurrentTime("1", "1", "1", "0", "1", "2020");
        schedulerBean.backgroundProcessing();
        verify(schedulerDao, times(1)).createTaskExecution(eq(identifiableObjectId));
        reset(schedulerDao);
    }


    @Test
    public void testBackgroundProcessing_simpleDigit_failed() {
        configureCron("*", "*", "1", "*", "*", "*");
        configureCurrentTime("1", "1", "2", "0", "1", "2020");
        schedulerBean.backgroundProcessing();
        verify(schedulerDao, times(0)).createTaskExecution(eq(identifiableObjectId));
        reset(schedulerDao);
    }

    @Test
    public void testBackgroundProcessing_digitWithSlash() {
        configureCron("*", "*", "*/5", "*", "*", "*");
        configureCurrentTime("1", "1", "10", "0", "1", "2020");
        schedulerBean.backgroundProcessing();
        verify(schedulerDao, times(1)).createTaskExecution(eq(identifiableObjectId));
    }

    @Test
    public void testBackgroundProcessing_digitWithSlash_failed() {
        configureCron("*", "*", "*/5", "*", "*", "*");
        configureCurrentTime("1", "1", "11", "0", "1", "2020");
        schedulerBean.backgroundProcessing();
        verify(schedulerDao, times(0)).createTaskExecution(eq(identifiableObjectId));
        reset(schedulerDao);
    }

    @Test
    public void testBackgroundProcessing_commaSeparated() {
        configureCron("*", "*", "1,3,5", "*", "*", "*");
        configureCurrentTime("1", "1", "3", "0", "1", "2020");
        schedulerBean.backgroundProcessing();
        verify(schedulerDao, times(1)).createTaskExecution(eq(identifiableObjectId));
        reset(schedulerDao);
    }

    @Test
    public void testBackgroundProcessing_commaSeparated_failed() {
        configureCron("*", "*", "1,3,5", "*", "*", "*");
        configureCurrentTime("1", "1", "2", "0", "1", "2020");
        schedulerBean.backgroundProcessing();
        verify(schedulerDao, times(0)).createTaskExecution(eq(identifiableObjectId));
        reset(schedulerDao);
    }

    @Test
    public void testBackgroundProcessing_hyphen() {
        configureCron("*", "*", "1-5", "*", "*", "*");
        configureCurrentTime("1", "1", "2", "0", "1", "2020");
        schedulerBean.backgroundProcessing();
        verify(schedulerDao, times(1)).createTaskExecution(eq(identifiableObjectId));
        reset(schedulerDao);
    }

    @Test
    public void testBackgroundProcessing_hyphen_failed() {
        configureCron("*", "*", "1-5", "*", "*", "*");
        configureCurrentTime("1", "1", "7", "0", "1", "2020");
        schedulerBean.backgroundProcessing();
        verify(schedulerDao, times(0)).createTaskExecution(eq(identifiableObjectId));
        reset(schedulerDao);
    }

    private void configureCurrentTime(String dayOfMonth, String dayOfWeek, String hour, String minute, String month, String year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, Integer.parseInt(year));
        calendar.set(Calendar.MONTH, Integer.parseInt(month));
        calendar.set(Calendar.DAY_OF_WEEK, Integer.parseInt(dayOfWeek));
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dayOfMonth));
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
        calendar.set(Calendar.MINUTE, Integer.parseInt(minute));
        schedulerBean.setCalendar(calendar);
    }


    private void configureCron(String dayOfMonth, String dayOfWeek, String hour, String minute, String month, String year) {
        when(identifiableObject.getString(SCHEDULE_DAY_OF_MONTH)).thenReturn(dayOfMonth);
        when(identifiableObject.getString(SCHEDULE_DAY_OF_WEEK)).thenReturn(dayOfMonth);
        when(identifiableObject.getString(SCHEDULE_HOUR)).thenReturn(hour);
        when(identifiableObject.getString(SCHEDULE_MINUTE)).thenReturn(minute);
        when(identifiableObject.getString(SCHEDULE_MONTH)).thenReturn(month);
        when(identifiableObject.getString(SCHEDULE_YEAR)).thenReturn(year);
    }
}