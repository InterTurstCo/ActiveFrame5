package ru.intertrust.cm.remoteclient.process.test;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

import javax.naming.NamingException;

import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.remoteclient.ClientBase;

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

            byte[] processDef = getProcessAsByteArray("../../test-module/src/main/resources/workflow/InternalDocTest.bpmn");
            String defId = getProcessService("admin").deployProcess(processDef,
                    "InternalDocTest.bpmn");

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
            if (stream != null) {
                stream.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    private ProcessService getProcessService(String login) throws NamingException {
        ProcessService service = (ProcessService) getService("ProcessService", ProcessService.Remote.class, login, "admin");
        return service;
    }

}
