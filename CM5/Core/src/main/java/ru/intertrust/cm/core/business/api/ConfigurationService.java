package ru.intertrust.cm.core.business.api;

import ru.intertrust.cm.core.config.BusinessObjectConfig;
import ru.intertrust.cm.core.config.Configuration;

/**
 * Сервис загрузки и работы с конфигурацией бизнес-объектов
 * @author vmatsukevich
 *         Date: 5/15/13
 *         Time: 4:32 PM
 */
public interface ConfigurationService {

    /**
     * Загрузка конфигурации
     * @param configuration конфигурация бизнес-объектов
     */
    void loadConfiguration(Configuration configuration);
    
    void loadSystemObjectConfig(BusinessObjectConfig businessObjectConfig);
    
    boolean isSystemObjectLoaded(BusinessObjectConfig businessObjectConfig);
}
