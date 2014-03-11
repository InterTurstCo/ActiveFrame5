package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.dao.api.*;

/**
 * Created by vmatsukevich on 2/18/14.
 */
public interface DaoFactory {

    AuditLogServiceDao createAuditLogServiceDao();
    AuthenticationDao createAuthenticationDao();
    CollectionsDao createCollectionsDao();
    ConfigurationDao createConfigurationDao();
    DomainObjectDao createDomainObjectDao();
    AttachmentContentDao createAttachmentContentDao();
    PersonManagementServiceDao createPersonManagementServiceDao();
    PersonServiceDao createPersonServiceDao();
    DataStructureDao createDataStructureDao();
    StatusDao createStatusDao();
}
