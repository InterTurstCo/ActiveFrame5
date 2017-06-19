package ru.intertrust.cm.remoteclient.load;

import java.io.File;

import ru.intertrust.cm.core.business.api.ImportDataService;
import ru.intertrust.cm.remoteclient.ClientBase;

public class TestImportData extends ClientBase {
    private ImportDataService.Remote importDataService;

    public static void main(String[] args) {
        try {
            TestImportData test = new TestImportData();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execute(String[] args) throws Exception {
        try {
            super.execute(args);
            importDataService = (ImportDataService.Remote) getService("ImportDataService", ImportDataService.Remote.class);

            /*importDataService.importData(readFile(new File("../../test-module/src/main/resources/importdata/import-notification_text.csv")), null, true);
            
            importDataService.importData(readFile(new File("import-organization.csv")), null, true);
            importDataService.importData(readFile(new File("import-department.csv")), null, true);
            importDataService.importData(readFile(new File("import-test-employee.csv")), null, true);            
            importDataService.importData(readFile(new File("import-employee.csv")), null, true);
            importDataService.importData(readFile(new File("set-organization-boss.csv")), null, true);
            importDataService.importData(readFile(new File("import-profile.csv")), null, true);
            importDataService.importData(readFile(new File("import-person-profile.csv")), null, true);
            importDataService.importData(readFile(new File("import-string-value.csv")), null, true);
            importDataService.importData(readFile(new File("import-boolean-value.csv")), null, true);
            importDataService.importData(readFile(new File("import-date-value.csv")), null, true);
            importDataService.importData(readFile(new File("import-locale-value.csv")), null, true);
            importDataService.importData(readFile(new File("import-long-value.csv")), null, true);
            importDataService.importData(readFile(new File("import-employee-prof.csv")), null, true);
            importDataService.importData(readFile(new File("import-schedule.csv")), null, true);*/
            importDataService.importData(readFile(new File("import-test_type_7.csv")), null, true);
            importDataService.importData(readFile(new File("import-test_type_8.csv")), null, true);
            importDataService.importData(readFile(new File("import-test_type_9.csv")), null, true);
            importDataService.importData(readFile(new File("import-test_type_9_sys_fields.csv")), null, true);            

            log("Test OK");
        } finally {
            writeLog();
        }
    }
}
