package ru.intertrust.cm.remoteclient.permissions.test;

import java.util.Collection;

import javax.naming.NamingException;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.PermissionService;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.remoteclient.ClientBase;

public class TestRecalkAllPermissions extends ClientBase {
    public static void main(String[] args) {
        try {
            TestRecalkAllPermissions test = new TestRecalkAllPermissions();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execute(String[] args) throws Exception {
        try {
            super.execute(args);
            //Пересчет всех дин групп
            IdentifiableObjectCollection groupCollection =
                    getCollectionsService().findCollectionByQuery("select id, group_name, object_id from user_group where object_id is not null");
            for (IdentifiableObject identifiableObject : groupCollection) {
                System.out.println("Recalc group " + identifiableObject.getString("group_name") + " for context "
                        + identifiableObject.getReference("object_id"));
                getPermissionService().recalcGroup(identifiableObject.getId());
            }

            //Пересчет всех acl
            //Получаем все типы доменных объектов
            Collection<DomainObjectTypeConfig> allTypes = getConfigurationService().getConfigs(DomainObjectTypeConfig.class);
            for (DomainObjectTypeConfig domainObjectTypeConfig : allTypes) {
                if (!domainObjectTypeConfig.isTemplate() && !domainObjectTypeConfig.getName().endsWith("_al")) {
                    IdentifiableObjectCollection collection =
                            getCollectionsService().findCollectionByQuery("select id from " + domainObjectTypeConfig.getName());
                    for (IdentifiableObject identifiableObject : collection) {
                        System.out.println("Refresh ACL of type=" + domainObjectTypeConfig.getName() + " id=" + identifiableObject.getId());
                        getPermissionService().refreshAclFor(identifiableObject.getId());
                    }
                }
            }

            log("Test complete");
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
