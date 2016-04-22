package ru.intertrust.cm.test.extension;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

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
    private AccessControlService accessControlService;

    @Override
    public void onAfterSave(DomainObject domainObject, List<FieldModification> changedFields) {
        DomainObject test25 = new GenericDomainObject("test_type_25");
        test25.setString("name", "_" + System.currentTimeMillis());
        test25.setReference("test_type_24", domainObject.getId());
        domainObjectDao.save(test25, accessControlService.createSystemAccessToken(AfterSaveTestType24.class.getName()));
    }

}
