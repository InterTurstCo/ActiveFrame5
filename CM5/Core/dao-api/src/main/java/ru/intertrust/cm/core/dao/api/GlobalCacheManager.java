package ru.intertrust.cm.core.dao.api;

import java.io.Serializable;
import java.util.Map;

/**
 * Управляющий интерфейс для глобального кэша
 * @author Denis Mitavskiy
 *         Date: 22.10.2015
 *         Time: 11:39
 */
public interface GlobalCacheManager {
    /**
     * Устанавливает настройки, специфичные для реализации глобального кэша. Например, в реализации по умолчанию присутствуют такие настройки:
     * global.cache.mode - String, режим работы глобального кэша (значения blocking/non-blocking)
     * global.cache.max.size - Long, максимальный размер кэша в байтах
     * @param cacheSettings карта с настройками
     */
    void applySettings(Map<String, Serializable> cacheSettings);

    /**
     * Возвращает настройки, специфичные для реализации глобального кэша. Например, в реализации по умолчанию присутствуют такие настройки:
     * global.cache.mode - String, режим работы глобального кэша (значения blocking/non-blocking)
     * global.cache.max.size - Long, максимальный размер кэша в байтах
     */
    Map<String, Serializable> getSettings();

    /**
     * Включает или выключает глобальный кэш (если это возможно). Возвращает состояние после попытки провести операцию
     * @param enabled включить или выключить глобальный кэш
     * @return включен или выключен глобальный кэш после вызова метода
     */
    boolean setEnabled(boolean enabled);

    /**
     * Возвращает текущее состояние глобального кэша
     * @return true если кэш включен
     */
    boolean isEnabled();

    /**
     * Устанавливает текущее состояние отладочного режима
     * @param enabled включить или выключить режим отладки глобального кэша
     */
    void setDebugEnabled(boolean enabled);

    /**
     * Возвращает текущее состояние отладочного режима
     * @return true если отладочный режим включен
     */
    boolean isDebugEnabled();

    /**
     * Устанавливает текущее состояние режима сбора расширенной статистики. Сбор расширенной статистики снижает производительность глобального кэша
     * @param enabled включить или выключить режим сбора расширенной статистики.
     * @return true, если режим включен после выполнения данной операции
     */
    boolean setExtendedStatisticsEnabled(boolean enabled);

    /**
     * Возвращает текущее состояние режима сбора расширенной статистики
     * @return true если режим сбора расширенной статистики включен
     */
    boolean isExtendedStatisticsEnabled();

    boolean isCacheAvailable();
}
