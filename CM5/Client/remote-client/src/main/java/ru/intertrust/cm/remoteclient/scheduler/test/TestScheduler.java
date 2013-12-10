package ru.intertrust.cm.remoteclient.scheduler.test;

import java.util.List;
import java.util.Random;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.ScheduleService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.schedule.Schedule;
import ru.intertrust.cm.core.business.api.schedule.ScheduleResult;
import ru.intertrust.cm.remoteclient.ClientBase;
import ru.intertrust.cm.test.schedule.TestScheduleParameters;

public class TestScheduler extends ClientBase {
    private CrudService crudService;
    private ScheduleService schedulerService;
    private List<DomainObject> taskList;
    private Random rnd = new Random();

    public static void main(String[] args) {
        try {
            TestScheduler test = new TestScheduler();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execute(String[] args) throws Exception {
        DomainObject task1 = null;
        DomainObject task2 = null;
        DomainObject task3 = null;
        DomainObject task4 = null;
        DomainObject task5 = null;
        DomainObject task6 = null;
        try {
            super.execute(args);

            crudService = (CrudService) getService(
                    "CrudServiceImpl", CrudService.Remote.class);

            schedulerService = (ScheduleService) getService(
                    "ScheduleService", ScheduleService.Remote.class);

            //Проверка получения доступных multible классов задач
            List<String> classNames = schedulerService.getTaskClasses();
            assertTrue("Multible task classes", classNames.size() > 0);

            //Получение задач по классу
            task1 = getTaskByClass("ru.intertrust.cm.test.schedule.TestSingleSchedule");
            task2 = getTaskByClass("ru.intertrust.cm.test.schedule.TestSingleScheduleWithParams");
            //Получение задач по имени
            task3 = getTaskByName("TestMultipleSchedule_OK");
            if (task3 == null) {
                task3 =
                        schedulerService.createScheduleTask("ru.intertrust.cm.test.schedule.TestScheduleMultiple",
                                "TestMultipleSchedule_OK");
            }
            task4 = getTaskByName("TestMultipleSchedule_ERROR");
            if (task4 == null) {
                task4 =
                        schedulerService.createScheduleTask("ru.intertrust.cm.test.schedule.TestScheduleMultiple",
                                "TestMultipleSchedule_ERROR");
            }
            task5 = getTaskByName("TestMultipleSchedule_TIMEOUT");
            if (task5 == null) {
                task5 =
                        schedulerService.createScheduleTask("ru.intertrust.cm.test.schedule.TestScheduleMultiple",
                                "TestMultipleSchedule_TIMEOUT");
            }
            task6 = getTaskByName("TestMultipleSchedule_MANUAL");
            if (task6 == null) {
                task6 =
                        schedulerService.createScheduleTask("ru.intertrust.cm.test.schedule.TestScheduleMultiple",
                                "TestMultipleSchedule_MANUAL");
            }

            //Проверка загрузки расписания
            Schedule schedule = schedulerService.getTaskSchedule(task6.getId());
            //меняем расписание
            schedule.setMinute(String.valueOf(rnd.nextInt(60)));
            schedulerService.setTaskSchedule(task6.getId(), schedule);
            //Повторно зачитываем и сравниваем
            Schedule newSchedule = schedulerService.getTaskSchedule(task6.getId());
            assertTrue("Chech chenge task schedule", schedule.equals(newSchedule));
            
            //Проверка установки и получение приоритета
            int newPriority = rnd.nextInt(5);
            schedulerService.setPriority(task6.getId(), newPriority);
            task6 = crudService.find(task6.getId());
            assertTrue("Chech chenge task schedule priority", newPriority == task6.getLong(ScheduleService.SCHEDULE_PRIORITY));

            //Проверка установки и получение таймаута
            int newTimeout = rnd.nextInt(60);
            schedulerService.setTimeout(task6.getId(), newTimeout);
            task6 = crudService.find(task6.getId());
            assertTrue("Chech chenge task schedule timeout", newTimeout == task6.getLong(ScheduleService.SCHEDULE_TIMEOUT));
            
            
            //Проверка загрузки параметров
            TestScheduleParameters loadParams = (TestScheduleParameters) schedulerService.getTaskParams(task2.getId());
            log("parent param: " + loadParams.getResult());
            assertTrue("Load params", loadParams != null);

            //Установка параметров задаче
            TestScheduleParameters testparam2 = new TestScheduleParameters();
            testparam2.setResult(String.valueOf(System.currentTimeMillis()));
            schedulerService.setTaskParams(task2.getId(), testparam2);

            //Установка параметров множественным задачам
            //Задача должна выполнится без ошибок
            TestScheduleParameters testparam3 = new TestScheduleParameters();
            testparam3.setResult("10000");
            schedulerService.setTaskParams(task3.getId(), testparam3);

            //Должна вызвать ошибку при выполнение
            TestScheduleParameters testparam4 = new TestScheduleParameters();
            testparam4.setResult("STRING");
            schedulerService.setTaskParams(task4.getId(), testparam4);

            //Должна прерваться по таймауту
            TestScheduleParameters testparam5 = new TestScheduleParameters();
            testparam5.setResult("80000");
            schedulerService.setTaskParams(task5.getId(), testparam5);
            schedulerService.setTimeout(task5.getId(), 1);

            //Должна запустится вручную
            TestScheduleParameters testparam6 = new TestScheduleParameters();
            testparam6.setResult("10000");
            schedulerService.setTaskParams(task6.getId(), testparam6);
            
            
            //Активация задач
            schedulerService.enableTask(task1.getId());
            schedulerService.enableTask(task2.getId());
            schedulerService.enableTask(task3.getId());
            schedulerService.enableTask(task4.getId());
            schedulerService.enableTask(task5.getId());
            //task6 не активируем а запускаем вручную
            schedulerService.run(task6.getId());

            //Спим 2 минуты. За это время все задачи должны отработать а те которым положено отвалится по таймауту
            Thread.currentThread().sleep(120000);

            //Получаем задачи после отработки
            DomainObject afterExecTask1 = crudService.find(task1.getId());
            DomainObject afterExecTask2 = crudService.find(task2.getId());
            DomainObject afterExecTask3 = crudService.find(task3.getId());
            DomainObject afterExecTask4 = crudService.find(task4.getId());
            DomainObject afterExecTask5 = crudService.find(task5.getId());
            DomainObject afterExecTask6 = crudService.find(task6.getId());

            //Проверка статуса и времени крайнего исполнения
            if (task1.getTimestamp(ScheduleService.SCHEDULE_LAST_END) != null) {
                assertTrue(
                        "Change last end time",
                        afterExecTask1.getTimestamp(ScheduleService.SCHEDULE_LAST_END).compareTo(
                                task1.getTimestamp(ScheduleService.SCHEDULE_LAST_END)) > 0);
                assertTrue(
                        "Change last redy time",
                        afterExecTask1.getTimestamp(ScheduleService.SCHEDULE_LAST_REDY).compareTo(
                                task1.getTimestamp(ScheduleService.SCHEDULE_LAST_REDY)) > 0);
                assertTrue(
                        "Change last run time",
                        afterExecTask1.getTimestamp(ScheduleService.SCHEDULE_LAST_RUN).compareTo(
                                task1.getTimestamp(ScheduleService.SCHEDULE_LAST_RUN)) > 0);
                assertTrue(
                        "Change last wait time",
                        afterExecTask1.getTimestamp(ScheduleService.SCHEDULE_LAST_WAIT).compareTo(
                                task1.getTimestamp(ScheduleService.SCHEDULE_LAST_WAIT)) > 0);
            } else {
                //При первом запуске проверяем что поля с датами не нулевые
                assertTrue(
                        "Change last time",
                        afterExecTask1.getTimestamp(ScheduleService.SCHEDULE_LAST_WAIT) != null &&
                                afterExecTask1.getTimestamp(ScheduleService.SCHEDULE_LAST_REDY) != null &&
                                afterExecTask1.getTimestamp(ScheduleService.SCHEDULE_LAST_RUN) != null &&
                                afterExecTask1.getTimestamp(ScheduleService.SCHEDULE_LAST_END) != null);
            }

            assertTrue("Check last run status desciption",
                    afterExecTask1.getString(ScheduleService.SCHEDULE_LAST_RESULT_DESCRIPTION).equals("COMPLETE"));
            assertTrue("Check last run status",
                    afterExecTask1.getLong(ScheduleService.SCHEDULE_LAST_RESULT) == ScheduleResult.Complete
                            .toLong());

            //Проверка результата задачи с параметрами
            assertTrue(
                    "Check last run status desciption with param",
                    afterExecTask2.getString(ScheduleService.SCHEDULE_LAST_RESULT_DESCRIPTION).equals(
                            testparam2.getResult()));
            assertTrue("Check last run status with param",
                    afterExecTask2.getLong(ScheduleService.SCHEDULE_LAST_RESULT) == ScheduleResult.Complete.toLong());

            //Проверка результата множественной задачи
            //Задача которая должна отработать без ошибок
            assertTrue(
                    "Check last run status desciption task3",
                    afterExecTask3.getString(ScheduleService.SCHEDULE_LAST_RESULT_DESCRIPTION).equals(
                            testparam3.getResult()));
            assertTrue("Check last run status task3",
                    afterExecTask3.getLong(ScheduleService.SCHEDULE_LAST_RESULT) == ScheduleResult.Complete.toLong());

            //Задача которая должна отработать с ошибкой
            assertTrue(
                    "Check last run status desciption task4",
                    !afterExecTask4.getString(ScheduleService.SCHEDULE_LAST_RESULT_DESCRIPTION).equals(
                            testparam4.getResult()));
            assertTrue("Check last run status task4",
                    afterExecTask4.getLong(ScheduleService.SCHEDULE_LAST_RESULT) == ScheduleResult.Error.toLong());

            //Задача которая должна отвалится по таймауту
            assertTrue(
                    "Check last run status desciption task5",
                    !afterExecTask5.getString(ScheduleService.SCHEDULE_LAST_RESULT_DESCRIPTION).equals(
                            testparam5.getResult()));
            assertTrue("Check last run status task5",
                    afterExecTask5.getLong(ScheduleService.SCHEDULE_LAST_RESULT) == ScheduleResult.Timeout.toLong());

            //Задача которая запустилась вручную
            assertTrue(
                    "Check last run status desciption task6",
                    afterExecTask6.getString(ScheduleService.SCHEDULE_LAST_RESULT_DESCRIPTION).equals(
                            testparam6.getResult()));
            assertTrue("Check last run status task6",
                    afterExecTask6.getLong(ScheduleService.SCHEDULE_LAST_RESULT) == ScheduleResult.Complete.toLong());            
            
            log("Test complete");
        } finally {

            //Отключение задач
            if (task1 != null)
                schedulerService.disableTask(task1.getId());
            if (task2 != null)
                schedulerService.disableTask(task2.getId());
            if (task3 != null)
                schedulerService.disableTask(task3.getId());
            if (task4 != null)
                schedulerService.disableTask(task4.getId());
            if (task5 != null)
                schedulerService.disableTask(task5.getId());

            writeLog();
        }
    }

    private DomainObject getTaskByName(String name) {
        if (taskList == null) {
            taskList = schedulerService.getTaskList();
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

    private DomainObject getTaskByClass(String className) {
        if (taskList == null) {
            taskList = schedulerService.getTaskList();
        }

        DomainObject result = null;
        for (DomainObject task : taskList) {
            if (task.getString("task_class").equals(className)) {
                result = task;
                break;
            }
        }
        return result;

    }

}
