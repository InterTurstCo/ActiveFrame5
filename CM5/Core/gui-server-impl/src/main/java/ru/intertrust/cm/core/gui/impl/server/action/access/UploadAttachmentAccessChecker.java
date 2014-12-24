package ru.intertrust.cm.core.gui.impl.server.action.access;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.access.AccessVerificationService;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * @author Lesia Puhova
 *         Date: 24.12.2014
 *         Time: 18:55
 */
@ComponentName("upload.attachment.access.checker")
public class UploadAttachmentAccessChecker implements AccessChecker {

    @Autowired
    private AccessVerificationService accessVerificationService;

    @Autowired
    private CrudService crudService;

    @Override
    public boolean checkAccess(Id objectId) {
        String attachmentTypeName = crudService.getDomainObjectType(objectId);
        return accessVerificationService.isCreatePermitted(attachmentTypeName);
    }
}
