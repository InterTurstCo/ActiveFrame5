package ru.intertrust.cm.core.dao.api;

/**
 * Сервис грифов доступа
 */
public interface SecurityStamp {
    public static final String STAMPED_TYPES_CONGIG_NAME = "STAMPED_TYPES";

    /**
     * Поддерживает ли тип ограничения прав по грфам доступа
     * @param typeName
     * @return
     */
    boolean isSupportSecurityStamp(String typeName);
}
