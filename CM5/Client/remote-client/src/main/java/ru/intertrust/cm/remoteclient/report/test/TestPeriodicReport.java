package ru.intertrust.cm.remoteclient.report.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.ScheduleService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.QueryParameter;
import ru.intertrust.cm.core.business.api.dto.RelativeDate;
import ru.intertrust.cm.core.business.api.dto.RelativeDateBase;
import ru.intertrust.cm.core.business.api.dto.ReportShceduleParameter;
import ru.intertrust.cm.core.business.api.dto.ShceduleTaskReportParam;
import ru.intertrust.cm.core.business.api.schedule.Schedule;
import ru.intertrust.cm.remoteclient.ClientBase;

public class TestPeriodicReport extends ClientBase {
    private CrudService crudService;
    private ScheduleService schedulerService;
    private List<DomainObject> taskList;
    private Random rnd = new Random();

    public static void main(String[] args) {
        try {
            TestPeriodicReport test = new TestPeriodicReport();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execute(String[] args) throws Exception {
        DomainObject task = null;
        try {
            super.execute(args);

            crudService = (CrudService) getService(
                    "CrudServiceImpl", CrudService.Remote.class);

            schedulerService = (ScheduleService) getService(
                    "ScheduleService", ScheduleService.Remote.class);

            //Получение задачи
            task = getTaskByName("TestPereodicReport");
            if (task == null) {
                //Создаем задачу
                task = schedulerService.createScheduleTask("ru.intertrust.cm.core.report.ReportShcedule",
                        "TestPereodicReport");
            }

            //Устанавливаем рассписание
            Schedule schedule = schedulerService.getTaskSchedule(task.getId());
            schedule.setMinute("*");
            schedule.setHour("*");
            schedulerService.setTaskSchedule(task.getId(), schedule);
            
            //Устанавливаем параметры
            ReportShceduleParameter param = new ReportShceduleParameter();
            param.setName("test-periodic-report");
            param.setParameters(new ArrayList<ShceduleTaskReportParam>());
            param.getParameters().add(new ShceduleTaskReportParam("STRING_PARAM", "String param value"));
            param.getParameters().add(new ShceduleTaskReportParam("LONG_PARAM", new Long(10)));
            param.getParameters().add(new ShceduleTaskReportParam("DATE_PARAM", new Date()));

            RelativeDate relDateParam = new RelativeDate();
            relDateParam.setBaseDate(RelativeDateBase.START_MONTH);
            param.getParameters().add(new ShceduleTaskReportParam("REL_DATE", relDateParam));
            
            param.getParameters().add(new ShceduleTaskReportParam("QUERY_PARAM", new QueryParameter("select id from person where id = {0}")));
            
            //Устанавливаем адресатов
            param.setAddresseeQuery("select id from person where id = {0}");
            
            //Устанавливаем контекст
            param.setReportContextQuery("select id from person where login = 'person10'");
             
            schedulerService.setTaskParams(task.getId(), param);
            
            //Запускаем задачу
            //schedulerService.enableTask(task.getId());
            schedulerService.run(task.getId());
            
            //Спим 10 сек, время генерации отчета
            //Thread.currentThread().sleep(70000);
            
            
            log("Test complete");
        } finally {

            //Отключение задач
            if (task != null)
                schedulerService.disableTask(task.getId());

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
