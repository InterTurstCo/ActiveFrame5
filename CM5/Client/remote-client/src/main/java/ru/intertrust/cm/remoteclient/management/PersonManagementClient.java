package ru.intertrust.cm.remoteclient.management;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.remoteclient.ClientBase;

/**
 * Приложение для управления пользователями и группами
 * @author larin
 *
 */
public class PersonManagementClient extends ClientBase {

    public static void main(String[] args) {
        try {
            PersonManagementClient test = new PersonManagementClient();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execute(String[] args) throws Exception {
        super.execute(args, new String[] { "command" });

        CrudService.Remote service = (CrudService.Remote) getService(
                "CrudServiceImpl", CrudService.Remote.class);

        String command = getParamerer("command");

        String[] arguments = new String[10];

        boolean exit = false;

        while (!exit) {
            try {
                if (command == null) {
                    // Интерактивный режим
                    System.out.println("Введите команду и аргументы");

                    BufferedReader commandReader = new BufferedReader(
                            new InputStreamReader(System.in));
                    String interactComands = commandReader.readLine();
                    String[] interactComandArray = interactComands.split(" ");
                    command = interactComandArray[0];
                    for (int i = 1; i < interactComandArray.length; i++) {
                        arguments[i - 1] = interactComandArray[i];
                    }
                } else {
                    // Режим командного файла
                    for (int i = 0; i < getCommandLineArgs().length; i++) {
                        arguments[i] = getCommandLineArgs()[i];
                    }
                    exit = true;
                }

                if (command.equals("create-person")) {
                    DomainObject person = service.createDomainObject("Person");
                    person.setString("Login", arguments[0]);
                    person.setString("FirstName", arguments[1]);
                    person.setString("LastName", arguments[2]);
                    person.setString("EMail", arguments[3]);
                    person = service.save(person);
                    
                    DomainObject authInfo = service.createDomainObject("Authentication_Info");
                    authInfo.setString("User_Uid", arguments[0]);
                    authInfo.setString("password", arguments[4]);
                    authInfo = service.save(authInfo);
                    
                    System.out.println("Create person success. Person id = " + person.getId() + ".");
                } else if (command.equals("set-passsword")) {

                } else if (command.equals("remove-person")) {
                    Map<String, Value> key = new HashMap<String, Value>();
                    key.put("login", new StringValue(arguments[0]));
                    DomainObject person = service.findByUniqueKey("person", key);
                    service.delete(person.getId());
                    System.out.println("Delete person success.");

                } else if (command.equals("create-group")) {

                } else if (command.equals("add-person-to-group")) {
                    DomainObject person = service.createDomainObject("group_member");
                    //TODO
                    person = service.save(person);
                } else if (command.equals("remove-person-from-group")) {

                } else if (command.equals("person-list")) {

                } else if (command.equals("group-list")) {

                } else if (command.equals("exit")) {
                    exit = true;
                } else {
                    System.out
                            .println("Command "
                                    + command
                                    + " not supported. Use create-person, set-passsword, create-group, add-person-to-group, remove-person-from-group, person-list, group-list");
                }
                
            } catch (Exception ex) {
                ex.printStackTrace();
            }finally{
                command = null;
            }
        }

        writeLog();
    }

}
