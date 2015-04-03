package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.dao.api.SqlLoggerEnforcer;

/**
 * Реализация {@link SqlLoggerEnforcer}
 */
public class SqlLoggerEnforcerImpl implements SqlLoggerEnforcer {

    private static ThreadLocal<Boolean> enforceLogging = new ThreadLocal<>();

    /**
     * {link SqlLoggerEnforcer#forceSqlLogging}
     */
    @Override
    public void forceSqlLogging() {
        enforceLogging.set(true);
    }

    /**
     * {link SqlLoggerEnforcer#cancelSqlLoggingEnforcement}
     */
    @Override
    public void cancelSqlLoggingEnforcement() {
        enforceLogging.set(null);
    }

    /**
     * {link SqlLoggerEnforcer#isSqlLoggingEnforced}
     */
    @Override
    public boolean isSqlLoggingEnforced() {
        Boolean value = enforceLogging.get();
        return value != null && value;
    }


}
