package ru.intertrust.cm.core.dao.impl.utils;

import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.impl.CollectionQueryInitializerImpl;
import ru.intertrust.cm.core.dao.impl.DomainObjectQueryHelper;

/**
 * Oracle-специфичная имплементация {@link ru.intertrust.cm.core.dao.impl.CollectionQueryInitializer }
 * @author vmatsukevich
 *
 */
public class OracleCollectionQueryInitializerImpl extends CollectionQueryInitializerImpl {

    public OracleCollectionQueryInitializerImpl(ConfigurationExplorer configurationExplorer, UserGroupGlobalCache userGroupCache,
            CurrentUserAccessor currentUserAccessor, DomainObjectQueryHelper domainObjectQueryHelper) {
        super(configurationExplorer, userGroupCache, currentUserAccessor, domainObjectQueryHelper);
    }

    @Override
    protected String applyOffsetAndLimit(String query, int offset, int limit) {
        if (limit != 0 && offset != 0) {
            return "select * from (" + query + ") where rownum >= " + offset + " and rownum < " + (offset + limit);

        } else if (limit != 0) {
            return "select * from (" + query + ") where rownum < " + limit;
        } else if (offset != 0) {
            return "select * from (" + query + ") where rownum >= " + offset;
        } else {
            return query;
        }
    }
}
