package ru.intertrust.cm.core.dao.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcOperations;

import ru.intertrust.cm.core.dao.api.InterserverLockingDao;

public class InterserverLockingDaoImpl implements InterserverLockingDao {

    private volatile boolean isTableCreated;

    @Autowired
    @Qualifier("jdbcTemplate")
    private JdbcOperations jdbcOperations;

    @Override
    public boolean lock(String resourceId, Date lockTime) {
        createTableIfNeeded();
        return getJdbcOperations().update("insert into locks values(?, ?)", resourceId, lockTime) > 0;
    }

    private void createTableIfNeeded() {
        if (!isTableCreated) {
            getJdbcOperations().execute(
                    "create table if not exists locks (resource_id varchar(256), lock_time timestamp, constraint pk_locks primary key (resource_id))");
            isTableCreated = true;
        }
    }

    protected JdbcOperations getJdbcOperations() {
        return jdbcOperations;
    }

    @Override
    public void unlock(String resourceId) {
        getJdbcOperations().update("delete from locks where resource_id = ?", resourceId);
    }

    @Override
    public Date getLastLockTime(String resourceId) {
        createTableIfNeeded();
        return getJdbcOperations().queryForObject("select max(lock_time) from locks where resource_id = ?", Date.class, resourceId);
    }

    @Override
    public void updateLock(String resourceId, Date lockTime) {
        getJdbcOperations().update("update locks set lock_time = ? where resource_id = ?", lockTime, resourceId);
    }

    @Override
    public void updateLock(String resourceId, Date oldLockTime, Date lockTime) {
        getJdbcOperations().update("update locks set lock_time = ? where resource_id = ? and lock_time = ?", lockTime, resourceId, oldLockTime);
    }

    @Override
    public boolean unlock(String resourceId, Date lockTime) {
        return getJdbcOperations().update("delete from locks where resource_id = ? and lock_time = ?", resourceId, lockTime) > 0;
    }
}
