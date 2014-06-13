package ru.intertrust.cm.core.business.api;

import ru.intertrust.cm.core.config.ConfigurationException;

/**
 * Сервис загрузки и работы с конфигурацией доменных объектов
 * @author vmatsukevich
 *         Date: 5/15/13
 *         Time: 4:32 PM
 */
public interface ConfigurationLoadService {
    public interface Remote extends ConfigurationLoadService {
    }

    /**
     * Загрузка конфигурации
     */
    void loadConfiguration();
}
