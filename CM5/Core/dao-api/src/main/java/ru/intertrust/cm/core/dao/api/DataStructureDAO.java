package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.config.model.DomainObjectConfig;
import ru.intertrust.cm.core.config.model.FieldConfig;
import ru.intertrust.cm.core.config.model.UniqueKeyConfig;

import java.util.List;

/**
 * DAO для работы со структурой базы данных (создание таблиц, колонок, индексов и т.п.)
 * @author vmatsukevich
 *         Date: 5/15/13
 *         Time: 4:27 PM
 */
public interface DataStructureDAO {

    String DOMAIN_OBJECT_TABLE = "DOMAIN_OBJECT";

    /**
     * Создает таблицу по конфигурации доменного объекта
     * @param config конфигурация доменного объекта
     */
    void createTable(DomainObjectConfig config);

    void updateTableStructure(String domainObjectConfigName, List<FieldConfig> fieldConfigList,
                              List<UniqueKeyConfig> uniqueKeyConfigList);

    /**
     * Создает последовательность для таблицы по конфигурации доменного объекта
     * @param config конфигурация доменного объекта
     */
    void createSequence(DomainObjectConfig config);

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
