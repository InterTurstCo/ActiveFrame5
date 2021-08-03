package ru.intertrust.cm.remoteclient.process.test;

import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.Id;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class CommonMethods {

    public static void deployProcesses(ProcessService.Remote service) throws IOException {
        deployProcess(service, "templates/testInternalDoc/InternalDoc.bpmn", "InternalDoc.bpmn", true);
        deployProcess(service, "templates/testInternalDoc/Negotiation.bpmn", "Negotiation.bpmn", true);
        deployProcess(service, "templates/testInternalDoc/Registration.bpmn", "Registration.bpmn", true);
        deployProcess(service, "templates/testInternalDoc/DocExecution.bpmn", "DocExecution.bpmn", true);
        deployProcess(service, "templates/testInternalDoc/CommissionExecution.bpmn", "CommissionExecution.bpmn", true);
    }

    public static Id deployProcess(ProcessService.Remote service, String processPath, String fileName, boolean deploy) throws IOException {
        return service.saveProcess(getProcessAsByteArray(processPath), fileName, deploy);
    }

    public static void undeployProcess(ProcessService.Remote service, Id id) {
        service.undeployProcess(id.toStringRepresentation(), true);
    }

    private static byte[] getProcessAsByteArray(String processPath) throws IOException {
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
            if (stream != null) {
                stream.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

}
