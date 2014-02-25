package ru.intertrust.cm.test.schedule;

import org.springframework.jmx.access.InvocationFailureException;

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
            System.out.println("Run TestScheduleMultiple value = " + testScheduleParameters.toString());

            if (testScheduleParameters.isError()){
                //Тест обработки ошибки в задачк
                throw new RuntimeException("Test exception in schedule task");
            }else if(testScheduleParameters.isThrowInterruptedOnTimeout()){
                //Тест прерывания по таймауту с помощью InterruptedException
                int work = 0;
                while(work < testScheduleParameters.getWorkTime()){
                    Thread.currentThread().sleep(1000);
                    work += 1;
                    if (Thread.currentThread().isInterrupted()){
                        throw new InterruptedException();
                    }
                }
            }else if(testScheduleParameters.isStopWorkOnTimeout()){
                //Тест обработки ошибки по таймауту с помощью прерывания цикла
                int work = 0;
                while(work < testScheduleParameters.getWorkTime()){
                    Thread.currentThread().sleep(1000);
                    work += 1;
                    if (Thread.currentThread().isInterrupted()){
                        break;
                    }
                }
            }else{
                //тест задания, которое не прерывается а продолжает работать
                Thread.currentThread().sleep(testScheduleParameters.getWorkTime() * 1000);
            }

            System.out.println("End Run TestScheduleMultiple value = " + testScheduleParameters.toString());
            return testScheduleParameters.getResult();
        } catch (Exception ex) {
            throw new ScheduleException("Error exec TestScheduleMultiple", ex);
        } 
    }

}
