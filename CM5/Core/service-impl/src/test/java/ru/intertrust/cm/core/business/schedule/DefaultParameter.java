package ru.intertrust.cm.core.business.schedule;

import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskDefaultParameters;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;

public class DefaultParameter implements ScheduleTaskDefaultParameters {

    @Override
    public ScheduleTaskParameters getDefaultParameters() {
        Parameters result = new Parameters();
        result.setResult("OK");
        return result;
    }

}