package ru.intertrust.cm.remoteclient.load;

import java.io.File;

import ru.intertrust.cm.core.business.api.ImportDataService;
import ru.intertrust.cm.remoteclient.ClientBase;

public class TestImportData extends ClientBase {
    private ImportDataService.Remote loadDataService;

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
            loadDataService = (ImportDataService.Remote) getService("ImportDataService", ImportDataService.Remote.class);

            loadDataService.importData(readFile(new File("import-organization.csv")));

        } finally {
            writeLog();
        }
    }
}
