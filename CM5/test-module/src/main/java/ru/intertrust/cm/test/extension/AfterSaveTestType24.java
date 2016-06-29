package ru.intertrust.cm.test.extension;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.extension.AfterSaveExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;

@ExtensionPoint(filter = "test_type_24")
public class AfterSaveTestType24 implements AfterSaveExtensionHandler {
    @Autowired
    private DomainObjectDao domainObjectDao;
    @Autowired
    private CrudService crudSerevice;
    @Autowired
    private AccessControlService accessControlService;

    @Override
    public void onAfterSave(DomainObject domainObject, List<FieldModification> changedFields) {
        if (domainObject.getString("name").equalsIgnoreCase("create-test25-by-system")) {
            //Тест под системным токеном
            DomainObject test25 = new GenericDomainObject("test_type_25");
            test25.setString("name", "_" + System.currentTimeMillis());
            test25.setReference("test_type_24", domainObject.getId());
            domainObjectDao.save(test25, accessControlService.createSystemAccessToken(AfterSaveTestType24.class.getName()));
            
        }else if(domainObject.getString("name").equalsIgnoreCase("create-test25-by-person")){
            //Тест под пользовательскими правами
            DomainObject test25 = crudSerevice.createDomainObject("test_type_25");
            test25.setString("name", "_" + System.currentTimeMillis());
            test25.setReference("test_type_24", domainObject.getId());
            crudSerevice.save(test25);            
        }
    }

}
