package ru.intertrust.cm.core.business.api;

import ru.intertrust.cm.core.config.ConfigurationException;

/**
 * Сервис загрузки и работы с конфигурацией доменных объектов
 * @author vmatsukevich
 *         Date: 5/15/13
 *         Time: 4:32 PM
 */
public interface ConfigurationControlService {

    public interface Remote extends ConfigurationControlService {

    }
    /**
     * Обновляет конфигурацию системы фрагментом конфигурации {@code configurationString}.
     * Обновляются только те части конфигурации, изменение которых не требует изменений структуры базы данных.
     * Например, изменения конфигурации доменных объектов будут проигнорированны,
     * а изменения конфигурации коллекций будут обработаны.
     * @param configurationString обновляемый фрагмент конфигурации
     * @throws ru.intertrust.cm.core.config.ConfigurationException
     */
    void updateConfiguration(String configurationString, String fileName) throws ConfigurationException;

    /**
     * Проверяет требуется ли рестарт приложения для полного апдейта конфигурации
     * @param configurationString строка конфигурации
     * @return true, если требуется перезагрузка, false - в противном случае
     */
    @Deprecated // marked for removal
    boolean restartRequiredForFullUpdate(String configurationString);
}
