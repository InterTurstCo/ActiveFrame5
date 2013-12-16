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

            importDataService.importData(readFile(new File("import-organization.csv")));
            importDataService.importData(readFile(new File("import-department.csv")));
            importDataService.importData(readFile(new File("import-employee.csv")));
            importDataService.importData(readFile(new File("set-organization-boss.csv")));
            importDataService.importData(readFile(new File("set-department-boss.csv")));            

        } finally {
            writeLog();
        }
    }
}
