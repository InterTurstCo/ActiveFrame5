package ru.intertrust.cm.core.dao.impl;

import java.util.Date;

import org.springframework.jdbc.core.JdbcOperations;

import ru.intertrust.cm.core.dao.api.InterserverLockingDao;

public class InterserverLockingDaoImpl implements InterserverLockingDao {

    private volatile boolean isTableCreated;
    private JdbcOperations jdbcOperations;

    @Override
    public boolean lock(String resourceId, Date lockTime) {
        createTableIfNeeded();
        return getJdbcOperations().update("insert into locks values(?, ?) where not exists (select * from locks where resource_id = ?)", resourceId, lockTime,
                resourceId) > 0;
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
        getJdbcOperations().update("delete from locks where resouce_id = ?", resourceId);
    }

    @Override
    public Date getLastLockTime(String resourceId) {
        createTableIfNeeded();
        return getJdbcOperations().queryForObject("select lock_time from locks where resource_id = ?", Date.class, resourceId);
    }

    @Override
    public void updateLock(String resourceId, Date lockTime) {
        getJdbcOperations().update("update locks set lock_time = ?", lockTime);
    }

    @Override
    public boolean unlock(String resourceId, Date lockTime) {
        return getJdbcOperations().update("delete from locks where resouce_id = ? and lock_time = ?", resourceId, lockTime) > 0;
    }
}
