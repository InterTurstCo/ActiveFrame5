package ru.intertrust.cm.test.schedule;

import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskDefaultParameters;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;

public class TestSheduleDefaultParameter implements ScheduleTaskDefaultParameters{

    @Override
    public ScheduleTaskParameters getDefaultParameters() {
        TestScheduleParameters result = new TestScheduleParameters();
        result.setResult("OK");        
        return result;
    }

}
