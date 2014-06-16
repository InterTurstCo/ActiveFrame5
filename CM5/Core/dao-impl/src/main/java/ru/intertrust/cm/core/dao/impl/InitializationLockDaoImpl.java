package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao;
import ru.intertrust.cm.core.dao.api.InitializationLockDao;
import ru.intertrust.cm.core.dao.impl.utils.DateUtils;

import java.util.Date;

import static ru.intertrust.cm.core.dao.impl.utils.DaoUtils.wrap;

/**
 * Реализация {@link ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao}
 * @author vmatsukevich
 *         Date: 8/13/13
 *         Time: 2:33 PM
 */
public class InitializationLockDaoImpl implements InitializationLockDao {

    protected static final String INSERT_QUERY =
            "insert into " + wrap(INITIALIZATION_LOCK_TABLE) + "(" +
                    wrap(ID_COLUMN) + ", " + wrap(SERVER_ID_COLUMN) + ", " + wrap(START_DATE_COLUMN) +
                    ") values (?, ?, ?)";

    protected static final String LOCK_QUERY =
            "update " + wrap(INITIALIZATION_LOCK_TABLE) + " set " + wrap(SERVER_ID_COLUMN) +  " = ?, " +
                    wrap(START_DATE_COLUMN) + " = ? where " + wrap(ID_COLUMN) + " = ?";

    protected static final String UNLOCK_QUERY =
            "update " + wrap(INITIALIZATION_LOCK_TABLE) + " set " + wrap(SERVER_ID_COLUMN) +  " = null, " +
                    wrap(START_DATE_COLUMN) + " = null where " + wrap(ID_COLUMN) + " = ?";

    protected static final String COUNT_ALL =
            "select count( " + wrap(ID_COLUMN) + ") from " + wrap(INITIALIZATION_LOCK_TABLE);

    protected static final String COUNT_FREE_QUERY =
            "select count( " + wrap(ID_COLUMN) + ") from " + wrap(INITIALIZATION_LOCK_TABLE) +
                    " where " + wrap(ID_COLUMN) + " = ? and " + wrap(SERVER_ID_COLUMN) + " is null";

    private static final long ID = 1;

    @Autowired
    private DomainObjectTypeIdDao domainObjectTypeIdDao;

    @Autowired
    private JdbcOperations jdbcTemplate;

    private BasicQueryHelper queryHelper;

    public void setJdbcTemplate(JdbcOperations jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void createInitializationLockTable() {
        jdbcTemplate.update(getQueryHelper().generateInitializationLockTableQuery());
    }

    @Override
    public void createLockRecord(long serverId) {
        jdbcTemplate.update(INSERT_QUERY, ID, serverId, DateUtils.getGMTDate(new Date()));
    }

    @Override
    public void lock(long serverId) {
        jdbcTemplate.update(LOCK_QUERY, serverId, DateUtils.getGMTDate(new Date()), ID);
    }

    @Override
    public void unlock(long serverId) {
        jdbcTemplate.update(UNLOCK_QUERY, ID);
    }

    @Override
    public boolean isLocked() {
        Integer freeRecordsCount = jdbcTemplate.queryForObject(COUNT_FREE_QUERY, Integer.class, ID);
        return freeRecordsCount == 0;
    }

    @Override
    public boolean isLockRecordCreated() {
        Integer recordsCount = jdbcTemplate.queryForObject(COUNT_ALL, Integer.class);
        return recordsCount > 0;
    }

    protected BasicQueryHelper getQueryHelper() {
        if (queryHelper == null) {
            queryHelper = new PostgreSqlQueryHelper(domainObjectTypeIdDao);
        }

        return queryHelper;
    }
}
