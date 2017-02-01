package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcOperations;
import ru.intertrust.cm.core.dao.api.*;
import ru.intertrust.cm.core.dao.impl.utils.DateUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static ru.intertrust.cm.core.dao.impl.utils.DaoUtils.wrap;

/**
 * Реализация {@link ru.intertrust.cm.core.dao.api.InitializationLockDao}
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

    protected static final String UPDATE_LOCK_QUERY =
            "update " + wrap(INITIALIZATION_LOCK_TABLE) + " set " + wrap(START_DATE_COLUMN) + " = ? where " +
                    wrap(ID_COLUMN) + " = ?";

    protected static final String SELECT_FOR_UPDATE_QUERY =
            "select " + wrap(ID_COLUMN) + " from " + wrap(INITIALIZATION_LOCK_TABLE) +
                    " where " +  wrap(ID_COLUMN) + " = ? and " +
                    "(" + wrap(SERVER_ID_COLUMN) +  " is null or " + wrap(START_DATE_COLUMN) + " < ?) for update";

    protected static final String UNLOCK_QUERY =
            "update " + wrap(INITIALIZATION_LOCK_TABLE) + " set " + wrap(SERVER_ID_COLUMN) +  " = null, " +
                    wrap(START_DATE_COLUMN) + " = null where " + wrap(ID_COLUMN) + " = ?";

    protected static final int LOCK_VALIDITY_TIME_SEC = 10;

    private static final long ID = 1;

    @Autowired
    private DomainObjectTypeIdDao domainObjectTypeIdDao;

    @Autowired
    @Qualifier("jdbcTemplate")
    private JdbcOperations jdbcTemplate;

    @Autowired
    private DataStructureDao dataStructureDao;

    @Autowired
    private MD5Service md5Service;

    @Autowired
    private DatabaseInfo databaseInfo;

    private BasicQueryHelper queryHelper;

    public void setJdbcTemplate(JdbcOperations jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void createInitializationLockTable() {
        jdbcTemplate.update(getQueryHelper().generateInitializationLockTableQuery());
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public boolean isInitializationLockTableCreated() {
        return dataStructureDao.isTableExist(INITIALIZATION_LOCK_TABLE);
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void createLockRecord(long serverId) {
        jdbcTemplate.update(INSERT_QUERY, ID, serverId, DateUtils.getGMTDate(new Date()));
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void lock(long serverId) {
        jdbcTemplate.update(LOCK_QUERY, serverId, DateUtils.getGMTDate(new Date()), ID);
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void unlock() {
        jdbcTemplate.update(UNLOCK_QUERY, ID);
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void updateLock() {
        jdbcTemplate.update(UPDATE_LOCK_QUERY, DateUtils.getGMTDate(new Date()), ID);
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public boolean isLocked() {
        Calendar startDate = DateUtils.getGMTDate(new Date());
        startDate.add(Calendar.SECOND, -LOCK_VALIDITY_TIME_SEC);

        List<Integer> unlockedRecords = jdbcTemplate.queryForList(SELECT_FOR_UPDATE_QUERY, Integer.class, ID, startDate);
        return unlockedRecords == null || unlockedRecords.isEmpty();
    }

    protected BasicQueryHelper getQueryHelper() {
        if (queryHelper == null) {
            DatabaseInfo.Vendor dbVendor = databaseInfo.getDatabaseVendor();
            if (DatabaseInfo.Vendor.ORACLE.equals(dbVendor)) {
                return new OracleQueryHelper(domainObjectTypeIdDao, md5Service);
            } else {
                return new PostgreSqlQueryHelper(domainObjectTypeIdDao, md5Service);
            }
        }

        return queryHelper;
    }
}
