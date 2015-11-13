package ru.intertrust.cm.core.dao.impl.utils;

import ru.intertrust.cm.core.config.ConfigurationExplorer;

/**
 * Утилитные методы работы с конфигурацией.
 * @author atsvetkov
 *
 */
public class ConfigurationExplorerUtils {

    /**
     * Поиск самого верхнего родительского типа для переданного типа доменного объекта. Если родительского типа нет -
     * возвращает переданный тип доменного объекта.
     * @param configurationExplorer
     * @param objectType
     * @return
     */
    public static String getTopLevelParentType(ConfigurationExplorer configurationExplorer, String objectType) {
        return configurationExplorer.getDomainObjectRootType(objectType);

    }
}
