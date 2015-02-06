package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.ColumnInfo;
import ru.intertrust.cm.core.business.api.dto.ForeignKeyInfo;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.IndexConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.UniqueKeyConfig;

import java.util.List;
import java.util.Map;

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
    void createTable(DomainObjectTypeConfig config, boolean isParentType);

    /**
     * Создает таблицы прав доступа для доменного объекта (таблицы _ACL и _READ)
     * @param config конфигурация доменного объекта
     */
    void createAclTables(DomainObjectTypeConfig config);

    /**
     * Обновляет структуру таблицы (добавляет колонки и уникальные ключи)
     * @param config конфигурация доменного объекта, таблицу которого необходимо обновить
     * @param fieldConfigList список колонок для добавления
     */
    void updateTableStructure(DomainObjectTypeConfig config, List<FieldConfig> fieldConfigList, boolean isParent);

    /**
     * Создает новые индексы для доменного объекта
     * @param domainObjectTypeConfig конфигурация доменного объекта
     * @param indexConfigList список конфигураций новых индексов
     */
    public void createIndices(DomainObjectTypeConfig domainObjectTypeConfig, List<IndexConfig> indexConfigList);

    /**
     * Удаляет индексы для доменного объекта. Если один из переданных индексов является автоматическим, то индекс не
     * удаляется.
     * @param domainObjectTypeConfig конфигурация доменного объекта
     * @param indexConfigList список конфигураций индексов для удаления.
     */
    public void deleteIndices(DomainObjectTypeConfig domainObjectTypeConfig, List<IndexConfig> indexConfigList);

    /**
     * Создает форен-ки и уникальные констрэйнты
     * @param domainObjectTypeConfig конфигурация доменного объекта, таблицу которого необходимо обновить
     * @param fieldConfigList список колонок для создания форен-ки констрэйнтов
     * @param uniqueKeyConfigList список уникальных ключей для добавления
     */
    void createForeignKeyAndUniqueConstraints(DomainObjectTypeConfig domainObjectTypeConfig, List<ReferenceFieldConfig> fieldConfigList,
                              List<UniqueKeyConfig> uniqueKeyConfigList);

    /**
     * Создает последовательность для таблицы по конфигурации доменного объекта
     * @param config конфигурация доменного объекта
     */
    void createSequence(DomainObjectTypeConfig config);

    /**
     * Создает сервисные таблицы, которые не задаются конфигурацией доменных объектов
     */
    void createServiceTables();

    /**
     * Проверяет, существует ли таблица с указанным именем
     * @param tableName имя таблицы
     * @return true если существует, иначе false
     */
    boolean isTableExist(String tableName);

    /**
     * Создание последовательности для таблицы аудита типа, переданного в параметре
     * @param config
     */
    void createAuditSequence(DomainObjectTypeConfig config);

    Map<String, Map<String, ColumnInfo>> getSchemaTables();

    Map<String, Map<String, ForeignKeyInfo>> getForeignKeys();

    void setColumnNullable(DomainObjectTypeConfig config, FieldConfig fieldConfig);
}
