package ru.intertrust.cm.remoteclient.notification;

import java.util.List;

import ru.intertrust.cm.core.business.api.NotificationService;
import ru.intertrust.cm.core.business.api.ScheduleService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.schedule.Schedule;
import ru.intertrust.cm.remoteclient.ClientBase;

public class TestNotificationJob extends ClientBase {

    public static void main(String[] args) {
        try {
            TestNotificationJob test = new TestNotificationJob();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execute(String[] args) throws Exception {
        try {
            super.execute(args);

            ScheduleService scheduleService = (ScheduleService.Remote) getService(
                    "ScheduleService", ScheduleService.Remote.class);
            
            DomainObject notificationTask = getTaskByName(scheduleService, "NotificationScheduleTask2");
            
            //Schedule schedule = scheduleService.getTaskSchedule(notificationTask.getId());
            if (notificationTask != null) {
                scheduleService.run(notificationTask.getId());
            } else {
                System.out.println("notificationTask is null");
            }
            
            System.out.println("Test End");
        } finally {
            writeLog();
        }
    }
    
    private DomainObject getTaskByName(ScheduleService scheduleService, String name) {
        List<DomainObject> taskList = scheduleService.getTaskList();
        if (taskList == null) {
            taskList = scheduleService.getTaskList();
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
