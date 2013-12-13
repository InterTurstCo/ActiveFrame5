package ru.intertrust.cm.performance.dataset.client;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.SortCriterion;
import ru.intertrust.cm.core.business.api.dto.SortCriterion.Order;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.performance.dataset.DatasetGenerationService;
/**
 *
 * @author erentsov
 *
 */
public class DatasetGenerationServiceClient {

    /**
     * @param args
     * @throws NamingException
     * @throws ParseException
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        String[] argNames = new String[]{"address", "user", "password", "log"};
        Options options = new Options();
        for(String argName : argNames){
            Option option = new Option(argName, true, null);
            if(!argName.equals("log")){
                option.setRequired(true);
            } else{
                option.setType(Boolean.class);
            }
            options.addOption(option);
        }

        CommandLineParser parser = new GnuParser();

        try{
            CommandLine cmd = parser.parse(options, args);
            String address = cmd.getOptionValue("address");
            String user = cmd.getOptionValue("user");
            String password = cmd.getOptionValue("password");
            Boolean log = (Boolean) cmd.getParsedOptionValue("log");
            boolean isLog = log != null ? log : false;
            String xml = cmd.getArgs().length == 0 ? null : cmd.getArgs()[0];
            if(xml == null) return;

            Files.readAllBytes(FileSystems.getDefault().getPath(xml));

            Properties jndiProps = new Properties();
            jndiProps.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
            jndiProps.put(Context.PROVIDER_URL,"remote://" + address);
            jndiProps.put("jboss.naming.client.ejb.context", true);
            jndiProps.put("jboss.naming.client.connect.options.org.xnio.Options.SASL_POLICY_NOPLAINTEXT", "false");
            jndiProps.put(Context.SECURITY_PRINCIPAL, user);
            jndiProps.put(Context.SECURITY_CREDENTIALS, password);
            jndiProps.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");

            Context ctx = new InitialContext(jndiProps);

            DatasetGenerationService.Remote service = (DatasetGenerationService.Remote)
                ctx.lookup(
                    "java:cm-sochi/web-app/DatasetGenerationServiceImpl!ru.intertrust.cm.performance.dataset.DatasetGenerationService$Remote"
            );

            CrudService.Remote crudService = (CrudService.Remote)
                    ctx.lookup(
                        "java:cm-sochi/web-app/CrudServiceImpl!ru.intertrust.cm.core.business.api.CrudService$Remote"
                );

            CollectionsService.Remote collectionsService = (CollectionsService.Remote)
                    ctx.lookup(
                        "java:cm-sochi/web-app/CollectionsServiceImpl!ru.intertrust.cm.core.business.api.CollectionsService$Remote"
                );

            String query = "select id from authentication_info ai where user_uid='admin'";
            collectionsService.findCollectionByQuery("select id from authentication_info ai where user_uid='admin'");
            
            IdentifiableObjectCollection identifiableObjectCollection = collectionsService.findCollectionByQuery(query);        
            
            System.out.print("Found collection1 : "  + identifiableObjectCollection);

          
            SortOrder sortOrder = new SortOrder();
            sortOrder.add(new SortCriterion("id", Order.ASCENDING));
            List<Filter> filterValues = new ArrayList<Filter>();
            Filter filter = new Filter();
            filter.setFilter("byDepartmentNames");
            List<Value> departmentNames = new ArrayList<Value>();
            departmentNames.add(new StringValue("department1"));
            departmentNames.add(new StringValue("department2"));

            filter.addMultiCriterion(0, departmentNames);
            filterValues.add(filter);

            IdentifiableObjectCollection objectCollection =
                    collectionsService.findCollection("GroupByName", sortOrder, filterValues);
            
            System.out.print("Found collection2 : " + objectCollection);


            IdentifiableObjectCollection countryObjectCollection =
                    collectionsService.findCollection("Countries", sortOrder, filterValues);
            
            System.out.print("Found collection2 : " + countryObjectCollection);

//            Id statusId = new RdbmsId(domainObjectTypeIdCache.getId("Status"), 1);
            
            Id statusId = new RdbmsId(31, 2);

            DomainObject personDomainObject = createPersonDomainObject(statusId);

            DomainObject savedPersonObject = crudService.save(personDomainObject);
            System.out.println("Saved object : " + savedPersonObject);

            DomainObject organizationDomainObject = createOrganizationDomainObject(statusId);

            DomainObject savedOrganizationObject = crudService.save(organizationDomainObject);
            System.out.println("Saved object : " + savedOrganizationObject);

            DomainObject departmentDomainObject = createDepartmentDomainObject(statusId, savedOrganizationObject);

            DomainObject savedDepartmentObject = crudService.save(departmentDomainObject);
            System.out.println("Saved object : " + savedDepartmentObject);

            DomainObject employeeDomainObject = createEmployeeDomainObject(statusId, savedDepartmentObject);

            DomainObject savedEmployeeObject = crudService.save(employeeDomainObject);
            System.out.println("Saved object : " + savedEmployeeObject);

            
            byte[] data = Files.readAllBytes(FileSystems.getDefault().getPath(xml));

//            String result = service.execute(data);
//            System.out.println(result);
//            if(isLog){
//                Path logPath = FileSystems.getDefault().getPath("log.txt");
//                Files.deleteIfExists(logPath);
//                Files.createFile(logPath);
//                Files.write(logPath, result.getBytes());
//            }
        } catch(ParseException exception){
            System.out.println(exception.getMessage());
        }  catch (NamingException e) {
            e.printStackTrace();
        }
    }
    
    private static GenericDomainObject createEmployeeDomainObject(Id statusId, DomainObject savedDepartmentDomainObject) {
        GenericDomainObject employeeDomainObject = new GenericDomainObject();
        employeeDomainObject.setCreatedDate(new Date());
        employeeDomainObject.setModifiedDate(new Date());
        employeeDomainObject.setTypeName("Employee");
        employeeDomainObject.setString("Name", "Employee " + new Date());
        employeeDomainObject.setString("Position", "Position");
        employeeDomainObject.setStatus(statusId);
        employeeDomainObject.setReference("Department", savedDepartmentDomainObject.getId());
        return employeeDomainObject;
    }

    private static GenericDomainObject createDepartmentDomainObject(Id statusId, DomainObject savedOrganizationObject) {
        GenericDomainObject departmentDomainObject = new GenericDomainObject();
        departmentDomainObject.setCreatedDate(new Date());
        departmentDomainObject.setModifiedDate(new Date());
        departmentDomainObject.setTypeName("Department");
        departmentDomainObject.setString("Name", "department1");
        departmentDomainObject.setStatus(statusId);
        departmentDomainObject.setReference("Organization", savedOrganizationObject.getId());
        return departmentDomainObject;
    }

    private static GenericDomainObject createPersonDomainObject(Id statusId) {
        GenericDomainObject personDomainObject = new GenericDomainObject();
        personDomainObject.setCreatedDate(new Date());
        personDomainObject.setModifiedDate(new Date());
        personDomainObject.setTypeName("Person");
        personDomainObject.setString("Login", "login " + new Date());
        personDomainObject.setStatus(statusId);
        return personDomainObject;
    }

    private static GenericDomainObject createStatusDomainObject(String statusName) {
        GenericDomainObject personDomainObject = new GenericDomainObject();
        personDomainObject.setCreatedDate(new Date());
        personDomainObject.setModifiedDate(new Date());
        personDomainObject.setTypeName("Status");
        personDomainObject.setString("Name", statusName);
        return personDomainObject;
    }
    
    private static GenericDomainObject createOrganizationDomainObject(Id statusId) {
        GenericDomainObject organizationDomainObject = new GenericDomainObject();
        organizationDomainObject.setCreatedDate(new Date());
        organizationDomainObject.setModifiedDate(new Date());
        organizationDomainObject.setTypeName("Organization");
        organizationDomainObject.setString("Name", "Organization" + new Date());
        organizationDomainObject.setStatus(statusId);
        return organizationDomainObject;
    }

}
