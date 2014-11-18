package ru.intertrust.cm.test.extension;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.dao.api.extension.BeforeSaveExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;

import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 17.11.2014
 *         Time: 14:01
 */
@ExtensionPoint(filter="letter")
public class BeforeSaveLetter implements BeforeSaveExtensionHandler {
    @Override
    public void onBeforeSave(DomainObject domainObject, List<FieldModification> changedFields) {
        if (domainObject.getString("subject") == null) {
            domainObject.setString("subject", "");
        }
    }
}
