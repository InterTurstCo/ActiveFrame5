package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.ColumnInfo;
import ru.intertrust.cm.core.business.api.dto.IndexInfo;
import ru.intertrust.cm.core.business.api.dto.UniqueKeyInfo;
import ru.intertrust.cm.core.config.*;

import java.util.Collection;
import java.util.Set;

/**
 * Сервис, кэширующий метаданные базы данных и предоставляющий доступ к ним
 * Created by vmatsukevich on 27.1.15.
 */
public interface SchemaCache {

    /**
     * Обновляет кэш
     */
    void reset();

    /**
     * Проверяет, существует ли таблица с указанным именем
     * @param tableName имя таблицы
     * @return true если существует, иначе false
     */
    boolean isTableExist(String tableName);

    /**
     * Проверяет, существует ли таблица для указанной конфигурации доменного объекта
     * @param config имя конфигурации доменного объекта
     * @return true если существует, иначе false
     */
    boolean isTableExist(DomainObjectTypeConfig config);

    /**
     * Проверяет, существует ли колонка для указанной конфигурации поля доменного объекта
     * @param config имя конфигурации доменного объекта
     * @param fieldConfig имя конфигурации поля типа доменного объекта
     * @return true если существует, иначе false
     */
    boolean isColumnExist(DomainObjectTypeConfig config, FieldConfig fieldConfig);

    /**
     * Проверяет, существует ли колонка для типа указанной конфигурации ссылочного поля доменного объекта
     * @param config имя конфигурации доменного объекта
     * @param referenceFieldConfig имя конфигурации ссылочного поля типа доменного объекта
     * @return true если существует, иначе false
     */
    boolean isReferenceColumnExist(DomainObjectTypeConfig config, ReferenceFieldConfig referenceFieldConfig);

    /**
     * Проверяет, имеет ли колонка not-null ограничение для указанной конфигурации поля доменного объекта
     * @param config имя конфигурации доменного объекта
     * @param fieldConfig имя конфигурации поля типа доменного объекта
     * @return true если имеет, иначе false
     */
    boolean isColumnNotNull(DomainObjectTypeConfig config, FieldConfig fieldConfig);

    /**
     * Возвращает длину колонки для указанной конфигурации поля доменного объекта
     * @param config имя конфигурации доменного объекта
     * @param fieldConfig имя конфигурации поля типа доменного объекта
     * @return длина колонки
     */
    int getColumnLength(DomainObjectTypeConfig config, FieldConfig fieldConfig);

    /**
     * Возарвщаею метаданные колонки в бд
     * @param config конфигурация типа ДО
     * @param fieldConfig конфигурация поля типа ДО
     * @return метаданные колонки в бд
     */
    ColumnInfo getColumnInfo(DomainObjectTypeConfig config, FieldConfig fieldConfig);

    /**
     * Возвращает метаданные колонки в бд, соответствующей типу ссылочного поля
     * @param config конфигурация типа ДО
     * @param referenceFieldConfig конфигурация ссылочного поля типа ДО
     * @return метаданные колонки в бд
     */
    ColumnInfo getReferenceColumnInfo(DomainObjectTypeConfig config, ReferenceFieldConfig referenceFieldConfig);

    /**
     * Находит имя внешнего ключа для ссылочного поля
     * @param config конфигурация типа ДО
     * @param fieldConfig конфигурация ссылочного поля типа ДО
     * @return
     */
    String getForeignKeyName(DomainObjectTypeConfig config, ReferenceFieldConfig fieldConfig);

    String getParentTypeForeignKeyName(DomainObjectTypeConfig config);

    /**
     * Возвращает все уникальные ключи типа доменного объекта
     * @param config конфигурация типа ДО
     * @return все уникальные ключи типа доменного объекта
     */
    Collection<UniqueKeyInfo> getUniqueKeys(DomainObjectTypeConfig config);

    /**
     * Находит имя уникального ключа по его конфигурации
     * @param config конфигурация типа ДО
     * @param keyConfig конфигурация уникального ключа
     * @return
     */
    String getUniqueKeyName(DomainObjectTypeConfig config, UniqueKeyConfig keyConfig);

    /**
     * Находит имя индекса по его конфигурации
     * @param config конфигурация типа ДО
     * @param indexConfig конфигурация индекса
     * @return имя индекса
     */
    String getIndexName(DomainObjectTypeConfig config, IndexConfig indexConfig);

    /**
     * Находит все созданные для типа доменного объекта индексы
     * @param config конфигурация типа ДО
     * @return все созданные для типа доменного объекта индексы
     */
    Collection<IndexInfo> getIndices(DomainObjectTypeConfig config);

    /**
     * Находит имена всех созданных для типа доменного объекта индексов
     * @param config конфигурация типа ДО
     * @return имена всех созданных для типа доменного объекта индексов
     */
    Set<String> getIndexNames(DomainObjectTypeConfig config);
}
