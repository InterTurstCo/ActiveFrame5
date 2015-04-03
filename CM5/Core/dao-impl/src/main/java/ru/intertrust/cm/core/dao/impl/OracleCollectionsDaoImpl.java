package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.impl.utils.OracleCollectionQueryInitializerImpl;

/**
 * Oracle-специфичная имплементация {@link ru.intertrust.cm.core.dao.api.CollectionsDao }
 * @author vmatsukevich
 *
 */
public class OracleCollectionsDaoImpl extends CollectionsDaoImpl {

    @Override
    protected CollectionQueryInitializer createCollectionQueryInitializer(ConfigurationExplorer configurationExplorer) {
        return new OracleCollectionQueryInitializerImpl(configurationExplorer, getUserGroupCache(), getCurrentUserAccessor());
    }
}
