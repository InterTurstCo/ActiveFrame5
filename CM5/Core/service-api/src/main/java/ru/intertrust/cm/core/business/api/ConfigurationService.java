package ru.intertrust.cm.core.business.api;

/**
 * Сервис загрузки и работы с конфигурацией доменных объектов
 * @author vmatsukevich
 *         Date: 5/15/13
 *         Time: 4:32 PM
 */
public interface ConfigurationService {

    interface Remote extends ConfigurationService {}

    /**
     * Загрузка конфигурации
     */
    void loadConfiguration();
}
