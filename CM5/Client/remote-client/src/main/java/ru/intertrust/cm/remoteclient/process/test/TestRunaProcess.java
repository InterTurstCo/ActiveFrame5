package ru.intertrust.cm.remoteclient.process.test;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.NamingException;

import org.springframework.util.StringUtils;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.PersonManagementService;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.action.SimpleActionConfig;
import ru.intertrust.cm.core.gui.api.server.ActionService;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionContext;
import ru.intertrust.cm.remoteclient.ClientBase;

import static ru.intertrust.cm.remoteclient.process.test.CommonMethods.deployProcess;

/**
 * Тестовый клиент к подсистеме процессов.
 *
 * @author larin
 */
public class TestRunaProcess extends ClientBase {

    public static void main(String[] args) throws InterruptedException {
        try {
            TestRunaProcess test = new TestRunaProcess();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // Пауза для IDE, чтоб обновилась output консоль
        Thread.sleep(1000);
    }

    public void execute(String[] args) throws Exception {
        try {
            super.execute(args);

            // Создаем персону
            Id adminPerson = getPersonManagementService().getPersonId("admin");
            if (adminPerson == null) {
                DomainObject person = getCrudService().createDomainObject("Person");
                person.setString("Login", "admin");
                person.setString("FirstName", "Администратор");
                person.setString("LastName", "Администраторович");
                person.setString("EMail", "admin@cm5.ru");
                person = getCrudService().save(person);
            }

            final ProcessService.Remote processService = getProcessService("admin");
            // Установка процесса
            final Id defId = deployProcess(processService, "templates/runa/test-1.par", "test-1", true);
            assertTrue("Deploy process", defId != null);

            // Повторная установка процесса
            Id defId2 = deployProcess(processService, "templates/runa/test-1.par", "test-1", true);
            assertTrue("Secondary deploy process", defId2 != null && !defId.equals(defId2));


            // Создание документа, который НЕ будет прикреплен к процессу
            DomainObject attachmentNotInProcess = getCrudService()
                    .createDomainObject("test_process_attachment");
            attachmentNotInProcess.setString("test_text", "Создание.");
            attachmentNotInProcess.setLong("test_long", 10L);
            attachmentNotInProcess.setDecimal("test_decimal",
                    new BigDecimal(10));
            attachmentNotInProcess.setTimestamp("test_date", new Date());
            attachmentNotInProcess = getCrudService().save(attachmentNotInProcess);

            // Создание документа, который будет прикреплен к процессу
            DomainObject attachment = getCrudService()
                    .createDomainObject("test_process_attachment");
            attachment.setString("test_text", "Создание.");
            attachment.setLong("test_long", 10L);
            attachment.setDecimal("test_decimal", new BigDecimal(10));
            attachment.setTimestamp("test_date", new Date());
            attachment.setReference("author", getEmployee("person2"));
            attachment.setReference("signer", getEmployee("person3"));
            attachment.setReference("registrator", getEmployee("person4"));
            attachment = getCrudService().save(attachment);

            //Получение действий для регистратора
            ActionService personActionService = getActionService("person4");
            List<ActionContext> actions = personActionService.getActions(attachment.getId());
            assertTrue("Action count to start", actions.size() == 0);

            //Получение действий для автора
            personActionService = getActionService("person2");
            actions = personActionService.getActions(attachment.getId());
            assertTrue("Action count to start",
                    actions.size() == 1 && ((ActionConfig) actions.get(0).getActionConfig()).getName().equals("start-test_process_attachment-process"));

            // Запуск процесса
            getProcessService("person2").startProcess("test-1", attachment.getId(), null);

            // Получение статуса объекта прикрепленного к процессу
            attachment = getCrudService().find(attachment.getId());
            DomainObject runStatus = getCrudService().findByUniqueKey("Status", Collections.singletonMap("Name", new StringValue("Run")));
            assertTrue("Check attachment status", attachment.getStatus().equals(runStatus.getId()));
                    

            //Получение задачь у пользователя 5
            // Для руны надо подождать минуту, чтоб успели синхронизироватся задачи
            //Thread.sleep(60000);
            personActionService = getActionService("person5");
            actions = personActionService.getActions(attachment.getId());
            SimpleActionContext context = (SimpleActionContext) actions.get(0);
            SimpleActionConfig config = context.getActionConfig();
            assertTrue("Action count to task 1", actions.size() == 1 && config.getProperty("complete.activity.id").equals("ID11"));

            //Попытка завершить задачу другим пользователем, у кого нет задачи
            // Получение всех задач пользователя и их завершение
            for (ActionContext actionContext : actions) {
                context = (SimpleActionContext) actionContext;
                config = context.getActionConfig();
                try {
                    getProcessService("person1").completeTask(new RdbmsId(config.getProperty("complete.task.id")), null,
                            config.getProperty("complete.task.action"));
                    assertTrue("Complete not person task", false);
                } catch (Exception ignoreEx) {
                    //Правильная ошибка
                }
            }

            // Получение всех задач пользователя и их завершение
            for (ActionContext actionContext : actions) {
                context = (SimpleActionContext) actionContext;
                config = context.getActionConfig();
                getProcessService("person5").completeTask(new RdbmsId(config.getProperty("complete.task.id")), null,
                        config.getProperty("complete.task.action"));
            }

            // Получение всех задач по документу который не прикреплен к
            // процессу. Должно получится 0 задач
            List<DomainObject> tasks = processService.getUserDomainObjectTasks(attachmentNotInProcess
                    .getId());
            log("Find " + tasks.size()
                    + " tasks for noattached to process document");
            assertTrue("Find all to not attached document tasks", tasks.size() == 0);

            //Получение задачь у пользователя 2, автора
            // Для руны надо подождать минуту, чтоб успели синхронизироватся задачи
            //Thread.sleep(60000);
            personActionService = getActionService("person2");
            actions = personActionService.getActions(attachment.getId());
            context = (SimpleActionContext) actions.get(0);
            config = context.getActionConfig();
            assertTrue("Action count to task 2", actions.size() == 1 && config.getProperty("complete.activity.id").equals("ID16"));

            // Получение всех задач по документу и их завершение
            for (ActionContext actionContext : actions) {
                context = (SimpleActionContext) actionContext;
                config = context.getActionConfig();
                getProcessService("person2").completeTask(new RdbmsId(config.getProperty("complete.task.id")), null,
                        config.getProperty("complete.task.action"));
            }

            //Получение задачь у пользователя 3, подписывающего
            // Для руны надо подождать минуту, чтоб успели синхронизироватся задачи
            //Thread.sleep(60000);
            personActionService = getActionService("person3");
            actions = personActionService.getActions(attachment.getId());
            context = (SimpleActionContext) actions.get(0);
            config = context.getActionConfig();
            assertTrue("Action count to task 3", actions.size() == 2 && config.getProperty("complete.activity.id").equals("ID27")
                    && (config.getProperty("complete.task.action").equals("first-action") || config.getProperty("complete.task.action").equals("second-action")));

            //Получение задачь у пользователя 4, регистратора
            personActionService = getActionService("person4");
            actions = personActionService.getActions(attachment.getId());
            context = (SimpleActionContext) actions.get(0);
            config = context.getActionConfig();
            assertTrue("Action count to task 3", actions.size() == 2 && config.getProperty("complete.activity.id").equals("ID27")
                    && (config.getProperty("complete.task.action").equals("first-action") || config.getProperty("complete.task.action").equals("second-action")));

            Set<String> completeTaskIds = new HashSet<>();
            for (ActionContext actionContext : actions) {
                context = (SimpleActionContext) actionContext;
                config = context.getActionConfig();
                if (!completeTaskIds.contains(config.getProperty("complete.task.id"))) {
                    getProcessService("person4").completeTask(new RdbmsId(config.getProperty("complete.task.id")), null,
                            config.getProperty("complete.task.action"));
                    completeTaskIds.add(config.getProperty("complete.task.id"));
                }
            }

            // Проверка задачи группе AllPersons
            personActionService = getActionService("person1");
            actions = personActionService.getActions(attachment.getId());
            context = (SimpleActionContext) actions.get(0);
            config = context.getActionConfig();
            assertTrue("Action count to AllPersons group", actions.size() == 1 && config.getProperty("complete.activity.id").equals("ID95"));

            for (ActionContext actionContext : actions) {
                context = (SimpleActionContext) actionContext;
                config = context.getActionConfig();
                getProcessService("person1").completeTask(new RdbmsId(config.getProperty("complete.task.id")), null,
                        config.getProperty("complete.task.action"));
            }

            // Проверка Изменения вложения и создания новго из процесса
            attachment = getCrudService().find(attachment.getId());
            assertTrue("Change domain object from process", attachment.getString("test_text").startsWith("Изменен в процессе"));
            String testText = attachment.getString("test_text");
            String createdId = testText.substring(testText.length() - 16);
            DomainObject createdDomainObject = getCrudService().find(new RdbmsId(createdId));
            assertTrue("Create domain object from process", createdDomainObject.getString("test_text").equals("Создан в процессе"));

            // Получение всех задач пользователя по документу и их завершение с
            // определенным результатом
            // Для руны надо подождать минуту, чтоб успели синхронизироватся задачи
            //Thread.sleep(60000);
            tasks = processService.getUserDomainObjectTasks(attachment.getId());
            log("Find " + tasks.size() + " tasks");
            for (DomainObject task : tasks) {
                if ("ID35".equals(task.getString("ActivityId"))) {
                    // Получаем все доступные действия
                    String taskActions = task.getString("Actions");
                    log("All actions = " + taskActions);
                    processService.completeTask(task.getId(), null, "YES");
                    log("Complete " + task.getId());
                }
            }

            // Проверка Изменения вложения по выбранному действию
            attachment = getCrudService().find(attachment.getId());
            assertTrue("Exclusive Gateway", attachment.getString("test_text").equals("YES"));

            // Получение задачи в подпроцессе и завершение под person 1
            // Для руны надо подождать минуту, чтоб успели синхронизироватся задачи
            //Thread.sleep(60000);
            tasks = getProcessService("person1").getUserDomainObjectTasks(attachment.getId());
            log("Find " + tasks.size() + " tasks");
            assertTrue("MultyTask for static. person1", tasks.size() == 1);

            // Завершаем задачу у person1, у person2 задача должна остатся
            getProcessService("person1").completeTask(tasks.get(0).getId(), null, null);

            // Получение задачи, по подпроцессу задачи быть не должно, задача
            // должна быть только по следующей активнности
            // Для руны надо подождать минуту, чтоб успели синхронизироватся задачи
            //Thread.sleep(60000);
            tasks = getProcessService("person3").getUserDomainObjectTasks(attachment.getId());
            log("Find " + tasks.size() + " tasks");
            assertTrue("Wait tasks from subprocess", tasks.size() == 0);

            // Завершаем задачу у person2
            tasks = getProcessService("person2").getUserDomainObjectTasks(attachment.getId());
            log("Find " + tasks.size() + " tasks");
            assertTrue("MultyTask for static. person2", tasks.size() == 1);
            // Завершаем задачу у person2, должна создастся задача у person3 и person4
            getProcessService("person2").completeTask(tasks.get(0).getId(), null, null);

            // Динамически назначаемые задачи person3 и person4
            // Получение задачи в подпроцессе и завершение под person 3
            // Для руны надо подождать минуту, чтоб успели синхронизироватся задачи
            //Thread.sleep(60000);
            tasks = getProcessService("person3").getUserDomainObjectTasks(attachment.getId());
            log("Find " + tasks.size() + " tasks");
            assertTrue("MultyTask for dynamic. person3", tasks.size() == 1);

            // Завершаем задачу у person3, у person4 задача должна остатся
            getProcessService("person3").completeTask(tasks.get(0).getId(), null, null);

            // Получение задачи у person5, это действие идет следующим за мультизадачей с динамическими исполнителями
            // задач быть не должно
            // Для руны надо подождать минуту, чтоб успели синхронизироватся задачи
            //Thread.sleep(60000);
            tasks = getProcessService("person5").getUserDomainObjectTasks(attachment.getId());
            log("Find " + tasks.size() + " tasks");
            assertTrue("Wait tasks from subprocess", tasks.size() == 0);

            // Завершаем задачу у person4
            tasks = getProcessService("person4").getUserDomainObjectTasks(attachment.getId());
            log("Find " + tasks.size() + " tasks");
            assertTrue("MultyTask for dynamic. person4", tasks.size() == 1);
            // Завершаем задачу у person2, должна создастся задача у person3 и person4
            getProcessService("person4").completeTask(tasks.get(0).getId(), null, null);

            // Для руны надо подождать минуту, чтоб успели синхронизироватся задачи
            //Thread.sleep(60000);
            // Должна появится задача у person5
            tasks = getProcessService("person5").getUserDomainObjectTasks(attachment.getId());
            assertTrue("Check task. person5", tasks.size() == 1);
            // Завершаем ее
            getProcessService("person5").completeTask(tasks.get(0).getId(), null, null);

            // Проверка получения данных запросом
            attachment = getCrudService().find(attachment.getId());
            testText = attachment.getString("test_text");
            String personId = testText.substring(testText.length() - 16);
            DomainObject personDomObj =  getCrudService().find(new RdbmsId(personId));
            assertTrue("Check find by query", personDomObj.getString("login").equals("person1"));

            // Удаление процесса
            //getProcessService("admin").undeployProcess("test-1", true);

        } finally {

            if (hasError) {
                log("Test filed");
            }else{
                log("Test complete");
            }
            writeLog();
        }
    }

    private DomainObject getEmployee(String employeeName) throws NamingException {
        DomainObject result = null;
        IdentifiableObjectCollection collection =
                getCollectionService().findCollectionByQuery("select t.id from person t where t.login = '" + employeeName
                        + "'");
        if (collection.size() > 0) {
            result = getCrudService().find(collection.getId(0));
        }
        return result;
    }

    private CollectionsService.Remote getCollectionService() throws NamingException {
        return getService("CollectionsServiceImpl", CollectionsService.Remote.class);
    }

    private CrudService.Remote getCrudService() throws NamingException {
        return getService("CrudServiceImpl", CrudService.Remote.class);
    }

    private ActionService.Remote getActionService(String login) throws NamingException {
        return getService("ActionServiceImpl", ActionService.Remote.class, login, "admin");
    }

    private ProcessService.Remote getProcessService(String login) throws NamingException {
        return getService("ProcessService", ProcessService.Remote.class, login, "admin");
    }

    private PersonManagementService.Remote getPersonManagementService() throws NamingException {
        PersonManagementService.Remote personService = getService(
                "PersonManagementService", PersonManagementService.Remote.class);
        return personService;
    }
}
