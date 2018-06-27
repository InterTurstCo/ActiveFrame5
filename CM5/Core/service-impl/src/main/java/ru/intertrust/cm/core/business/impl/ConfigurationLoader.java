package ru.intertrust.cm.core.business.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ConfigurationLoadService;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.api.ExtensionService;
import ru.intertrust.cm.core.dao.api.extension.OnLoadConfigurationExtensionHandler;
import ru.intertrust.cm.core.util.SpringApplicationContext;

/**
 * Класс, предназначенный для загрузки конфигурации доменных объектов
 * 
 * @author vmatsukevich Date: 5/6/13 Time: 9:36 AM
 */
public class ConfigurationLoader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationLoader.class);

    @Autowired
    private ConfigurationLoadService configurationLoadService;

    @Autowired
    private SpringApplicationContext springApplicationContext;

    @Autowired
    private ConfigurationExtensionProcessor configurationExtensionProcessor;

    @Autowired
    private AccessControlService accessControlService;
    
    private boolean configurationLoaded;

    /*
     * @Autowired private ExtensionService extensionService;
     */

    public ConfigurationLoader() {
    }

    /**
     * Устанавливает {@link #configurationLoadService}
     * 
     * @param configurationLoadService
     *            сервис для работы с конфигурацией доменных объектов
     */
    public void setConfigurationLoadService(
            ConfigurationLoadService configurationLoadService) {
        this.configurationLoadService = configurationLoadService;
    }

    /**
     * Загружает конфигурацию доменных объектов, валидирует и создает
     * соответствующие сущности в базе. Добавляет запись администратора
     * (admin/admin) в таблицу authentication_info.
     * 
     * @throws Exception
     */
    public void load() throws Exception {
        configurationLoadService.loadConfiguration();
        onLoadComplete();
    }

    /**
     * Обновляет конфигурацию доменных объектов, валидирует и создает
     * соответствующие сущности в базе. Добавляет запись администратора
     * (admin/admin) в таблицу authentication_info.
     *
     * @throws Exception
     */
    public void update() throws Exception {
        try {
            configurationLoadService.updateConfiguration();
            onLoadComplete();
        } catch (Throwable throwable) {
            logger.error("Unexpected exception", throwable);
            throw throwable;
        }
    }

    public void applyConfigurationExtensionCleaningOutInvalid() {
        extensionProcessor().applyConfigurationExtensionCleaningOutInvalid();
    }

    public void onLoadComplete(){
        // Вызов точки расширения
        if (springApplicationContext.getContext() != null) {
            ExtensionService extensionService = springApplicationContext.getContext()
                    .getBean(ExtensionService.class);
            OnLoadConfigurationExtensionHandler extension = extensionService.getExtentionPoint(
                    OnLoadConfigurationExtensionHandler.class, null);
            extension.onLoad();
        }

        //Установка флага загруженности конфигурации
        configurationLoaded = true;
    }

    private ConfigurationExtensionProcessor extensionProcessor() {
        final ConfigurationExtensionProcessor configurationExtensionProcessor = (ConfigurationExtensionProcessor) SpringApplicationContext.getContext().getBean("configurationExtensionProcessor");
        configurationExtensionProcessor.setAccessToken(accessControlService.createSystemAccessToken("ConfigurationLoader"));
        return configurationExtensionProcessor;
    }

    /**
     * Метод возвращает флаг загруженности конфигурации
     * @return
     */
    public boolean isConfigurationLoaded(){
        return configurationLoaded;
    }


    /**
     * возращает true если таблица конфигурации существует.
     * @return
     */
    public boolean isConfigurationTableExist(){
        return configurationLoadService.isConfigurationTableExist();
    }
    
}
