package ru.intertrust.cm.core.dao.impl.utils;

import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;

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
        DomainObjectTypeConfig domainObjectTypeConfig = configurationExplorer.getConfig(DomainObjectTypeConfig.class, objectType);        
        if (domainObjectTypeConfig != null && domainObjectTypeConfig.getExtendsAttribute() != null) {
            String parentType = domainObjectTypeConfig.getExtendsAttribute();
            return getTopLevelParentType(configurationExplorer, parentType);
        }else{
            return objectType;
        }
        
    }
}
