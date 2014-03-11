package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.dao.api.*;

/**
 * Created by vmatsukevich on 2/18/14.
 */
public abstract class AbstractDaoFactory implements DaoFactory{
    @Override
    public AuditLogServiceDao createAuditLogServiceDao() {
        return new AuditLogServiceDaoImpl();
    }

    @Override
    public AuthenticationDao createAuthenticationDao() {
        return new AuthenticationDaoImpl();
    }

    @Override
    public CollectionsDao createCollectionsDao() {
        return new CollectionsDaoImpl();
    }

    @Override
    public ConfigurationDao createConfigurationDao() {
        return new ConfigurationDaoImpl();
    }

    @Override
    public DomainObjectDao createDomainObjectDao() {
        return new DomainObjectDaoImpl();
    }

    @Override
    public AttachmentContentDao createAttachmentContentDao() {
        return new FileSystemAttachmentContentDaoImpl();
    }

    @Override
    public PersonManagementServiceDao createPersonManagementServiceDao() {
        return new PersonManagementServiceDaoImpl();
    }

    @Override
    public PersonServiceDao createPersonServiceDao() {
        return new PersonServiceDaoImpl();
    }

    @Override
    public StatusDao createStatusDao() {
        return new StatusDaoImpl();
    }
}
