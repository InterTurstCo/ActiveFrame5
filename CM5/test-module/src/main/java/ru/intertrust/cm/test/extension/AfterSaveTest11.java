package ru.intertrust.cm.test.extension;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.StatusDao;
import ru.intertrust.cm.core.dao.api.extension.AfterSaveExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;

@ExtensionPoint(filter = "test_type_11")
public class AfterSaveTest11 implements AfterSaveExtensionHandler {
    @Autowired
    private DomainObjectDao domainSevice;

    @Autowired
    private StatusDao statusDao;

    @Autowired
    private AccessControlService accessControlService;

    @Override
    public void onAfterSave(DomainObject domainObject, List<FieldModification> changedFields) {
        
        if (domainObject.getString("status_name") != null && domainObject.getString("status_name").length() >0){
            Id statusId = statusDao.getStatusIdByName(domainObject.getString("status_name"));
            domainSevice.setStatus(domainObject.getId(), statusId,
                    accessControlService.createSystemAccessToken(this.getClass().getName()));
        }

    }

}
