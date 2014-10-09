package ru.intertrust.cm.remoteclient.process.test;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.naming.NamingException;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.PersonManagementService;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.ActionService;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.CompleteTaskActionContext;
import ru.intertrust.cm.core.model.ProcessException;
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

            byte[] processDef = getProcessAsByteArray("templates/TestSimpleProcess.bpmn");
            String defId = getProcessService("admin").deployProcess(processDef,
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
            attachment.setReference("author", getEmployee("person2"));
            attachment.setReference("signer", getEmployee("person3"));
            attachment.setReference("registrator", getEmployee("person4"));
            attachment = crudService.save(attachment);

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
            assertTrue("Action count to task 1", actions.size() == 1 && ((CompleteTaskActionContext) actions.get(0)).getActivityId().equals("usertask1"));

            //Попытка завершить задачу другим пользователем, у кого нет задачи
            // Получение всех задач пользователя и их завершение
            for (ActionContext actionContext : actions) {
                CompleteTaskActionContext taskActionContext = (CompleteTaskActionContext) actionContext;
                try{
                    getProcessService("person1").completeTask(taskActionContext.getTaskId(), null, taskActionContext.getTaskAction());
                    assertTrue("Complete not person task", false);
                }catch(Exception ignoreEx){
                    //Правильная ошибка
                }
            }
            
            // Получение всех задач пользователя и их завершение
            for (ActionContext actionContext : actions) {
                CompleteTaskActionContext taskActionContext = (CompleteTaskActionContext) actionContext;
                getProcessService("person5").completeTask(taskActionContext.getTaskId(), null, taskActionContext.getTaskAction());
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
            assertTrue("Action count to task 2", actions.size() == 1 && ((CompleteTaskActionContext) actions.get(0)).getActivityId().equals("usertask2"));

            // Получение всех задач по документу и их завершение
            for (ActionContext actionContext : actions) {
                CompleteTaskActionContext taskActionContext = (CompleteTaskActionContext) actionContext;
                getProcessService("person2").completeTask(taskActionContext.getTaskId(), null, taskActionContext.getTaskAction());
            }

            //Получение задачь у пользователя 3, подписывающего
            personActionService = getActionService("person3");
            actions = personActionService.getActions(attachment.getId());
            assertTrue("Action count to task 3", actions.size() == 1 && ((CompleteTaskActionContext) actions.get(0)).getActivityId().equals("usertask6")
                    && ((CompleteTaskActionContext) actions.get(0)).getTaskAction().equals("first-action"));

            //Получение задачь у пользователя 4, регистратора
            personActionService = getActionService("person4");
            actions = personActionService.getActions(attachment.getId());
            assertTrue("Action count to task 3", actions.size() == 1 && ((CompleteTaskActionContext) actions.get(0)).getActivityId().equals("usertask6")
                    && ((CompleteTaskActionContext) actions.get(0)).getTaskAction().equals("second-action"));            
            
            for (ActionContext actionContext : actions) {
                CompleteTaskActionContext taskActionContext = (CompleteTaskActionContext) actionContext;
                getProcessService("person4").completeTask(taskActionContext.getTaskId(), null, taskActionContext.getTaskAction());
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

            crudService.delete(attachmentNotInProcess.getId());

            log("Test complete");
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
        return (CollectionsService.Remote) getService("CollectionsServiceImpl", CollectionsService.Remote.class);
    }

    private CrudService.Remote getCrudService() throws NamingException {
        return (CrudService.Remote) getService("CrudServiceImpl", CrudService.Remote.class);
    }

    private ActionService getActionService(String login) throws NamingException {
        ActionService actionService =
                (ActionService) getService("ActionServiceImpl", ActionService.Remote.class, login, "admin");
        return actionService;
    }

    private ProcessService getProcessService(String login) throws NamingException {
        ProcessService service = (ProcessService) getService("ProcessService", ProcessService.Remote.class, login, "admin");
        return service;
    }

}
