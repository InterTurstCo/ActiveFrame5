package ru.intertrust.cm.core.business.impl;

import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.config.BusinessObjectsConfigurationLogicalValidator;

/**
 * Класс, предназначенный для загрузки конфигурации бизнес-объектов
 * @author vmatsukevich
 *         Date: 5/6/13
 *         Time: 9:36 AM
 */
public class ConfigurationLoader {

    private BusinessObjectsConfigurationLogicalValidator logicalValidator;
    private ConfigurationService configurationService;

    public ConfigurationLoader() {
    }

    public void setLogicalValidator(BusinessObjectsConfigurationLogicalValidator logicalValidator) {
        this.logicalValidator = logicalValidator;
    }

    /**
     * Устанавливает {@link #configurationService}
     * @param configurationService сервис для работы с конфигурацией бизнес-объектов
     */
    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    /**
     * Загружает конфигурацию бизнес-объектов, валидирует и создает соответствующие сущности в базе.
     * Добавляет запись администратора (admin/admin) в таблицу authentication_info.
     * @throws Exception
     */
    public void load() throws Exception {
        logicalValidator.validate();
        configurationService.loadConfiguration();
    }

}
