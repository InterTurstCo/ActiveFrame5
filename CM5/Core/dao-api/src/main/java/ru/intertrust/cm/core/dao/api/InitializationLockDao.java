package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.DomainObjectTypeId;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;

import java.util.List;

/**
 * Dao для DOMAIN_OBJECT_TYPE_ID
 * @author vmatsukevich
 *         Date: 8/13/13
 *         Time: 2:42 PM
 */
public interface InitializationLockDao {

    String INITIALIZATION_LOCK_TABLE = "initialization_lock";
    String ID_COLUMN = "id";
    String SERVER_ID_COLUMN = "server_id";
    String START_DATE_COLUMN = "start_date";

    void createInitializationLockTable();

    void createLockRecord(long serverId);

    void lock(long serverId);

    void unlock(long serverId);

    boolean isLocked();

    boolean isLockRecordCreated();
}
