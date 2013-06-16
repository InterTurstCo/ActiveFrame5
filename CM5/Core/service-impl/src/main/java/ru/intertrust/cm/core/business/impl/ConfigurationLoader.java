package ru.intertrust.cm.core.business.impl;

import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.config.DomainObjectsConfigurationLogicalValidator;

/**
 * Класс, предназначенный для загрузки конфигурации доменных объектов
 * @author vmatsukevich
 *         Date: 5/6/13
 *         Time: 9:36 AM
 */
public class ConfigurationLoader {

    private DomainObjectsConfigurationLogicalValidator logicalValidator;
    private ConfigurationService configurationService;

    public ConfigurationLoader() {
    }

    public void setLogicalValidator(DomainObjectsConfigurationLogicalValidator logicalValidator) {
        this.logicalValidator = logicalValidator;
    }

    /**
     * Устанавливает {@link #configurationService}
     * @param configurationService сервис для работы с конфигурацией доменных объектов
     */
    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    /**
     * Загружает конфигурацию доменных объектов, валидирует и создает соответствующие сущности в базе.
     * Добавляет запись администратора (admin/admin) в таблицу authentication_info.
     * @throws Exception
     */
    public void load() throws Exception {
        logicalValidator.validate();
        configurationService.loadConfiguration();
    }

}
