package ru.intertrust.cm.core.dao.api.extension;

/**
 * Инициализация кэшей серверов НЕ менеджеров кластера
 * Запускается после загрузки конфигурации
 */
public interface NotManagerDataLoadApplicationInitializer extends ExtensionPointHandler {
    void notManagerinitialize();
}
