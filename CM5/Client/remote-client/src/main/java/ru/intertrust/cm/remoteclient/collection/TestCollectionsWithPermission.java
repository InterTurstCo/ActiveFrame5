package ru.intertrust.cm.remoteclient.collection;

import javax.naming.NamingException;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.remoteclient.ClientBase;

public class TestCollectionsWithPermission extends ClientBase{
    public static void main(String[] args) {
        try {
            TestCollectionsWithPermission test = new TestCollectionsWithPermission();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void execute(String[] args) throws NamingException {
        CollectionsService collectionService = (CollectionsService.Remote) getService(
                "CollectionsServiceImpl", CollectionsService.Remote.class, "person10", "admin");
        
    }
}
