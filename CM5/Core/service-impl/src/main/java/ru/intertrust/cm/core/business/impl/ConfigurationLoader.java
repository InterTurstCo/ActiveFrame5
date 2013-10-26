package ru.intertrust.cm.core.business.impl;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import ru.intertrust.cm.core.dao.api.ExtensionService;
import ru.intertrust.cm.core.dao.api.extension.OnLoadConfigurationExtensionHandler;

/**
 * Класс, предназначенный для загрузки конфигурации доменных объектов
 * 
 * @author vmatsukevich Date: 5/6/13 Time: 9:36 AM
 */
public class ConfigurationLoader implements ApplicationContextAware {

    @Autowired
    private ConfigurationControlService configurationControlService;
    private ApplicationContext context;

    /*
     * @Autowired private ExtensionService extensionService;
     */

    public ConfigurationLoader() {
    }

    /**
     * Устанавливает {@link #configurationControlService}
     * 
     * @param configurationControlService
     *            сервис для работы с конфигурацией доменных объектов
     */
    public void setConfigurationControlService(
            ConfigurationControlService configurationControlService) {
        this.configurationControlService = configurationControlService;
    }

    /**
     * Загружает конфигурацию доменных объектов, валидирует и создает
     * соответствующие сущности в базе. Добавляет запись администратора
     * (admin/admin) в таблицу authentication_info.
     * 
     * @throws Exception
     */
    public void load() throws Exception {
        configurationControlService.loadConfiguration();

        // Вызов точки расширения
        if (context != null) {
            ExtensionService extensionService = context
                    .getBean(ExtensionService.class);
            OnLoadConfigurationExtensionHandler extension = extensionService.getExtentionPoint(
                    OnLoadConfigurationExtensionHandler.class, null);
            extension.onLoad();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.context = applicationContext;

    }

}
