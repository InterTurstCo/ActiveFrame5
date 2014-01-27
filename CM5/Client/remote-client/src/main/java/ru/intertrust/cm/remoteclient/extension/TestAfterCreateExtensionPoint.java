package ru.intertrust.cm.remoteclient.extension;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.remoteclient.ClientBase;

public class TestAfterCreateExtensionPoint extends ClientBase {

    private CrudService.Remote crudService;

    public static void main(String[] args) {
        try {
            TestAfterCreateExtensionPoint test = new TestAfterCreateExtensionPoint();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execute(String[] args) throws Exception {
        try {
            super.execute(args);

            crudService = (CrudService.Remote) getService(
                    "CrudServiceImpl", CrudService.Remote.class);

            DomainObject country = crudService.createDomainObject("country");
            assertTrue("Fill name", !country.getString("name").isEmpty());

        } finally {
            writeLog();
        }
    }
}
