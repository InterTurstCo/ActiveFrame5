package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.dao.api.*;

/**
 * Абстрактная имплементация {@link DaoFactory}, содержащая реализации фабрик-методов для неспецифичных ДАО-сервисов
 * @author vmatsukevich
 */
public abstract class AbstractDaoFactory implements DaoFactory {
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
    public InitializationLockDao createInitializationLockDao() {
        return new InitializationLockDaoImpl();
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

    @Override
    public SchedulerDao createSchedulerDao() {
        return new SchedulerDaoImpl();
    }
}
