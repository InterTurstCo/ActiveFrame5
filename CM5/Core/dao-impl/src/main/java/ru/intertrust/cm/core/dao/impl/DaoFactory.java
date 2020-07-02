package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.dao.api.*;

/**
 * Интерфейс абстрактной фабрики ДАО-сервисов.
 * @author vmatsukevich
 */
public interface DaoFactory extends DatabaseDaoFactory {

    /**
     * Создаёт AuditLogServiceDao
     * @return AuditLogServiceDao
     */
    AuditLogServiceDao createAuditLogServiceDao();

    /**
     * Создаёт AuthenticationDao
     * @return AuthenticationDao
     */
    AuthenticationDao createAuthenticationDao();

    /**
     * Создаёт CollectionsDao
     * @return CollectionsDao
     */
    CollectionsDao createCollectionsDao();

    /**
     * Создаёт ConfigurationDao
     * @return ConfigurationDao
     */
    ConfigurationDao createConfigurationDao();

    /**
     * Создаёт DomainObjectDao
     * @return DomainObjectDao
     */
    DomainObjectDao createDomainObjectDao();

    DomainObjectQueryHelper createQueryHelper();

    InitializationLockDao createInitializationLockDao();

    /**
     * Создаёт PersonManagementServiceDao
     * @return PersonManagementServiceDao
     */
    PersonManagementServiceDao createPersonManagementServiceDao();

    /**
     * Создаёт PersonServiceDao
     * @return PersonServiceDao
     */
    PersonServiceDao createPersonServiceDao();

    /**
     * Создаёт DataStructureDao
     * @return DataStructureDao
     */
    DataStructureDao createDataStructureDao();

    /**
     * Создаёт StatusDao
     * @return StatusDao
     */
    StatusDao createStatusDao();

    /**
     * Создаёт IdGenerator
     * @return IdGenerator
     */
    IdGenerator createIdGenerator();

    /**
     * Создаёт SchedulerDao
     * @return SchedulerDao
     */
    SchedulerDao createSchedulerDao();
    
}
