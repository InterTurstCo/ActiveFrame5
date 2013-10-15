package ru.intertrust.cm.remoteclient.process.test;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.PersonManagementService;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.remoteclient.ClientBase;

/**
 * Тестовый клиент к подсистеме процессов.
 * @author larin
 * 
 */
public class TestProcess extends ClientBase {

    public static void main(String[] args) {
        try {
            TestProcess test = new TestProcess();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execute(String[] args) throws Exception {
        try {
            super.execute(args);

            ProcessService.Remote service = (ProcessService.Remote) getService(
                    "ProcessService", ProcessService.Remote.class);

            CrudService.Remote crudService = (CrudService.Remote) getService(
                    "CrudServiceImpl", CrudService.Remote.class);

            PersonManagementService.Remote personService =
                    (PersonManagementService.Remote) getService("PersonManagementService", PersonManagementService.Remote.class);

            // Создаем персону
            Id adminPerson = personService.getPersonId("admin");
            if (adminPerson == null) {
                DomainObject person = crudService.createDomainObject("Person");
                person.setString("Login", "admin");
                person.setString("FirstName", "Администратор");
                person.setString("LastName", "Администраторович");
                person.setString("EMail", "admin@cm5.ru");
                person = crudService.save(person);
            }

            byte[] processDef = getProcessAsByteArray("templates/SimpleProcess.bpmn");
            String defId = service.deployProcess(processDef,
                    "SimpleProcess.bpmn");

            // Создание документа, который НЕ будет прикреплен к процессу
            DomainObject attachmentNotInProcess = crudService
                    .createDomainObject("test_process_attachment");
            attachmentNotInProcess.setString("test_text", "Создание.");
            attachmentNotInProcess.setLong("test_long", 10L);
            attachmentNotInProcess.setDecimal("test_decimal",
                    new BigDecimal(10));
            attachmentNotInProcess.setTimestamp("test_date", new Date());
            // attachmentNotInProcess.setReference("person", new
            // RdbmsId("person|1"));
            attachmentNotInProcess = crudService.save(attachmentNotInProcess);

            // Создание документа, который будет прикреплен к процессу
            DomainObject attachment = crudService
                    .createDomainObject("test_process_attachment");
            attachment.setString("test_text", "Создание.");
            attachment.setLong("test_long", 10L);
            attachment.setDecimal("test_decimal", new BigDecimal(10));
            attachment.setTimestamp("test_date", new Date());
            // attachment.setReference("person", new RdbmsId("person|1"));
            attachment = crudService.save(attachment);

            // Запуск процесса
            service.startProcess("simpleProcess", attachment.getId(), null);

            // Получение всех задач пользователя и их завершение
            List<DomainObject> tasks = service.getUserTasks();
            log("Find " + tasks.size() + " tasks");
            assertTrue("Find all tasks", tasks.size() > 0);
            for (DomainObject task : tasks) {
                if ("usertask1".equals(task.getString("ActivityId"))) {
                    service.completeTask(task.getId(), null, null);
                    log("Complete " + task.getId());
                }
            }

            // Получение всех задач по документу который не прикреплен к
            // процессу. Должно получится 0 задач
            tasks = service.getUserDomainObjectTasks(attachmentNotInProcess
                    .getId());
            log("Find " + tasks.size()
                    + " tasks for noattached to process document");
            assertTrue("Find all to not attached document tasks", tasks.size() == 0);

            // Получение всех задач по документу и их завершение
            tasks = service.getUserDomainObjectTasks(attachment.getId());
            log("Find " + tasks.size() + " tasks");
            for (DomainObject task : tasks) {
                if ("usertask2".equals(task.getString("ActivityId"))) {
                    service.completeTask(task.getId(), null, null);
                    log("Complete " + task.getId());
                }
            }

            // Получение всех задач пользователя по документу и их завершение с
            // определенным результатом
            tasks = service.getUserDomainObjectTasks(attachment.getId());
            log("Find " + tasks.size() + " tasks");
            for (DomainObject task : tasks) {
                if ("usertask3".equals(task.getString("ActivityId"))) {
                    // Получаем все доступные действия
                    String actions = task.getString("Actions");
                    log("All actions = " + actions);
                    service.completeTask(task.getId(), null, "YES");
                    log("Complete " + task.getId());
                }
            }

            // Получение задачи в подпроцессе и завершение одной из них
            tasks = service.getUserDomainObjectTasks(attachment.getId());
            log("Find " + tasks.size() + " tasks");
            for (DomainObject task : tasks) {
                if ("usertask4".equals(task.getString("ActivityId"))) {
                    service.completeTask(task.getId(), null, null);
                    log("Complete " + task.getId());
                    // Завершаем только одну из двух
                    break;
                }
            }

            // Получение задачи, по подпроцессу задачи быть не должно, задача
            // должна быть только по следующей активнности
            tasks = service.getUserDomainObjectTasks(attachment.getId());
            log("Find " + tasks.size() + " tasks");
            assertTrue("Delete subprocess task", tasks.size() == 1);
            for (DomainObject task : tasks) {
                if ("usertask5".equals(task.getString("ActivityId"))) {
                    service.completeTask(task.getId(), null, null);
                    log("Complete " + task.getId());
                    break;
                }
            }

            crudService.delete(attachmentNotInProcess.getId());

        } finally {
            writeLog();
        }
    }

    private byte[] getProcessAsByteArray(String processPath) throws IOException {
        FileInputStream stream = null;
        ByteArrayOutputStream out = null;
        try {
            stream = new FileInputStream(processPath);
            out = new ByteArrayOutputStream();

            int read = 0;
            byte[] buffer = new byte[1024];
            while ((read = stream.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }

            return out.toByteArray();
        } finally {
            stream.close();
            out.close();
        }
    }

    private void assertTrue(String message, boolean param) throws Exception {
        if (!param) {
            throw new Exception(message);
        }
        System.out.println(message + ": OK");
    }

    private void assertFalse(String message, boolean param) throws Exception {
        if (param) {
            throw new Exception(message);
        }
        System.out.println(message + ": OK");
    }

}
