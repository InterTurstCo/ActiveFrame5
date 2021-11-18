package ru.intertrust.cm.remoteclient.process.test;

import javax.naming.NamingException;

import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.remoteclient.ClientBase;

import static ru.intertrust.cm.remoteclient.process.test.CommonMethods.deployProcess;

/**
 * Тестовый клиент к подсистеме процессов.
 * @author larin
 * 
 */
public class DeployProcess extends ClientBase {

    public static void main(String[] args) {
        try {
            DeployProcess test = new DeployProcess();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execute(String[] args) throws Exception {
        try {
            super.execute(args);

            final ProcessService.Remote service = getProcessService("admin");
            Id defId = deployProcess(service,
                    "../../test-module/src/main/resources/workflow/InternalDocTest.bpmn",
                    "InternalDocTest.bpmn", ProcessService.SaveType.ACTIVATE);

            log("Test complete. defId: " + defId);
        } finally {
            writeLog();
        }
    }

    private ProcessService.Remote getProcessService(String login) throws NamingException {
        return getService("ProcessService", ProcessService.Remote.class, login, "admin");
    }

}
