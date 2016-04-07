package ru.intertrust.cm.test.schedule;

import javax.ejb.EJBContext;
import javax.ejb.SessionContext;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTask;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskHandle;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;

@ScheduleTask(name = "TestUseApiSchedule", minute = "*/1")
public class TestUseApiSchedule implements ScheduleTaskHandle{
    @Autowired
    private CollectionsService collectionsService;
    
    @Override
    public String execute(EJBContext ejbContext, SessionContext sessionContext, ScheduleTaskParameters parameters) throws InterruptedException {
        IdentifiableObjectCollection collection = collectionsService.findCollectionByQuery("select * from person where login='admin'");
        if (collection.size() == 0){
            throw new RuntimeException("TestUseApiSchedule not work");
        }
        return "Test is OK";
    }

}
