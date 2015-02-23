package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;

/**
 * Обработчик изменения конфигурации поля типа доменного объекта
 */
public interface FieldConfigChangeHandler {

    /**
     * Обрабатывает изменения конфигурации поля типа доменного объекта
     * @param newFieldConfig новая конфигурация поля типа доменного объекта
     * @param oldFieldConfig старая конфигурация поля типа доменного объекта
     * @param domainObjectTypeConfig конфигурация типа доменного объекта
     * @param configurationExplorer configurationExplorer
     */
    void handle(FieldConfig newFieldConfig, FieldConfig oldFieldConfig,
                DomainObjectTypeConfig domainObjectTypeConfig, ConfigurationExplorer configurationExplorer);
}
