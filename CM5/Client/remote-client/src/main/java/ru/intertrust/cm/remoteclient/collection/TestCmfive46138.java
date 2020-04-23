package ru.intertrust.cm.remoteclient.collection;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.remoteclient.ClientBase;

public class TestCmfive46138 extends ClientBase {

    public static void main(String[] args) {
        try {
            TestCmfive46138 test = new TestCmfive46138();
            test.execute(args);
        } catch (Exception ex) {
            logger.error("Test Error", ex);
        }
    }

    public void execute(String[] args) throws Exception {
        super.execute(args);
        CrudService crudService = (CrudService.Remote) getService(
                "CrudServiceImpl", CrudService.Remote.class, "person1", "admin");
        DomainObject testDomObj = crudService.createDomainObject("test_type_43");
        testDomObj.setString("stringField", "_" + System.currentTimeMillis());
        crudService.save(testDomObj);

        testDomObj = crudService.createDomainObject("test_type_43");
        testDomObj.setString("stringField", "_" + System.currentTimeMillis());
        crudService.save(testDomObj);

        crudService = (CrudService.Remote) getService(
                "CrudServiceImpl", CrudService.Remote.class, "person2", "admin");
        testDomObj = crudService.createDomainObject("test_type_43");
        testDomObj.setString("stringField", "_" + System.currentTimeMillis());
        crudService.save(testDomObj);

        CollectionsService collectionService = (CollectionsService.Remote) getService(
                "CollectionsServiceImpl", CollectionsService.Remote.class, "person1", "admin");
        IdentifiableObjectCollection collection11 = collectionService.findCollection("test_type_43_col");
        IdentifiableObjectCollection collection12 = collectionService.findCollectionByQuery("select id from test_type_43");

        collectionService = (CollectionsService.Remote) getService(
                "CollectionsServiceImpl", CollectionsService.Remote.class, "person2", "admin");
        IdentifiableObjectCollection collection21 = collectionService.findCollection("test_type_43_col");
        IdentifiableObjectCollection collection22 = collectionService.findCollectionByQuery("select id from test_type_43");

        collectionService = (CollectionsService.Remote) getService(
                "CollectionsServiceImpl", CollectionsService.Remote.class, "admin", "admin");
        IdentifiableObjectCollection collection31 = collectionService.findCollection("test_type_43_col");
        IdentifiableObjectCollection collection32 = collectionService.findCollectionByQuery("select id from test_type_43");

        assertTrue("Test row count", collection11.size() != collection21.size());
        assertTrue("Test row count", collection11.size() == collection31.size());

        assertTrue("Test row count", collection12.size() != collection22.size());
        assertTrue("Test row count", collection12.size() == collection32.size());

        assertTrue("Test row count", collection11.size() == collection12.size());
        assertTrue("Test row count", collection21.size() == collection22.size());
        assertTrue("Test row count", collection31.size() == collection32.size());

        if (hasError){
            logger.error("Test failed");
        }else{
            logger.error("Test complete");
        }
    }
}
