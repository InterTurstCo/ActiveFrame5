package ru.intertrust.cm.remoteclient.crud.test;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.gui.impl.server.services.ExceptionType;
import ru.intertrust.cm.core.gui.impl.server.services.TestBmtService;
import ru.intertrust.cm.core.gui.impl.server.services.TestCmtService;
import ru.intertrust.cm.remoteclient.ClientBase;

public class TestTransaction extends ClientBase {

    private CollectionsService collectionsService;
    private TestCmtService cmtService;
    private TestBmtService bmtService;


    public static void main(String[] args) {
        try {
            TestTransaction test = new TestTransaction();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execute(String[] args) throws Exception {
        super.execute(args);

        cmtService = (TestCmtService) getService(
                "TestCmtServiceImpl", TestCmtService.Remote.class);
        bmtService = (TestBmtService) getService(
                "TestBmtServiceImpl", TestBmtService.Remote.class);
        collectionsService = (CollectionsService) getService(
                "CollectionsServiceImpl", CollectionsService.Remote.class);

        //Начало тестов
        //Вызов CMT Бина и выброс исключения из него, исключения не обработанные
        String name = "test-" + System.currentTimeMillis();
        log("CMT->FatalException");
        try{            
            cmtService.executeWithException(name, ExceptionType.FatalException);
        }catch(Exception ex){
            log("Throw " + ex.getClass());
        }
        check(name);

        name = "test-" + System.currentTimeMillis();
        log("CMT->SystemException");
        try{            
            cmtService.executeWithException(name, ExceptionType.SystemException);
        }catch(Exception ex){
            log("Throw " + ex.getClass());
        }
        check(name);

        name = "test-" + System.currentTimeMillis();
        log("CMT->RuntimeException");
        try{            
            cmtService.executeWithException(name, ExceptionType.RuntumeException);
        }catch(Exception ex){
            log("Throw " + ex.getClass());
        }
        check(name);
        log("=================================================================================");

        //Вызов CMT Бина который в свою очередь вызывает другой CМT бин, в первом CMT обрабатывается исключение
        name = "test-" + System.currentTimeMillis();
        log("CMT->CMT->FatalException->Rolback=true");
        try{
            cmtService.executeAndTry(name, ExceptionType.FatalException, true);
        }catch(Exception ex){
            log("Throw " + ex.getClass());
        }
        check(name);
        
        name = "test-" + System.currentTimeMillis();
        log("CMT->CMT->FatalException->Rolback=false");        
        cmtService.executeAndTry(name, ExceptionType.FatalException, false);
        check(name);

        name = "test-" + System.currentTimeMillis();
        log("CMT->CMT->SystemException->Rolback=true");
        try{
            cmtService.executeAndTry(name, ExceptionType.SystemException, true);
        }catch(Exception ex){
            log("Throw " + ex.getClass());
        }
        check(name);
        
        name = "test-" + System.currentTimeMillis();
        log("CMT->CMT->SystemException->Rolback=false");
        cmtService.executeAndTry(name, ExceptionType.SystemException, false);
        check(name);

        name = "test-" + System.currentTimeMillis();
        log("CMT->CMT->RuntumeException->Rolback=true");
        try{
            cmtService.executeAndTry(name, ExceptionType.RuntumeException, true);
        }catch(Exception ex){
            log("Throw " + ex.getClass());
        }
        check(name);
        
        name = "test-" + System.currentTimeMillis();
        log("CMT->CMT->RuntumeException->Rolback=false");
        cmtService.executeAndTry(name, ExceptionType.RuntumeException, false);
        check(name);
        log("=================================================================================");

        //Вызов BMT Бина который в свою очередь вызывает другой CМT бин, в вызываемом BMT обрабатывается исключение
        name = "test-" + System.currentTimeMillis();
        log("BMT->CMT->FatalException->Rolback=true");
        bmtService.execute(name, ExceptionType.FatalException, true);
        check(name);
        
        name = "test-" + System.currentTimeMillis();
        log("BMT->CMT->FatalException->Rolback=false");
        bmtService.execute(name, ExceptionType.FatalException, false);
        check(name);

        name = "test-" + System.currentTimeMillis();
        log("BMT->CMT->SystemException->Rolback=true");
        bmtService.execute(name, ExceptionType.SystemException, true);
        check(name);
        
        name = "test-" + System.currentTimeMillis();
        log("BMT->CMT->SystemException->Rolback=false");
        bmtService.execute(name, ExceptionType.SystemException, false);
        check(name);

        name = "test-" + System.currentTimeMillis();
        log("BMT->CMT->RuntumeException->Rolback=true");
        bmtService.execute(name, ExceptionType.RuntumeException, true);
        check(name);
        
        name = "test-" + System.currentTimeMillis();
        log("BMT->CMT->RuntumeException->Rolback=false");
        bmtService.execute(name, ExceptionType.RuntumeException, false);
        check(name);        
        
        log("Test OK");
    }

    private void check(String name){
        IdentifiableObjectCollection collection = collectionsService.findCollectionByQuery("select * from test_type_15 where name = '" + name + "'");
        log("Transaction Rolback " + (collection.size() == 0));        
    }

    

}
