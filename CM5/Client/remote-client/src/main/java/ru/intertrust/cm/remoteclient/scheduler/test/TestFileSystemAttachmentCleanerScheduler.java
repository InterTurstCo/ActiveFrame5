package ru.intertrust.cm.remoteclient.scheduler.test;

import ru.intertrust.cm.core.business.api.ScheduleService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.remoteclient.ClientBase;

import java.util.List;

public class TestFileSystemAttachmentCleanerScheduler extends ClientBase {

    private ScheduleService schedulerService;


    public static void main(String[] args) {
        try {
            TestFileSystemAttachmentCleanerScheduler test = new TestFileSystemAttachmentCleanerScheduler();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execute(String[] args) throws Exception {
        super.execute(args);

        schedulerService = (ScheduleService) getService(
                "ScheduleService", ScheduleService.Remote.class);

        DomainObject task = getTaskByName("FileSystemAttachmentCleanerScheduleTask");
        if (task == null) {
            task = schedulerService.createScheduleTask(
                            "ru.intertrust.cm.core.business.impl.FileSystemAttachmentCleanerScheduleTask",
                            "FileSystemAttachmentCleanerScheduleTask");
        }

        schedulerService.enableTask(task.getId());

    }

    private DomainObject getTaskByName(String name) {
        List<DomainObject> taskList = schedulerService.getTaskList();

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
