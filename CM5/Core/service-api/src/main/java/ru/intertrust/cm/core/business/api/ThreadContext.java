package ru.intertrust.cm.core.business.api;

/**
 * Бин контекст потока. Любой код может положить в этот бин переменные а другой код так же может забрать эти переменные.
 * Значения переменных доступны толькор в контексте одного потока
 * @author larin
 *
 */
public interface ThreadContext {
    /**
     * Получить переменную
     * @param key
     * @return
     */
    Object get(String key);
    
    /**
     * Установить переменную
     * @param key
     * @param value
     */
    void set(String key, Object value);
}
