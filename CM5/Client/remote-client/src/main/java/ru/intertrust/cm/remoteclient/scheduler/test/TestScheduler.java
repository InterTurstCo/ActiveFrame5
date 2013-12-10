package ru.intertrust.cm.remoteclient.scheduler.test;

import java.util.List;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.ReportService;
import ru.intertrust.cm.core.business.api.ReportServiceAdmin;
import ru.intertrust.cm.core.business.api.ScheduleService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.remoteclient.ClientBase;

public class TestScheduler extends ClientBase {
    private CrudService crudService;
    private CollectionsService collectionService;
    private ScheduleService schedulerService;
    private List<DomainObject> taskList;

    public static void main(String[] args) {
        try {
            TestScheduler test = new TestScheduler();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execute(String[] args) throws Exception {
        try {
            super.execute(args);

            crudService = (CrudService) getService(
                    "CrudServiceImpl", CrudService.Remote.class);

            collectionService = (CollectionsService) getService(
                    "CollectionsServiceImpl", CollectionsService.Remote.class);
            
            schedulerService = (ScheduleService) getService(
                    "ScheduleService", ScheduleService.Remote.class);
            
            //Получение отчета по классу
            DomainObject task = getTaskByClass("ru.intertrust.cm.test.schedule.TestSingleSchedule");
            schedulerService.enableTask(task.getId());
            
            Thread.currentThread().sleep(60000);
            
            schedulerService.disableTask(task.getId());
        }finally{
            writeLog();
        }
    }

    private DomainObject getTaskByClass(String className) {
        if (taskList == null){
            taskList = schedulerService.getTaskList();
        }
        
        DomainObject result = null;
        for (DomainObject task : taskList) {
            if (task.getString("task_class").equals(className)){
                result = task;
                break;
            }
        }
        return result;
        
    }

}
