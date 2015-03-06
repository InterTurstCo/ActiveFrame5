package ru.intertrust.cm.remoteclient.crypto;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.crypto.CryptoService;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.crypto.VerifyResult;
import ru.intertrust.cm.remoteclient.ClientBase;

public class TestCryptoService extends ClientBase {
    private CrudService crudService;
    private CollectionsService collectionsService;
    private CryptoService cryptoService;
    private String[] types = new String[]{"country_attachment", "fauna_attachment"};
    
    
    public static void main(String[] args) {
        try {
            TestCryptoService test = new TestCryptoService();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void execute(String[] args) throws Exception {
        super.execute(args);

        crudService = (CrudService) getService(
                "CrudServiceImpl", CrudService.Remote.class);
        collectionsService = (CollectionsService) getService(
                "CollectionsServiceImpl", CollectionsService.Remote.class);
        cryptoService = (CryptoService) getService(
                "CryptoService", CryptoService.Remote.class);
        
        for (String type : types) {
            String query = "select id from " + type;
            IdentifiableObjectCollection collection = collectionsService.findCollectionByQuery(query);
            for (IdentifiableObject identifiableObject : collection) {
                VerifyResult result = cryptoService.verify(identifiableObject.getId());
                if (result.getSignerInfos().size() > 0){
                    System.out.println(result.toString());
                }
            }
        }

    }   
}
