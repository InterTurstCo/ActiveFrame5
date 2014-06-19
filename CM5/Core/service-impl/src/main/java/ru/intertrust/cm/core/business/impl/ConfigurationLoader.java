package ru.intertrust.cm.core.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ConfigurationLoadService;
import ru.intertrust.cm.core.dao.api.ExtensionService;
import ru.intertrust.cm.core.dao.api.extension.OnLoadConfigurationExtensionHandler;
import ru.intertrust.cm.core.util.SpringApplicationContext;

/**
 * Класс, предназначенный для загрузки конфигурации доменных объектов
 * 
 * @author vmatsukevich Date: 5/6/13 Time: 9:36 AM
 */
public class ConfigurationLoader {

    @Autowired
    private ConfigurationLoadService configurationLoadService;

    @Autowired
    private SpringApplicationContext springApplicationContext;
    
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

    /**
     * Обновляет конфигурацию доменных объектов, валидирует и создает
     * соответствующие сущности в базе. Добавляет запись администратора
     * (admin/admin) в таблицу authentication_info.
     *
     * @throws Exception
     */
    public void update() throws Exception {
        configurationLoadService.updateConfiguration();

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
    
    /**
     * Метод возвращает флаг загруженности конфигурации
     * @return
     */
    public boolean isConfigurationLoaded(){
        return configurationLoaded;
    }
    
}
