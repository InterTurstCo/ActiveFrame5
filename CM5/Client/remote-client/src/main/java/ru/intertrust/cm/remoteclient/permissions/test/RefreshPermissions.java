package ru.intertrust.cm.remoteclient.permissions.test;

import javax.naming.NamingException;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.PermissionService;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.remoteclient.ClientBase;

public class RefreshPermissions extends ClientBase {
    public static final String TYPE_NAME = "typeName";
    public static void main(String[] args) {
        try {
            RefreshPermissions test = new RefreshPermissions();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execute(String[] args) throws Exception {
        try {
            super.execute(args, new String[]{TYPE_NAME});
            
            String type = getParamerer(TYPE_NAME);
            log("Refresh permissions on " + type);
            int errorCount = 0;
            
            IdentifiableObjectCollection collection =
                    getCollectionsService().findCollectionByQuery("select id from " + type);
            for (IdentifiableObject identifiableObject : collection) {
                try{
                    log("Refresh ACL of type=" + type + " id=" + identifiableObject.getId());
                    getPermissionService().refreshAclFor(identifiableObject.getId());
                }catch(Exception ex){
                    log("Error refresh permissions on " + identifiableObject.getId());
                    errorCount++;
                }
            }

            log("Complete with " + errorCount + " errors.");
        } finally {
            writeLog();
        }
    }

    private CrudService.Remote getCrudService() throws NamingException {
        return (CrudService.Remote) getService("CrudServiceImpl", CrudService.Remote.class);
    }

    private PermissionService.Remote getPermissionService() throws NamingException {
        return (PermissionService.Remote) getService("PermissionService",
                PermissionService.Remote.class);
    }

    private ConfigurationService.Remote getConfigurationService() throws NamingException {
        return (ConfigurationService.Remote) getService("ConfigurationServiceImpl",
                ConfigurationService.Remote.class);
    }

    private CollectionsService.Remote getCollectionsService() throws NamingException {
        return (CollectionsService.Remote) getService(
                "CollectionsServiceImpl", CollectionsService.Remote.class);
    }

}
