package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.ColumnInfo;
import ru.intertrust.cm.core.business.api.dto.ForeignKeyInfo;
import ru.intertrust.cm.core.business.api.dto.IndexInfo;
import ru.intertrust.cm.core.business.api.dto.UniqueKeyInfo;
import ru.intertrust.cm.core.config.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * Создает индексы по конфигурации доменного объекта
     * @param config конфигурация доменного объекта
     */
    void createTableIndices(DomainObjectTypeConfig config, boolean isParentType);

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
    void updateTableStructure(DomainObjectTypeConfig config, List<FieldConfig> fieldConfigList, boolean isAl, boolean isParent);

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
     * Удаляет индексы доменного объекта по имени, в том= числе и автоиндексы.
     * @param domainObjectTypeConfig конфигурация доменного объекта
     * @param indicesToDelete список имен индексов для удаления.
     */
    void deleteIndices(DomainObjectTypeConfig domainObjectTypeConfig, Set<String> indicesToDelete);

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
     * Создает уникальные констрэйнты
     * @param config конфигурация доменного объекта, таблицу которого необходимо обновить
     * @param uniqueKeyConfigList список уникальных ключей для добавления
     */
    void createUniqueConstraints(DomainObjectTypeConfig config,
                                 List<UniqueKeyConfig> uniqueKeyConfigList);

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

    /**
     * Извлекает метаданные схемы о таблицах и их колонках
     * @return метаданные схемы о таблицах и их колонках
     */
    Map<String, Map<String, ColumnInfo>> getSchemaTables();

    /**
     * Извлекает метаданные схемы о внешних ключах
     * @return метаданные схемы о внешних ключах
     */
    Map<String, Map<String, ForeignKeyInfo>> getForeignKeys();

    /**
     * Извлекает метаданные схемы об уникальных ключах
     * @return метаданные схемы об уникальных ключах
     */
    Map<String, Map<String, UniqueKeyInfo>> getUniqueKeys();

    /**
     * Извлекает метаданные схемы о индексах
     * @return метаданные схемы о индексах
     */
    Map<String, Map<String, IndexInfo>> getIndexes();

    /**
     * Извлекает метаданные схемы о индексах
     * @return метаданные схемы о индексах
     */
    Map<String, Map<String, IndexInfo>> getIndexes(DomainObjectTypeConfig config);

    /**
     * Устанавливает/снимает с колонки not-null ограничение
     * @param config конфигурация типа доменного объекта
     * @param fieldConfig конфигурация поля типа доменного объекта
     * @param notNull указывает установить или снять not-null ограничение
     */
    void setColumnNotNull(DomainObjectTypeConfig config, FieldConfig fieldConfig, boolean notNull);

    /**
     * Удаляет констрэйнт
     * @param config конфигурация типа доменного объекта
     * @param constraintName имя констрэйнта
     */
    void dropConstraint(DomainObjectTypeConfig config, String constraintName);

    /**
     * Изменяет тип колонки
     * @param config конфигурация типа доменного объекта
     * @param oldFieldConfig старая конфигурация поля типа доменного объекта
     * @param newFieldConfig новая конфигурация поля типа доменного объекта
     */
    void updateColumnType(DomainObjectTypeConfig config, FieldConfig oldFieldConfig, FieldConfig newFieldConfig);

    /**
     * Физически удаляет колонку типа ДО
     * @param config конфигурация типа доменного объекта
     * @param fieldConfig конфигурация поля типа доменного объекта
     */
    void deleteColumn(DomainObjectTypeConfig config, FieldConfig fieldConfig);

    /**
     * Переименовывает поле типа доменного объекта
     * @param config конфигурация типа доменного объекта
     * @param oldName имя поля типа доменного объекта
     * @param newFieldConfig
     */
    void renameColumn(DomainObjectTypeConfig config, String oldName, FieldConfig newFieldConfig);

    /**
     * Физически удаляет тип ДО
     * @param config конфигурация типа доменного объекта
     */
    void deleteTable(DomainObjectTypeConfig config);

    /**
     * Физически удаляет тип ДО и его таблицы-спутники: *_al, *_acl, *_read
     * @param config конфигурация типа доменного объекта
     */
    void deleteTypeTables(DomainObjectTypeConfig config);

    /**
     * Выполняет произвольный sql-запрос
     * @param sqlQuery sql-запрос
     */
    void executeSqlQuery(String sqlQuery);

    /**
     * Возвращает тип данных колонки в бд
     * @param fieldConfig конфигурация поля типа доменного объекта
     * @return тип данных колонки в бд
     */
    String getSqlType(FieldConfig fieldConfig);

    /**
     * Выполняет сбор статистики базы данных
     */
    void gatherStatistics();

    /**
     * Добавление колонок в таблицу
     * @param type
     * @param fields
     */
    void addColumns(DomainObjectTypeConfig type, List<FieldConfig> fields);
}
