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

import static ru.intertrust.cm.remoteclient.process.test.CommonMethods.deployProcesses;

/**
 * Тестовый клиент к подсистеме процессов.
 * @author larin
 * 
 */
public class TestProcessInternalDoc extends ClientBase {
    private Hashtable<String, Id> personIds = new Hashtable<String, Id>();
    private Hashtable<String, Id> additionalPersonIds = new Hashtable<String, Id>();
    private Hashtable<String, Id> examinePersonIds = new Hashtable<String, Id>();
    private Hashtable<String, Id> executorPersonIds = new Hashtable<String, Id>();
    private Hashtable<Integer, DomainObject> resolutionCards = new Hashtable<Integer, DomainObject>();
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

            //Создание статуса черновик
           /* List<DomainObject> allStatuses = crudService.findAll("Status");
            Iterator<DomainObject> iter = allStatuses.iterator();
            DomainObject status = null;
            DomainObject draftStatus = null;
            while (iter.hasNext()){
            	status = iter.next();
            	if ((status.getString("Name").equals("Draft"))){
            		draftStatus = status;
            	}
            }
            if (draftStatus ==null){
        		draftStatus = crudService
                        .createDomainObject("Status");
        		draftStatus.setString("Name", "Draft");
        		draftStatus = crudService.save(draftStatus);
            }*/
           
            // Создание документа, который будет прикреплен к процессу
            DomainObject attachment = crudService
                    .createDomainObject("Internal_Document");
            attachment.setString("Name", "Тестовый документ");
           // attachment.setReference("Status", draftStatus);
            attachment.setString("ReturnOnReject", "YES");
            attachment.setLong("Stage", 0L);
            attachment.setString("RegNum", "InternalDoc111");
            //attachment.setString("ServerState", "Draft");
            attachment = crudService.save(attachment);
            
            
            // Создаем персону
            Id adminPerson = personService.getPersonId("admin");
            if (adminPerson == null) {
                DomainObject person = crudService.createDomainObject("Person");
                person.setString("Login", "admin");
                person.setString("FirstName", "Администратор");
                person.setString("LastName", "Администраторович");
                person.setString("EMail", "admin@cm5.ru");
                person = crudService.save(person);
                adminPerson = person.getId();
            }
            
            // Создаем Автора документа
            Id docAuthor = personService.getPersonId("docAuthor");
            if (docAuthor == null) {
             	DomainObject person =	createEmployee();
                docAuthor = person.getId();
            }
            
            // Создаем Регистранта
            Id registrant = personService.getPersonId("Registrant");
            if (registrant == null) {
             	DomainObject person =	createEmployee();
             	registrant = person.getId();
            }
            
            //Создание карточек рассматривающих
            for (int i=1;i<=5;i++){
            	//Создание карточки согласования
                DomainObject examineCard = crudService
                        .createDomainObject("Examine_Card");
                examineCard.setString("Name", "Карточка рассмотрения #"+String.valueOf(i));
               // examineCard.setReference("Status", draftStatus);
                examineCard.setString("ServerState", "Draft");
                examineCard.setReference("Parent_Document", attachment);
                
                Id examiner = personService.getPersonId("Examiner"+i);
                if (examiner == null) {
                 	DomainObject person =	createEmployee();
                 	examiner = person.getId();
                }
                examineCard.setReference("Examiner", examiner);
                examinePersonIds.put("Examiner" + i, examiner);
                examineCard =  crudService.save(examineCard);
            }
            
            //Создание карточек поручений
            for (int i=1;i<=5;i++){
            	//Создание карточки согласования
                DomainObject resolutionCard = crudService
                        .createDomainObject("Resolution_Card");
                resolutionCard.setString("Name", "Карточка поручения #"+String.valueOf(i));
               // examineCard.setReference("Status", draftStatus);
                resolutionCard.setString("ServerState", "Execution");
                resolutionCard.setReference("Parent_Document", attachment);
                
                Id executor = personService.getPersonId("Executor"+i);
                if (executor == null) {
                 	DomainObject person =	createEmployee();
                 	executor = person.getId();
                }
                resolutionCard.setReference("Executor", executor);
                executorPersonIds.put("Executor" + i, executor);
                resolutionCard =  crudService.save(resolutionCard);
                resolutionCards.put(i, resolutionCard);
            }

            deployProcesses(service);

            attachment.setReference("docAuthor", docAuthor);
            attachment.setReference("Registrant", registrant);
            attachment = crudService.save(attachment);
            Id personId = null;
            //Создание карточек согласования
            for (int i=1;i<=10;i++){

            	//Создание карточки согласования
                DomainObject negotiationCard = crudService
                        .createDomainObject("Negotiation_Card");
                negotiationCard.setString("Name", "Карточка согласования #"+String.valueOf(i));
                //negotiationCard.setReference("Status", draftStatus);
                negotiationCard.setString("ServerState", "Draft");
                negotiationCard.setReference("Parent_Document", attachment);
                
                
                personId = personService.getPersonId("Negotiator" + i);
                if (personId == null) {
                	DomainObject person =	createEmployee();
                	personId =person.getId();
                }
                personIds.put("Negotiator" + i, personId);
                negotiationCard.setReference("Negotiator",personId );
                negotiationCard =  crudService.save(negotiationCard);
                //negotiationCard.setReference("Add_Negotiation_Card", negotiationCard);
                for (int j=1;j<4;j++){
                	 DomainObject negotiationCardChild = crudService
                             .createDomainObject("Negotiation_Card");
                	 negotiationCardChild.setString("Name", "Дочерняя карточка согласования #"+String.valueOf(i));
                	 //negotiationCardChild.setReference("Status", draftStatus);
                	 negotiationCardChild.setString("ServerState", "Draft");
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
                negotiationCard =  crudService.save(negotiationCard);
            }
            
            // Запуск процесса
            service.startProcess("InternalDocument", attachment.getId(), null);
           
            List<DomainObject> tasks = null;
            Iterator<Entry<String, Id>> iter2 = personIds.entrySet().iterator();
        	
            while (iter2.hasNext()){
            	Entry<String, Id> entry = iter2.next();
            	personId = entry.getValue();
            	// Получение всех задач пользователя и их завершение
                tasks = service.getUserTasks(personId);
                log("Find " + tasks.size() + " tasks for user: " +entry.getKey());
                for (DomainObject task : tasks) {
                    if ("askNegotiate".equals(task.getString("ActivityId"))) {
                        // Получаем все доступные действия
                        String actions = task.getString("Actions");
                        log("All Actions = " + actions);
                        service.completeTask(task.getId(), null, "AGREE");
                        log("Complete askNegotiate task: " + task.getId());
                    }
                }
            }
            
            iter2 = additionalPersonIds.entrySet().iterator();
        	
            while (iter2.hasNext()){
            	Entry<String, Id> entry = iter2.next();
            	personId = entry.getValue();
            	// Получение всех задач пользователя и их завершение
                tasks = service.getUserTasks(personId);
                log("Find " + tasks.size() + " tasks for user: " +entry.getKey());
                for (DomainObject task : tasks) {
                    if ("askNegotiate".equals(task.getString("ActivityId"))) {
                        // Получаем все доступные действия
                        String actions = task.getString("Actions");
                        log("All Actions = " + actions);
                        service.completeTask(task.getId(), null, "AGREE");
                        log("Complete askAdditionalNegotiate task: " + task.getId());
                    }
                }
            }
                     
            
        	// Получение всех задач регистратора и регистрация документа
            tasks = service.getUserTasks(registrant);
            log("Find " + tasks.size() + " tasks for user: " +registrant);
            for (DomainObject task : tasks) {
                if ("askRegistration".equals(task.getString("ActivityId"))) {
                    // Получаем все доступные действия
                    service.completeTask (task.getId(), null,null);
                    log("Complete askRegistration " + task.getId());
                }
            }
            
            iter2 = examinePersonIds.entrySet().iterator();
        	int count = 1;
            while (iter2.hasNext()){
            	Entry<String, Id> entry = iter2.next();
            	personId = entry.getValue();
            	// Получение всех задач пользователя и их завершение
                tasks = service.getUserTasks(personId);
                log("Find " + tasks.size() + " tasks for user: " +entry.getKey());
                for (DomainObject task : tasks) {
                    if ("askExamine".equals(task.getString("ActivityId"))) {
                        // Получаем все доступные действия
                        //String actions = task.getString("Actions");
                       // log("All Actions = " + actions);
                    	service.startProcess("CommissionExecution", resolutionCards.get(count).getId(), null);
                        service.completeTask(task.getId(), null, null);
                        log("Complete askExamine task: " + task.getId());
                        count++;
                    }
                }
            }
            // Запуск процесса
            //service.startProcess("CommissionExecution", attachment.getId(), null);
            
            
        } finally {
            writeLog();
        }
    }

    private DomainObject createEmployee() throws NamingException{
    	CrudService crudService = getCrudService();
    	//Создание согласующего
        DomainObject employee = crudService
                .createDomainObject("Employee");
        employee.setReference("Department", createDepartment());
        employee.setString("Name", "Согласующий #"+getStrTime());
        employee.setString("Position", "Должность #"+getStrTime());
        employee.setString("Phone",getStrTime());
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
        department.setString("Name", "Test department #" + getStrTime());
        department.setReference("Organization", createOrganization());
        department = crudService.save(department);
        return department;
    }
    
    private DomainObject createOrganization() throws NamingException{
        //Создаем организацию
    	CrudService crudService = getCrudService();
        DomainObject organization = crudService.createDomainObject("Organization");
        organization.setString("Name", "Test organization #" + getStrTime());
        organization = crudService.save(organization);
        return organization;
    }
    
    private String getStrTime(){
    	Date date= new java.util.Date();
    	String unicStr = String.valueOf(date.getTime());
    	return unicStr;
    }
    
}
