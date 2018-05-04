package ru.intertrust.cm.core.business.impl;

/**
 * Интерфейс для инициализатора приложения, гарантирующего глобальную синхронизацию инициализации для нескольких узлов в кластере
 */
public interface GloballyLockableInitializer {

    interface Remote extends GloballyLockableInitializer {}

    /**
     * Выполняет блокировку перед вызовом init в случае если текущий сервер ведущий.
     * @throws Exception
     */
    void start() throws Exception;

    /**
     * Снимает блокировку выставленную в init в случае если текущий сервер ведущий.
     * @throws Exception
     */
    void finish() throws Exception;

}
