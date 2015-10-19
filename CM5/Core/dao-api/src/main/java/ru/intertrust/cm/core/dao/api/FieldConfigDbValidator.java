package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.ColumnInfo;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;

/**
 * Проверяет соответствие конфигурации поля типа ДО соотв. колонке в базе
 */
public interface FieldConfigDbValidator {

    /**
     * Проверяет соответствие конфигурации поля типа ДО соотв. колонке в базе
     */
    void validate(FieldConfig fieldConfig, DomainObjectTypeConfig domainObjectTypeConfig, ColumnInfo columnInfo);
}
