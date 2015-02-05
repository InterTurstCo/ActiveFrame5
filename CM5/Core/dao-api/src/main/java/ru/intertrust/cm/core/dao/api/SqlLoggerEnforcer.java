package ru.intertrust.cm.core.dao.api;

/**
 * Сервис принудительного логирования выполняемых к базе данных запросов
 */
public interface SqlLoggerEnforcer {

    /**
     * Включает принудительное логирование запросов
     */
    void forceSqlLogging();

    /**
     * Выключает принудительное логирование запросов
     */
    void cancelSqlLoggingEnforcement();

    /**
     * Определяет включено ли принудительное логирование запросов
     * @return true, если включено принудительное логирование запросов, в противном случае - false
     */
    boolean isSqlLoggingEnforced();
}
