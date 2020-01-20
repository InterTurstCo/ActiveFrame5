package ru.intertrust.cm.globalcache.impl.localjvm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.StampedLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.DomainObjectsModification;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.Pair;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.UserSubject;
import ru.intertrust.cm.core.dao.dto.CollectionTypesKey;
import ru.intertrust.cm.globalcache.api.AccessChanges;
import ru.intertrust.cm.globalcache.api.CollectionSubKey;

public class StampedSynchronizedGlobalCacheImpl extends LockManagerGlobalCacheImpl {
    private static final Logger logger = LoggerFactory.getLogger(StampedSynchronizedGlobalCacheImpl.class);

    @Autowired
    private StampedLockManager lockManager;

    @Override
    protected LockManager getLockManager() {
        return lockManager;
    }
}
