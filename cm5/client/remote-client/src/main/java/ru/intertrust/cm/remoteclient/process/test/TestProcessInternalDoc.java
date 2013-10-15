package ru.intertrust.cm.remoteclient.process.test;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Time;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.naming.NamingException;

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
public class TestProcessInternalDoc extends ClientBase {
    private Hashtable<String, Id> personIds = new Hashtable<String, Id>();
    private Hashtable<String, Id> additionalPersonIds = new Hashtable<String, Id>();
    public static void main(String[] args) {
        try {
            TestProcessInternalDoc test = new TestProcessInternalDoc();
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


            CrudService crudService = getCrudService();
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
            


            byte[] processDef = getProcessAsByteArray("templates/testInternalDoc/InternalDoc.bpmn");
            String defId = service.deployProcess(processDef,
                    "InternalDoc.bpmn");

            processDef = getProcessAsByteArray("templates/testInternalDoc/Negotiation.bpmn");
            defId = service.deployProcess(processDef,
                    "Negotiation.bpmn");
            
            // Создание документа, который будет прикреплен к процессу
            DomainObject attachment = crudService
                    .createDomainObject("Internal_Document");
            attachment.setString("Name", "Тестовый документ");
            attachment.setString("Status", "Черновик");
            attachment.setString("ReturnOnReject", "YES");
            attachment.setLong("Stage", 0L);
            attachment = crudService.save(attachment);
            Id personId = null;
            //Создание карточек согласования
            for (int i=1;i<10;i++){

            	//Создание карточки согласования
                DomainObject negotiationCard = crudService
                        .createDomainObject("Negotiation_Card");
                negotiationCard.setString("Name", "Карточка согласования #"+String.valueOf(i));
                negotiationCard.setString("Status", "Draft");
                negotiationCard.setReference("Parent_Document", attachment);
                
                personId = personService.getPersonId("Negotiator" + i);
                if (personId == null) {
                	DomainObject person =	createEmployee();
                	personId =person.getId();
                }
                personIds.put("Negotiator" + i, personId);
                negotiationCard.setReference("Negotiator",personId );
                negotiationCard =  crudService.save(negotiationCard);
                for (int j=1;j<4;j++){
                	 DomainObject negotiationCardChild = crudService
                             .createDomainObject("Negotiation_Card");
                	 negotiationCardChild.setString("Name", "Дочерняя карточка согласования #"+String.valueOf(i));
                	 negotiationCardChild.setString("Status", "Draft");
                	 negotiationCardChild.setReference("Parent_Document", attachment);
                	 negotiationCardChild.setReference("Add_Negotiation_Card", negotiationCard);
                	 personId = personService.getPersonId(i+"SubNegatiator" + j);
                     if (personId == null) {
                     	DomainObject person =	createEmployee();
                     	personId =person.getId();
                     }
                     additionalPersonIds.put(i+"SubNegatiator" + j, personId);
                     negotiationCardChild.setReference("Negotiator", personId);
                     negotiationCardChild =  crudService.save(negotiationCardChild);
                	
                }
            }
            
            // Запуск процесса
            service.startProcess("InternalDocument", attachment.getId(), null);

            Iterator<Entry<String, Id>> iter = personIds.entrySet().iterator();
        	
            while (iter.hasNext()){
            	Entry<String, Id> entry = iter.next();
            	personId = entry.getValue();
            	// Получение всех задач пользователя и их завершение
                List<DomainObject> tasks = service.getUserTasks(personId);
                log("Find " + tasks.size() + " tasks for user: " +entry.getKey());
                for (DomainObject task : tasks) {
                    if ("askNegotiate".equals(task.getString("ActivityId"))) {
                        // Получаем все доступные действия
                        String actions = task.getString("Actions");
                        log("All Actions = " + actions);
                        service.completeTask(task.getId(), null, "ADDITIONAL_NEGOTIATION");
                        log("Complete " + task.getId());
                    }
                }
            }
            
            iter = additionalPersonIds.entrySet().iterator();
        	
            while (iter.hasNext()){
            	Entry<String, Id> entry = iter.next();
            	personId = entry.getValue();
            	// Получение всех задач пользователя и их завершение
                List<DomainObject> tasks = service.getUserTasks(personId);
                log("Find " + tasks.size() + " tasks for user: " +entry.getKey());
                for (DomainObject task : tasks) {
                    if ("askNegotiate".equals(task.getString("ActivityId"))) {
                        // Получаем все доступные действия
                        String actions = task.getString("Actions");
                        log("All Actions = " + actions);
                        service.completeTask(task.getId(), null, "AGREE");
                        log("Complete " + task.getId());
                    }
                }
            }
            
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
    private DomainObject createEmployee() throws NamingException{
    	CrudService crudService = getCrudService();
    	Date date= new java.util.Date();
    	String unicStr = String.valueOf(date.getTime());
    	//Создание согласующего
        DomainObject employee = crudService
                .createDomainObject("Employee");
        employee.setReference("Department", createDepartment());
        employee.setString("Name", "Согласующий #"+unicStr);
        employee.setString("Position", "Должность #"+unicStr);
        employee.setString("Phone",unicStr);
        employee =  crudService.save(employee); 
        return employee;
    }
    
    private CrudService getCrudService() throws NamingException{
        CrudService.Remote crudService = (CrudService.Remote) getService(
                "CrudServiceImpl", CrudService.Remote.class);
        return crudService;
    }
    
    private DomainObject createDepartment() throws NamingException{
        //Создаем департамент
    	CrudService crudService = getCrudService();
        DomainObject department = crudService.createDomainObject("Department");
        department.setString("Name", "Test department");
        department = crudService.save(department);
        return department;
    }
}
