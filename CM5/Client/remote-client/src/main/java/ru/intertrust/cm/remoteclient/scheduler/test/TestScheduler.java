package ru.intertrust.cm.remoteclient.scheduler.test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.ScheduleService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.schedule.Schedule;
import ru.intertrust.cm.core.business.api.schedule.ScheduleResult;
import ru.intertrust.cm.remoteclient.AssertExeption;
import ru.intertrust.cm.remoteclient.ClientBase;
import ru.intertrust.cm.test.schedule.TestScheduleParameters;

public class TestScheduler extends ClientBase {
    private CrudService crudService;
    private ScheduleService schedulerService;
    private CollectionsService collectionsService;
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
        DomainObject task7 = null;
        DomainObject task8 = null;
        DomainObject task9 = null;
        try {
            super.execute(args);

            crudService = (CrudService) getService(
                    "CrudServiceImpl", CrudService.Remote.class);

            schedulerService = (ScheduleService) getService(
                    "ScheduleService", ScheduleService.Remote.class);
            
            collectionsService = (CollectionsService)getService(
                    "CollectionsServiceImpl", CollectionsService.Remote.class);

            //Проверка получения доступных multible классов задач
            List<String> classNames = schedulerService.getTaskClasses();
            assertTrue("Multible task classes", classNames.size() > 0);

            //Получение задач по классу
            task1 = getTaskByClass("ru.intertrust.cm.test.schedule.TestSingleSchedule");
            task2 = getTaskByClass("ru.intertrust.cm.test.schedule.TestSingleScheduleWithParams");
            task9 = getTaskByClass("ru.intertrust.testmodule.extensions.TestScheduleInDifferentPackage");
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
            task7 = getTaskByName("TestMultipleSchedule_TIMEOUT_INTERRUPTED");
            if (task7 == null) {
                task7 =
                        schedulerService.createScheduleTask("ru.intertrust.cm.test.schedule.TestScheduleMultiple",
                                "TestMultipleSchedule_TIMEOUT_INTERRUPTED");
            }
            task8 = getTaskByName("TestMultipleSchedule_TIMEOUT_STOP");
            if (task8 == null) {
                task8 =
                        schedulerService.createScheduleTask("ru.intertrust.cm.test.schedule.TestScheduleMultiple",
                                "TestMultipleSchedule_TIMEOUT_STOP");
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
            if (task2 == null) {
                throw new AssertExeption("task2 is null");
            }
            TestScheduleParameters loadParams = (TestScheduleParameters) schedulerService.getTaskParams(task2.getId());
            log("parent param: " + loadParams.getResult());
            assertTrue("Load params", loadParams != null);

            //Установка параметров задаче
            TestScheduleParameters testparam2 = new TestScheduleParameters();
            testparam2.setResult(String.valueOf(System.currentTimeMillis()));
            testparam2.setWorkTime(5);
            schedulerService.setTaskParams(task2.getId(), testparam2);

            //Установка параметров множественным задачам
            //Задача должна выполнится без ошибок
            TestScheduleParameters testparam3 = new TestScheduleParameters();
            testparam3.setResult("Task OK");
            testparam3.setWorkTime(5);
            schedulerService.setTaskParams(task3.getId(), testparam3);

            //Должна вызвать ошибку при выполнение
            TestScheduleParameters testparam4 = new TestScheduleParameters();
            testparam4.setResult("Test error");
            testparam4.setError(true);
            schedulerService.setTaskParams(task4.getId(), testparam4);

            //Должна проигнорировать таймаут а сервис должен задачу сделать неактивной
            TestScheduleParameters testparam5 = new TestScheduleParameters();
            testparam5.setResult("Test timeout by bad task");
            testparam5.setWorkTime(140);
            schedulerService.setTaskParams(task5.getId(), testparam5);
            schedulerService.setTimeout(task5.getId(), 1);

            //Должна запустится вручную
            TestScheduleParameters testparam6 = new TestScheduleParameters();
            testparam6.setResult("Test hand task");
            schedulerService.setTaskParams(task6.getId(), testparam6);

            //Должна завершится по таймауту выкидыванием InterruptedException
            TestScheduleParameters testparam7 = new TestScheduleParameters();
            testparam7.setResult("Test InterruptedException");
            testparam7.setThrowInterruptedOnTimeout(true);
            testparam7.setWorkTime(125);
            schedulerService.setTaskParams(task7.getId(), testparam7);
            schedulerService.setTimeout(task7.getId(), 1);

            //Должна завершится по таймауту просто прекращением работы
            TestScheduleParameters testparam8 = new TestScheduleParameters();
            testparam8.setResult("Test end work by timeout");
            testparam8.setWorkTime(125);
            testparam8.setStopWorkOnTimeout(true);
            schedulerService.setTaskParams(task8.getId(), testparam8);
            schedulerService.setTimeout(task8.getId(), 1);

            //Ждем чтобы было без 5 секунд до начала запуска задач по расписанию, для синхронизации теста и заданий
            while (Calendar.getInstance().get(Calendar.SECOND) != 55) {
                Thread.currentThread().sleep(500);
            }

            //Активация задач
            schedulerService.enableTask(task1.getId());
            schedulerService.enableTask(task2.getId());
            schedulerService.enableTask(task3.getId());
            schedulerService.enableTask(task4.getId());
            schedulerService.enableTask(task5.getId());
            //task6 не активируем а запускаем вручную
            schedulerService.run(task6.getId());
            schedulerService.enableTask(task7.getId());
            schedulerService.enableTask(task8.getId());
            schedulerService.enableTask(task9.getId());

            //Спим 85. За это время все задачи должны отработать а те которым положено - отвалится по таймауту, кроме задачи 5
            Thread.currentThread().sleep(85000);

            //Получаем задачи после отработки
            DomainObject afterExecTask1 = getLastTaskExecution(task1.getId());
            DomainObject afterExecTask2 = getLastTaskExecution(task2.getId());
            DomainObject afterExecTask3 = getLastTaskExecution(task3.getId());
            DomainObject afterExecTask4 = getLastTaskExecution(task4.getId());
            DomainObject afterExecTask5 = getLastTaskExecution(task5.getId());
            DomainObject afterExecTask6 = getLastTaskExecution(task6.getId());
            DomainObject afterExecTask7 = getLastTaskExecution(task7.getId());
            DomainObject afterExecTask8 = getLastTaskExecution(task8.getId());
            DomainObject afterExecTask9 = getLastTaskExecution(task9.getId());

            //Проверка статуса и времени крайнего исполнения
            assertTrue("Status complete", afterExecTask1.getStatus().equals(getStatusByName(ScheduleService.SCHEDULE_STATUS_COMPLETE)));
            assertTrue("Change last end time", afterExecTask1.getTimestamp(ScheduleService.SCHEDULE_COMPLETE) != null);
            assertTrue("Change last redy time", afterExecTask1.getTimestamp(ScheduleService.SCHEDULE_REDY) != null);
            assertTrue("Change last run time", afterExecTask1.getTimestamp(ScheduleService.SCHEDULE_RUN) != null);
            assertTrue("Change last wait time", afterExecTask1.getTimestamp(ScheduleService.SCHEDULE_WAIT) != null);
            assertTrue("Check last run status desciption",
                    afterExecTask1.getString(ScheduleService.SCHEDULE_RESULT_DESCRIPTION).equals("COMPLETE"));
            assertTrue("Check last run status",
                    afterExecTask1.getLong(ScheduleService.SCHEDULE_RESULT) == ScheduleResult.Complete.toLong());

            //Проверка результата задачи с параметрами
            assertTrue(
                    "Check last run status desciption with param",
                    afterExecTask2.getString(ScheduleService.SCHEDULE_RESULT_DESCRIPTION).equals(
                            testparam2.getResult()));
            assertTrue("Check last run status with param",
                    afterExecTask2.getLong(ScheduleService.SCHEDULE_RESULT) == ScheduleResult.Complete.toLong());

            //Проверка результата множественной задачи
            //Задача которая должна отработать без ошибок
            assertTrue(
                    "Check last run status desciption task3",
                    afterExecTask3.getString(ScheduleService.SCHEDULE_RESULT_DESCRIPTION).equals(
                            testparam3.getResult()));
            assertTrue("Check last run status task3",
                    afterExecTask3.getLong(ScheduleService.SCHEDULE_RESULT) == ScheduleResult.Complete.toLong());

            //Задача которая должна отработать с ошибкой
            assertTrue(
                    "Check last run status desciption task4",
                    !afterExecTask4.getString(ScheduleService.SCHEDULE_RESULT_DESCRIPTION).equals(
                            testparam4.getResult()));
            assertTrue("Check last run status task4",
                    afterExecTask4.getLong(ScheduleService.SCHEDULE_RESULT) == ScheduleResult.Error.toLong());

            //Задача которая не должна  отваливается по таймауту так как игнорирует команду cancel
            assertTrue(
                    "Check last run status desciption task5",
                    afterExecTask5.getString(ScheduleService.SCHEDULE_RESULT_DESCRIPTION) == null);
            assertTrue("Check last run status task5",
                    afterExecTask5.getLong(ScheduleService.SCHEDULE_RESULT) == null);

            //Задача которая запустилась вручную
            assertTrue(
                    "Check last run status desciption task6",
                    afterExecTask6.getString(ScheduleService.SCHEDULE_RESULT_DESCRIPTION).equals(
                            testparam6.getResult()));
            assertTrue("Check last run status task6",
                    afterExecTask6.getLong(ScheduleService.SCHEDULE_RESULT) == ScheduleResult.Complete.toLong());

            //Задача которая завершилась по InterruptedException
            assertTrue(
                    "Check last run status desciption task7",
                    !afterExecTask7.getString(ScheduleService.SCHEDULE_RESULT_DESCRIPTION).equals(
                            testparam7.getResult()));
            assertTrue("Check last run status task7",
                    afterExecTask7.getLong(ScheduleService.SCHEDULE_RESULT) == ScheduleResult.Timeout.toLong());

            //Задача которая завершилась по InterruptedException
            assertTrue(
                    "Check last run status desciption task8",
                    !afterExecTask8.getString(ScheduleService.SCHEDULE_RESULT_DESCRIPTION).equals(
                            testparam8.getResult()));
            assertTrue("Check last run status task8",
                    afterExecTask8.getLong(ScheduleService.SCHEDULE_RESULT) == ScheduleResult.Timeout.toLong());

            //Спим еще 65. За это завершится игнорирующая таймаут задача
            Thread.currentThread().sleep(70000);

            //Задача которая не отвалилась по таймауту должна завершится и стать не активной
            afterExecTask5 = crudService.find(afterExecTask5.getId());
            assertTrue("Check last run status desciption task5",
                    !testparam5.getResult().equals(afterExecTask5.getString(ScheduleService.SCHEDULE_RESULT_DESCRIPTION)));
            assertTrue("Check last run status task5",
                    afterExecTask5.getLong(ScheduleService.SCHEDULE_RESULT) == ScheduleResult.Timeout.toLong());

            
            task5 = crudService.find(task5.getId());
            assertTrue("Check active task5", !task5.getBoolean(ScheduleService.SCHEDULE_ACTIVE));
            assertTrue("Check bad flag in task5", 
                    task5.getBoolean(ScheduleService.SCHEDULE_BAD_TASK) != null && task5.getBoolean(ScheduleService.SCHEDULE_BAD_TASK));

            // Статус задачи из другово пакета
            assertTrue("Change last time",
                        afterExecTask9.getTimestamp(ScheduleService.SCHEDULE_WAIT) != null &&
                                afterExecTask9.getTimestamp(ScheduleService.SCHEDULE_REDY) != null &&
                                afterExecTask9.getTimestamp(ScheduleService.SCHEDULE_RUN) != null &&
                                afterExecTask9.getTimestamp(ScheduleService.SCHEDULE_COMPLETE) != null);

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
            if (task7 != null)
                schedulerService.disableTask(task7.getId());
            if (task8 != null)
                schedulerService.disableTask(task8.getId());
            if (task9 != null)
                schedulerService.disableTask(task9.getId());

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

    private DomainObject getLastTaskExecution(Id taskId) {
        List<Value> params = new ArrayList<Value>();
        params.add(new ReferenceValue(taskId));
        IdentifiableObjectCollection collection =
                collectionsService.findCollectionByQuery("select id from schedule_execution where schedule = {0} order by created_date desc limit 1", params);
        return crudService.find(collection.get(0).getId());
    }

    private Id getStatusByName(String status) {
        List<Value> params = new ArrayList<Value>();
        params.add(new StringValue(status));
        IdentifiableObjectCollection collection =
                collectionsService.findCollectionByQuery("select id from status where name = {0}", params);
        return collection.get(0).getId();
    }

}
