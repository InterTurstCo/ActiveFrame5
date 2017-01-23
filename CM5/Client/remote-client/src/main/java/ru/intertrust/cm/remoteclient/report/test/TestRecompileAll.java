package ru.intertrust.cm.remoteclient.report.test;

import ru.intertrust.cm.core.business.api.ReportServiceAdmin;
import ru.intertrust.cm.remoteclient.ClientBase;

public class TestRecompileAll extends ClientBase{
    private ReportServiceAdmin reportServiceAdmin;

    public static void main(String[] args) {
        try {
            TestRecompileAll test = new TestRecompileAll();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void execute(String[] args) throws Exception {
        try {
            super.execute(args);

            reportServiceAdmin = (ReportServiceAdmin) getService("ReportServiceAdmin", ReportServiceAdmin.Remote.class);
            reportServiceAdmin.recompileAll();
            
            log("Test complete");
        } finally {
            writeLog();
        }
    }    
}
