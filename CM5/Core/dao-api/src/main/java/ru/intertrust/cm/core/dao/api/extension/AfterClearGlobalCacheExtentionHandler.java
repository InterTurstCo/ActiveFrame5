package ru.intertrust.cm.core.dao.api.extension;

/**
 * Точка расширения на операцию сброса глобального кэша. Обычно используется
 * для сбросов локальных кэшей сервисов
 */
public interface AfterClearGlobalCacheExtentionHandler extends ExtensionPointHandler{
    /**
     * Выполнение действия при сбросе глобального кэша
     */
    void onClearGlobalCache();
}
