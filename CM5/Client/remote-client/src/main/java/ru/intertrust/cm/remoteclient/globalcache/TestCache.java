package ru.intertrust.cm.remoteclient.globalcache;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.remoteclient.ClientBase;

import java.util.Random;

public class TestCache extends ClientBase {

    private CollectionsService.Remote collectionService;
    private Random rnd = new Random();

    public static void main(String[] args) {
        try {
            TestCache test = new TestCache();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void execute(String[] args) throws Exception {
        try {
            super.execute(args);
            log("Start");

            collectionService = (CollectionsService.Remote) getService(
                    "CollectionsServiceImpl", CollectionsService.Remote.class);

            for (int i = 0; i < 2; i++) {
                String query = "select t.*, '_" + rnd.nextLong() + "' from notification_text t, person, group_member ";
                IdentifiableObjectCollection collection = collectionService.findCollectionByQuery(query);
                log("Execute " + i);
            }

            log("Finish");
        } finally {
            writeLog();
        }
    }
}