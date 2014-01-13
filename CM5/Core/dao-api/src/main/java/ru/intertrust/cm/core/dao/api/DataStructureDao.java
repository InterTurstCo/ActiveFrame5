package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.IndexConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.UniqueKeyConfig;

import java.util.List;

/**
 * DAO для работы со структурой базы данных (создание таблиц, колонок, индексов и т.п.)
 * @author vmatsukevich
 *         Date: 5/15/13
 *         Time: 4:27 PM
 */
public interface DataStructureDao {

    String AUTHENTICATION_INFO_TABLE = "authentication_info";
    String USER_UID_COLUMN = "user_uid";

    /**
     * Создает таблицу по конфигурации доменного объекта
     * @param config конфигурация доменного объекта
     */
    void createTable(DomainObjectTypeConfig config);

    /**
     * Создает таблицы прав доступа для доменного объекта (таблицы _ACL и _READ)
     * @param config конфигурация доменного объекта
     */
    void createAclTables(DomainObjectTypeConfig config);

    /**
     * Обновляет структуру таблицы (добавляет колонки и уникальные ключи)
     * @param domainObjectConfigName название доменного объекта, таблицу которого необходимо обновить
     * @param fieldConfigList список колонок для добавления
     */
    void updateTableStructure(String domainObjectConfigName, List<FieldConfig> fieldConfigList);

    /**
     * Создает новые индексы для доменного объекта
     * @param domainObjectConfigName название доменного объекта
     * @param indexConfigList список конфигураций новых индексов
     */
    public void createIndices(String domainObjectConfigName, List<IndexConfig> indexConfigList);

    /**
     * Удаляет индексы для доменного объекта. Если один из переданных индексов является автоматическим, то индекс не
     * удаляется.
     * @param domainObjectTypeConfig название доменного объекта
     * @param indexConfigList список конфигураций индексов для удаления.
     */
    public void deleteIndices(DomainObjectTypeConfig domainObjectTypeConfig, List<IndexConfig> indexConfigList);

    /**
     * Создает форен-ки и уникальные констрэйнты
     * @param domainObjectConfigName название доменного объекта, таблицу которого необходимо обновить
     * @param fieldConfigList список колонок для создания форен-ки констрэйнтов
     * @param uniqueKeyConfigList список уникальных ключей для добавления
     */
    void createForeignKeyAndUniqueConstraints(String domainObjectConfigName, List<ReferenceFieldConfig> fieldConfigList,
                              List<UniqueKeyConfig> uniqueKeyConfigList);

    /**
     * Создает последовательность для таблицы по конфигурации доменного объекта
     * @param config конфигурация доменного объекта
     */
    void createSequence(DomainObjectTypeConfig config);

    /**
     * Возвращает кол-во таблиц в базе данных
     * @return кол-во таблиц в базе данных
     */
    Integer countTables();

    /**
     * Создает сервисные таблицы, которые не задаются конфигурацией доменных объектов
     */
    void createServiceTables();

    /**
     * Проверяет, существует ли таблица с указанным именем
     * @param tableName имя таблицы
     * @return true если существует, иначе false
     */
    boolean doesTableExists(String tableName);

    /**
     * Создание таблицы для хранения информации сервиса AuditLog
     * @param config
     */
    void createAuditLogTable(DomainObjectTypeConfig config);

    /**
     * Создание последовательности для таблицы аудита типа, переданного в параметре
     * @param config
     */
    void createAuditSequence(DomainObjectTypeConfig config);
}
