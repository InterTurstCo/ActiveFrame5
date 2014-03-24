package ru.intertrust.cm.core.dao.impl.utils;

import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.impl.CollectionQueryInitializerImpl;

/**
 * Oracle-специфичная имплементация {@link ru.intertrust.cm.core.dao.impl.CollectionQueryInitializer }
 * @author vmatsukevich
 *
 */
public class OracleCollectionQueryInitializerImpl extends CollectionQueryInitializerImpl {

    public OracleCollectionQueryInitializerImpl(ConfigurationExplorer configurationExplorer) {
        super(configurationExplorer);
    }

    @Override
    protected String applyOffsetAndLimit(String query, int offset, int limit) {
        if (limit == 0) {
            return query;
        }

        return "select * from (" + query + ") where rownum >= " + offset + " and rownum < " + (offset + limit);
    }
}
