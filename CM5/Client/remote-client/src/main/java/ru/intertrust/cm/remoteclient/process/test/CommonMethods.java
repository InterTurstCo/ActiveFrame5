package ru.intertrust.cm.remoteclient.process.test;

import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.Id;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class CommonMethods {

    public static void deployProcesses(ProcessService.Remote service) throws IOException {
        deployProcess(service, "templates/testInternalDoc/InternalDoc.bpmn", "InternalDoc.bpmn", true);
        deployProcess(service, "templates/testInternalDoc/Negotiation.bpmn", "Negotiation.bpmn", true);
        deployProcess(service, "templates/testInternalDoc/Registration.bpmn", "Registration.bpmn", true);
        deployProcess(service, "templates/testInternalDoc/DocExecution.bpmn", "DocExecution.bpmn", true);
        deployProcess(service, "templates/testInternalDoc/CommissionExecution.bpmn", "CommissionExecution.bpmn", true);
    }

    public static Id deployProcess(ProcessService.Remote service, String processPath, String fileName, boolean deploy) throws IOException {
        return service.saveProcess(() -> newFileInputStream(processPath), fileName, deploy);
    }

    public static Id deployProcess(ProcessService service, String processPath, String fileName, boolean deploy) throws IOException {
        return service.saveProcess(() -> newFileInputStream(processPath), fileName, deploy);
    }

    private static InputStream newFileInputStream(String processPath) {
        try {
            return new FileInputStream(processPath);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
