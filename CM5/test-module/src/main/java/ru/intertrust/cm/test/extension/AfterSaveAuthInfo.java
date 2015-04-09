package ru.intertrust.cm.test.extension;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.dao.api.extension.AfterSaveExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;

@ExtensionPoint(filter = "Authentication_Info")
public class AfterSaveAuthInfo implements AfterSaveExtensionHandler {
    static final Logger logger = LoggerFactory.getLogger(AfterSaveAuthInfo.class);

    @Override
    public void onAfterSave(DomainObject domainObject, List<FieldModification> changedFields) {
        logger.debug("Before save DOP '" + domainObject.getTypeName() + "' '" + domainObject.getString("user_uid") + "' in "
                + AfterSaveAuthInfo.class.getName());
    }
}
