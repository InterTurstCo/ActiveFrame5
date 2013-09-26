package ru.intertrust.cm.remoteclient.management;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
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

		String command = getCommandLine().getOptionValue("command");

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
					for (int i = 0; i < getCommandLine().getArgs().length; i++) {
						arguments[i] = getCommandLine().getArgs()[i];
					}
					exit = true;
				}

				if (command.equals("create-person")) {
					DomainObject person = service.createDomainObject("Person");
					person.setString("Login", arguments[0]);
					person.setString("FirstName", arguments[1]);
					person.setString("LastName", arguments[2]);
					person.setString("EMail", arguments[3]);
					service.save(person);
				} else if (command.equals("set-passsword")) {

				} else if (command.equals("create-group")) {

				} else if (command.equals("add-person-to-group")) {

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
			}
		}

		writeLog();
	}

}
