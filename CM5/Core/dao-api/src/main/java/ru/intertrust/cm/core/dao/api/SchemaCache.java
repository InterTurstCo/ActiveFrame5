package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.ColumnInfo;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.UniqueKeyConfig;

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
     * Определяет существование внещнего ключа для ссылочного поля
     * @param config конфигурация типа ДО
     * @param fieldConfig конфигурация ссылочного поля типа ДО
     * @return
     */
    boolean isReferenceFieldForeignKeyExist(DomainObjectTypeConfig config, ReferenceFieldConfig fieldConfig);

    /**
     * Находит имя уникального ключа по его конфигурации
     * @param config конфигурация типа ДО
     * @param keyConfig конфигурация уникального ключа
     * @return
     */
    String getUniqueKeyName(DomainObjectTypeConfig config, UniqueKeyConfig keyConfig);
}
