package ru.intertrust.cm.test.acess;

import java.util.List;

import javax.annotation.security.RunAs;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.PermissionService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.DomainObjectPermission;

@Stateless(name="TestPermissionsServiceInAcyncMode")
@Local(TestPermissionsServiceInAcyncMode.class)
@Remote(TestPermissionsServiceInAcyncMode.Remote.class)
@RunAs("system")
public class TestPermissionsServiceInAcyncModeImpl implements TestPermissionsServiceInAcyncMode {
    @EJB
    private PermissionService permissionService;
    @EJB
    private CrudService crudService;
    

    @Override
    @Asynchronous
    public void test() {
        
        List<DomainObject> persons = crudService.findAll("person");
        
        DomainObjectPermission permission = permissionService.getObjectPermission(persons.get(0).getId());
        
    }

}
