package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.config.model.*;

import java.util.List;

/**
 * DAO для работы со структурой базы данных (создание таблиц, колонок, индексов и т.п.)
 * @author vmatsukevich
 *         Date: 5/15/13
 *         Time: 4:27 PM
 */
public interface DataStructureDao {

    String AUTHENTICATION_INFO_TABLE = "AUTHENTICATION_INFO";

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
}
