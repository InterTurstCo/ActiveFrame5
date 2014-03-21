package ru.intertrust.cm.remoteclient.scheduler.test;

import java.util.Calendar;
import java.util.List;
import java.util.Random;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.ScheduleService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationPriority;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationTaskMode;
import ru.intertrust.cm.core.business.api.notification.NotificationTaskConfig;
import ru.intertrust.cm.core.config.FindObjectsConfig;
import ru.intertrust.cm.core.config.FindObjectsQueryConfig;
import ru.intertrust.cm.remoteclient.ClientBase;

public class TestNotificationSchedule extends ClientBase {

    private CrudService crudService;
    private ScheduleService schedulerService;
    private List<DomainObject> taskList;
    private Random rnd = new Random();

    public static void main(String[] args) {
        try {
            TestNotificationSchedule test = new TestNotificationSchedule();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execute(String[] args) throws Exception {
        DomainObject task = null;
        try {
            super.execute(args);

            crudService = (CrudService) getService(
                    "CrudServiceImpl", CrudService.Remote.class);

            schedulerService = (ScheduleService) getService(
                    "ScheduleService", ScheduleService.Remote.class);

            task = getTaskByName("NotificationScheduleTaskTest");
            if (task == null) {
                task =
                        schedulerService.createScheduleTask(
                                "ru.intertrust.cm.core.business.impl.notification.NotificationScheduleTask",
                                "NotificationScheduleTaskTest");
            }

            NotificationTaskConfig testparam = new NotificationTaskConfig();
            testparam.setNotificationType("TEST_NOTIFICATION_SCHEDULE_TASK");
            testparam.setNotificationPriority(NotificationPriority.NORMAL);
            testparam.setTaskMode(NotificationTaskMode.BY_DOMAIN_OBJECT);
            
            //По всем организациям
            FindObjectsConfig findDomainObject = new FindObjectsConfig();
            findDomainObject.setFindObjectType(new FindObjectsQueryConfig("select id from organization"));
            testparam.setFindDomainObjects(findDomainObject);

            //Руководителям организации
            FindObjectsConfig findPersonObject = new FindObjectsConfig();
            findPersonObject.setFindObjectType(new FindObjectsQueryConfig("select boss from organization where id={0}"));
            testparam.setFindPersons(findPersonObject);
            
            schedulerService.setTaskParams(task.getId(), testparam);

            //Ждем чтобы было без 5 секунд до начала запуска задач по расписанию, для синхронизации теста и заданий
            while (Calendar.getInstance().get(Calendar.SECOND) != 55) {
                Thread.currentThread().sleep(500);
            }

            schedulerService.enableTask(task.getId());

            Thread.currentThread().sleep(100000);

        } finally {

            //Отключение задач
            if (task != null)
                schedulerService.disableTask(task.getId());
            writeLog();
        }
    }

    private DomainObject getTaskByName(String name) {
        if (taskList == null) {
            taskList = schedulerService.getTaskList();
        }

        DomainObject result = null;
        for (DomainObject task : taskList) {
            if (task.getString("name").equals(name)) {
                result = task;
                break;
            }
        }
        return result;
    }
}
