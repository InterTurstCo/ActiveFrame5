package ru.intertrust.cm.globalcache.api;

import ru.intertrust.cm.core.dao.access.UserSubject;
import ru.intertrust.cm.core.dao.api.DomainEntitiesCloner;

/**
 * @author Denis Mitavskiy
 *         Date: 13.08.2015
 *         Time: 17:06
 */
public abstract class CollectionSubKey {
    public final UserSubject subject;
    public final int offset;
    public final int limit;

    public CollectionSubKey(UserSubject subject, int offset, int limit) {
        this.subject = subject;
        this.offset = offset;
        this.limit = limit;
    }

    public abstract int getKeyEntriesQty();

    public abstract CollectionSubKey getCopy(DomainEntitiesCloner cloner);
}
