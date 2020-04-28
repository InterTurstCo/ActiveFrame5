package ru.intertrust.cm.core.business.impl;

import ru.intertrust.cm.core.business.api.ConfigurationLoadService;

public interface ConfigurationLoader {

    /**
     * Устанавливает {@link ConfigurationLoadService}
     *
     * @param configurationLoadService
     *            сервис для работы с конфигурацией доменных объектов
     */
    void setConfigurationLoadService(ConfigurationLoadService configurationLoadService);

    /**
     * Загружает конфигурацию доменных объектов, валидирует и создает
     * соответствующие сущности в базе. Добавляет запись администратора
     * (admin/admin) в таблицу authentication_info.
     *
     * @throws Exception
     */
    void load() throws Exception;

    /**
     * Обновляет конфигурацию доменных объектов, валидирует и создает
     * соответствующие сущности в базе. Добавляет запись администратора
     * (admin/admin) в таблицу authentication_info.
     *
     * @throws Exception
     */
    void update() throws Exception;

    void applyConfigurationExtensionCleaningOutInvalid();

    void onLoadComplete();

    /**
     * Метод возвращает флаг загруженности конфигурации
     * @return
     */
    boolean isConfigurationLoaded();

    /**
     * возращает true если таблица конфигурации существует.
     * @return
     */
    boolean isConfigurationTableExist();

}
