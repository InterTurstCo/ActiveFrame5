package ru.intertrust.cm.remoteclient.process.test;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.PersonManagementService;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.action.SimpleActionConfig;
import ru.intertrust.cm.core.gui.api.server.ActionService;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionContext;
import ru.intertrust.cm.remoteclient.ClientBase;

import javax.naming.NamingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static ru.intertrust.cm.remoteclient.process.test.CommonMethods.deployProcess;

/**
 * Тестовый клиент к подсистеме процессов.
 *
 * @author larin
 */
public class TestProcess extends ClientBase {

    public static void main(String[] args) throws InterruptedException {
        try {
            TestProcess test = new TestProcess();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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

            final ProcessService processService = getProcessService("admin");
            Id defId = deployProcess(processService, "Client/remote-client/templates/TestSimpleProcess.bpmn",
                    "SimpleProcess.bpmn", true);

            // Создание документа, который НЕ будет прикреплен к процессу
            DomainObject attachmentNotInProcess = getCrudService()
                    .createDomainObject("test_process_attachment");
            attachmentNotInProcess.setString("test_text", "Создание.");
            attachmentNotInProcess.setLong("test_long", 10L);
            attachmentNotInProcess.setDecimal("test_decimal",
                    new BigDecimal(10));
            attachmentNotInProcess.setTimestamp("test_date", new Date());
            // attachmentNotInProcess.setReference("person", new
            // RdbmsId("person|1"));
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
            getProcessService("person2").startProcess("testSimpleProcess", attachment.getId(), null);

            //Получение задачь у пользователя 5
            personActionService = getActionService("person5");
            actions = personActionService.getActions(attachment.getId());
            SimpleActionContext context = (SimpleActionContext) actions.get(0);
            SimpleActionConfig config = context.getActionConfig();
            assertTrue("Action count to task 1", actions.size() == 1 && config.getProperty("complete.activity.id").equals("usertask1"));

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
            List<DomainObject> tasks = getProcessService("admin").getUserDomainObjectTasks(attachmentNotInProcess
                    .getId());
            log("Find " + tasks.size()
                    + " tasks for noattached to process document");
            assertTrue("Find all to not attached document tasks", tasks.size() == 0);

            //Получение задачь у пользователя 2, автора
            personActionService = getActionService("person2");
            actions = personActionService.getActions(attachment.getId());
            context = (SimpleActionContext) actions.get(0);
            config = context.getActionConfig();
            assertTrue("Action count to task 2", actions.size() == 1 && config.getProperty("complete.activity.id").equals("usertask2"));

            // Получение всех задач по документу и их завершение
            for (ActionContext actionContext : actions) {
                context = (SimpleActionContext) actionContext;
                config = context.getActionConfig();
                getProcessService("person2").completeTask(new RdbmsId(config.getProperty("complete.task.id")), null,
                        config.getProperty("complete.task.action"));
            }

            //Получение задачь у пользователя 3, подписывающего
            personActionService = getActionService("person3");
            actions = personActionService.getActions(attachment.getId());
            context = (SimpleActionContext) actions.get(0);
            config = context.getActionConfig();
            assertTrue("Action count to task 3", actions.size() == 1 && config.getProperty("complete.activity.id").equals("usertask6")
                    && config.getProperty("complete.task.action").equals("first-action"));

            //Получение задачь у пользователя 4, регистратора
            personActionService = getActionService("person4");
            actions = personActionService.getActions(attachment.getId());
            context = (SimpleActionContext) actions.get(0);
            config = context.getActionConfig();
            assertTrue("Action count to task 3", actions.size() == 1 && config.getProperty("complete.activity.id").equals("usertask6")
                    && config.getProperty("complete.task.action").equals("second-action"));

            for (ActionContext actionContext : actions) {
                context = (SimpleActionContext) actionContext;
                config = context.getActionConfig();
                getProcessService("person4").completeTask(new RdbmsId(config.getProperty("complete.task.id")), null,
                        config.getProperty("complete.task.action"));
            }

            // Получение всех задач пользователя по документу и их завершение с
            // определенным результатом
            tasks = getProcessService("admin").getUserDomainObjectTasks(attachment.getId());
            log("Find " + tasks.size() + " tasks");
            for (DomainObject task : tasks) {
                if ("usertask3".equals(task.getString("ActivityId"))) {
                    // Получаем все доступные действия
                    String taskActions = task.getString("Actions");
                    log("All actions = " + taskActions);
                    getProcessService("admin").completeTask(task.getId(), null, "YES");
                    log("Complete " + task.getId());
                }
            }

            // Получение задачи в подпроцессе и завершение одной из них
            tasks = getProcessService("admin").getUserDomainObjectTasks(attachment.getId());
            log("Find " + tasks.size() + " tasks");
            for (DomainObject task : tasks) {
                if ("usertask4".equals(task.getString("ActivityId"))) {
                    getProcessService("admin").completeTask(task.getId(), null, null);
                    log("Complete " + task.getId());
                    // Завершаем только одну из двух
                    break;
                }
            }

            // Получение задачи, по подпроцессу задачи быть не должно, задача
            // должна быть только по следующей активнности
            tasks = getProcessService("admin").getUserDomainObjectTasks(attachment.getId());
            log("Find " + tasks.size() + " tasks");
            assertTrue("Delete subprocess task", tasks.size() == 1);
            for (DomainObject task : tasks) {
                if ("usertask5".equals(task.getString("ActivityId"))) {
                    getProcessService("admin").completeTask(task.getId(), null, null);
                    log("Complete " + task.getId());
                    break;
                }
            }

            //Проверка наличия уведомления у пользователя person1
            List<Value> params = new ArrayList<>();
            params.add(new ReferenceValue(attachment.getId()));

            //Пауза чтоб отправились асинхронные уведомления
            Thread.sleep(10000);

            IdentifiableObjectCollection collection = getCollectionService().findCollectionByQuery("select n.id from notification n where n.context_object = {0}", params);
            assertTrue("Send message", collection.size() == 1);
            //Корректность текста
            DomainObject notification = getCrudService().find(collection.get(0).getId());
            assertTrue("Send message text", notification.getString("body").indexOf(attachment.getId().toStringRepresentation()) > 0);


            //Проверка на то что попали и ждем в signalintermediatecatchevent1
            attachment = getCrudService().find(attachment.getId());
            assertFalse("White in signalintermediatecatchevent1 1", attachment.getString("test_text").endsWith("Получили уведомление."));

            //отправка правильного сообщения
            getProcessService("admin").sendProcessMessage("testSimpleProcess", attachment.getId(), "START_BY_SIGNAL", null);

            //Строка должна изменится
            attachment = getCrudService().find(attachment.getId());
            assertTrue("Go then signalintermediatecatchevent1", attachment.getString("test_text").endsWith("Получили уведомление."));

            //отправка еще одного правильного сообщения
            getProcessService("admin").sendProcessMessage("testSimpleProcess", attachment.getId(), "START_BY_SIGNAL_2", null);

            //Строка должна еще раз изменится
            attachment = getCrudService().find(attachment.getId());
            assertTrue("Go then scripttask9", attachment.getString("test_text").endsWith("Получили уведомление 2."));

            //проверка event gateway
            getProcessService("admin").sendProcessMessage("testSimpleProcess", attachment.getId(), "START_BY_SIGNAL_3", null);

            //Строка должна еще раз изменится
            attachment = getCrudService().find(attachment.getId());
            assertTrue("Check eventgateway1", attachment.getString("test_text").endsWith("Получили уведомление 3."));

            //проверка отправки сигнала
            getProcessService("admin").sendProcessSignal("START_BY_SIGNAL_5");

            //Строка должна еще раз изменится
            attachment = getCrudService().find(attachment.getId());
            assertTrue("Check signalintermediatecatchevent1", attachment.getString("test_text").endsWith("Получили уведомление 5."));

            //Проверяем что таймер еще не сработал
            attachment = getCrudService().find(attachment.getId());
            assertTrue("Check timer start", attachment.getString("test_text").endsWith("Получили уведомление 5."));

            //Спим более минуты, должен сработать таймер
            Thread.sleep(120000);
            attachment = getCrudService().find(attachment.getId());
            assertTrue("Check timer end", attachment.getString("test_text").endsWith("Сработал таймер."));

            personActionService = getActionService("person5");
            actions = personActionService.getActions(attachment.getId());

            // Получение всех задач по документу и их завершение
            for (ActionContext actionContext : actions) {
                context = (SimpleActionContext) actionContext;
                config = context.getActionConfig();
                getProcessService("person5").completeTask(new RdbmsId(config.getProperty("complete.task.id")), null,
                        config.getProperty("complete.task.action"));
            }

            //Проверяем что таймер еще не сработал
            attachment = getCrudService().find(attachment.getId());
            assertTrue("Check timer 2 start", attachment.getString("test_text").endsWith("Сработал таймер."));

            //Спим более минуты, должен сработать второй таймер
            Thread.sleep(120000);

            //Проверяем таймер повторно
            attachment = getCrudService().find(attachment.getId());
            assertTrue("Check timer 2 end", attachment.getString("test_text").endsWith("Сработал таймер 2."));

            //Получаем сервис чтоб удалить под admin
            getProcessService("admin");
            getCrudService().delete(attachmentNotInProcess.getId());

            if (hasError){
                log("Test FAILURE");
            }else{
                log("Test COMPLETE");
            }
        } finally {
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

    private ActionService getActionService(String login) throws NamingException {
        return getService("ActionServiceImpl", ActionService.Remote.class, login, "admin");
    }

    private ProcessService getProcessService(String login) throws NamingException {
        return getService("ProcessService", ProcessService.Remote.class, login, "admin");
    }

    private PersonManagementService.Remote getPersonManagementService() throws NamingException {
        return getService("PersonManagementService", PersonManagementService.Remote.class);
    }
}
