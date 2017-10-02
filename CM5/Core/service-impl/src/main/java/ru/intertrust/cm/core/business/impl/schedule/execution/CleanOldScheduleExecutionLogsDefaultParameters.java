package ru.intertrust.cm.core.business.impl.schedule.execution;

import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskDefaultParameters;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;

/**
 * Параметры по умолчанию для периодической задачи удаления логов периодических задач.<br/>
 * Текущее условие по умолчанию - все логи старше месяца удаляются.<br/>
 * <br/>
 * <p>
 * Created by Myskin Sergey on 28.09.2017.
 */
public class CleanOldScheduleExecutionLogsDefaultParameters implements ScheduleTaskDefaultParameters {

    private static final int DEFAULT_EXPIRED_MONTHS_COUNT = 1;
    private static final int DEFAULT_NOT_SET_PARAMETER = 0;

    @Override
    public ScheduleTaskParameters getDefaultParameters() {
        CleanOldScheduleExecutionLogsParameters defaultParameters = new CleanOldScheduleExecutionLogsParameters();

        defaultParameters.setMonths(DEFAULT_EXPIRED_MONTHS_COUNT);

        defaultParameters.setWeeks(DEFAULT_NOT_SET_PARAMETER);
        defaultParameters.setDays(DEFAULT_NOT_SET_PARAMETER);

        return defaultParameters;
    }

}
