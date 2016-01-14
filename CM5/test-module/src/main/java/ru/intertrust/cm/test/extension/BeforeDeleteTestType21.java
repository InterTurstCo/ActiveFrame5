package ru.intertrust.cm.test.extension;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.dao.api.extension.BeforeDeleteExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;

@ExtensionPoint(filter="test_type_21")
public class BeforeDeleteTestType21 implements BeforeDeleteExtensionHandler{

    @Autowired
    private CrudService crudService;
    
    
    
    @Override
    public void onBeforeDelete(DomainObject deletedDomainObject) {
        deletedDomainObject.setString("description", "description-" + System.currentTimeMillis());
        crudService.save(deletedDomainObject);        
    }

}
