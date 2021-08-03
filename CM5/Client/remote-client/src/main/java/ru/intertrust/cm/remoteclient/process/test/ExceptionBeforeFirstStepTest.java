package ru.intertrust.cm.remoteclient.process.test;

import org.apache.commons.io.IOUtils;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.PersonManagementService;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.gui.api.server.ActionService;
import ru.intertrust.cm.remoteclient.ClientBase;

import javax.naming.NamingException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Date;

import static ru.intertrust.cm.remoteclient.process.test.CommonMethods.deployProcess;

/**
 * This test deploys process, checks the state of the process and then completes it.
 */
public class ExceptionBeforeFirstStepTest extends ClientBase {

    public static void main(String[] args) throws Exception {
        ExceptionBeforeFirstStepTest oneStepProcessTest = new ExceptionBeforeFirstStepTest();
        oneStepProcessTest.execute(args);
    }

    @Override
    protected void execute(String[] args) throws Exception {
        super.execute(args);

        // Создаем персону
        Id adminPerson = getPersonManagementService().getPersonId("admin");
        if (adminPerson == null) {
            createPerson();
        }

        final ProcessService.Remote processService = getProcessService("admin");
        Id defId = deployProcess(processService, "Client/remote-client/templates/exception-before-first-step.bpmn",
                "exception-before-first-step.bpmn", true);

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

        // Запуск процесса
        try {
            String id = getProcessService("person2").startProcess("exception-before-first-step", attachment.getId(), null);
        } catch (Exception ex) {
            // exception here. Ignore it
            ex.printStackTrace();

            // Если происходит ошибка, процесс откатывается... По хорошему, тут бы дописать проверки
        }

    }

    private void printJpg(String id) throws IOException, NamingException {
        File tmp = File.createTempFile("tmp", ".jpg");
        byte[] jpg = getProcessService("person5").getProcessInstanceModel(id);
        try (InputStream is = new ByteArrayInputStream(jpg);
             OutputStream os = new FileOutputStream(tmp)) {
            IOUtils.copy(is, os);
            System.out.println(tmp.getAbsolutePath());
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

    private void createPerson() throws NamingException {
        DomainObject person = getCrudService().createDomainObject("Person");
        person.setString("Login", "admin");
        person.setString("FirstName", "Администратор");
        person.setString("LastName", "Администраторович");
        person.setString("EMail", "admin@cm5.ru");
        person = getCrudService().save(person);
    }

    private PersonManagementService.Remote getPersonManagementService() throws NamingException {
        return getService("PersonManagementService", PersonManagementService.Remote.class);
    }

    private ProcessService.Remote getProcessService(String login) throws NamingException {
        return getService("ProcessService", ProcessService.Remote.class, login, "admin");
    }

    private CrudService.Remote getCrudService() throws NamingException {
        return getService("CrudServiceImpl", CrudService.Remote.class);
    }

    private CollectionsService.Remote getCollectionService() throws NamingException {
        return getService("CollectionsServiceImpl", CollectionsService.Remote.class);
    }

    private ActionService.Remote getActionService(String login) throws NamingException {
        return getService("ActionServiceImpl", ActionService.Remote.class, login, "admin");
    }

}
