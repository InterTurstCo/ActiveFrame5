package ru.intertrust.cm.remoteclient.scheduler.test;

import java.util.List;

import ru.intertrust.cm.core.business.api.ScheduleService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.remoteclient.ClientBase;

public class TestAllNodeTask extends ClientBase{
    
    private ScheduleService schedulerService;
    
    public static void main(String[] args) {
        try {
            TestAllNodeTask test = new TestAllNodeTask();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void execute(String[] args) throws Exception {
        super.execute(args);
        

        schedulerService = (ScheduleService) getService(
                "ScheduleService", ScheduleService.Remote.class);
        
        List<DomainObject> tasks = schedulerService.getTaskList();
        DomainObject allNodeTask = null;
        for (DomainObject task : tasks) {
            if (task.getString("name").equalsIgnoreCase("TestAllNodeTask")){
                allNodeTask = task;
                break;
            }
        }
        
        schedulerService.run(allNodeTask.getId());
        log("Test complete");
        
    }
}
