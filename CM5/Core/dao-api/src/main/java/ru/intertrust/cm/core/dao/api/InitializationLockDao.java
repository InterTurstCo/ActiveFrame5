package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.DomainObjectTypeId;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;

import java.util.List;

/**
 * Dao для INITIALIZATION_LOCK
 * @author vmatsukevich
 *         Date: 8/13/13
 *         Time: 2:42 PM
 */
public interface InitializationLockDao {

    String INITIALIZATION_LOCK_TABLE = "initialization_lock";
    String ID_COLUMN = "id";
    String SERVER_ID_COLUMN = "server_id";
    String START_DATE_COLUMN = "start_date";

    /**
     * Creates INITIALIZATION_LOCK table
     */
    void createInitializationLockTable();

    /**
     * Returns true if INITIALIZATION_LOCK table exists
     */
    boolean isInitializationLockTableCreated();

    /**
     * Creates initialization lock record
     * @param serverId id of the server that performs initialization
     */
    void createLockRecord(long serverId);

    /**
     * Updates initialization lock record with serverId
     * @param serverId id of the server that performs initialization
     */
    void lock(long serverId);

    /**
     * Updates initialization lock record to indicate that initialization is not currently being performed
     */
    void unlock();

    /**
     * Returns true if initialization is in process
     * @return
     */
    boolean isLocked();

    /**
     * Returns true if initialization lock record is created
     * @return
     */
    boolean isLockRecordCreated();
}
