package ru.intertrust.cm.core.business.impl;

/**
 * Интерфейс для инициализатора приложения, гарантирующего глобальную синхронизацию инициализации для нескольких узлов в кластере
 */
public interface GloballyLockableInitializer {

    interface Remote extends GloballyLockableInitializer {}

    /**
     * Выолняет инициализацию приложения, глобально синхронизированную между узлами в кластере
     */
    void init();
}
