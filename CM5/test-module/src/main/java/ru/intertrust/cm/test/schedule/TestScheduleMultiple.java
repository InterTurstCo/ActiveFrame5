package ru.intertrust.cm.test.schedule;

import ru.intertrust.cm.core.business.api.schedule.ScheduleTask;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskHandle;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;
import ru.intertrust.cm.core.business.api.schedule.SheduleType;
import ru.intertrust.cm.core.model.ScheduleException;

@ScheduleTask(name = "TestScheduleMultiple", minute = "*/1", configClass = TestSheduleDefaultParameter.class,
        type = SheduleType.Multipliable)
public class TestScheduleMultiple implements ScheduleTaskHandle {

    @Override
    public String execute(ScheduleTaskParameters parameters) {
        try {
            TestScheduleParameters testScheduleParameters = (TestScheduleParameters) parameters;

            //Тестируем обработчик ошибки в поле Result может быть как строка так и число. Если строка то упадем с ошибкой, что должны увидеть в результатах выполнения задачи 
            long value = Long.parseLong(testScheduleParameters.getResult());

            System.out.println("Run TestScheduleMultiple");
            Thread.currentThread().sleep(value);
            return testScheduleParameters.getResult();
        } catch (Exception ex) {
            throw new ScheduleException("Error exec TestScheduleMultiple", ex);
        }
    }

}
