package ru.intertrust.cm.core.dao.impl;

import java.util.Date;

import org.springframework.jdbc.core.JdbcOperations;

import ru.intertrust.cm.core.dao.api.InterserverLockingDao;

public class InterserverLockingDaoImpl implements InterserverLockingDao {

    private volatile boolean isTableCreated;
    private JdbcOperations jdbcOperations;

    @Override
    public void lock(String resourceId, Date lockTime) {
        if (!isTableCreated) {
            getJdbcOperations().execute(
                    "create table if not exists locks (resource_id varchar(256), lock_time timestamp, constraint pk_locks primary key (resource_id))");
            isTableCreated = true;
        }
        long n = getJdbcOperations().queryForObject("select count(*) from locks where resource_id = ?", Long.class, resourceId);
        if (n == 0) {
            getJdbcOperations().update("insert into locks values(?, ?) where not exists (select * from locks where resource_id = ?)", resourceId, lockTime,
                    resourceId);
        } else {
            getJdbcOperations().update("update locks set lock_time = ?", lockTime);
        }

    }

    protected JdbcOperations getJdbcOperations() {
        return jdbcOperations;
    }

    @Override
    public boolean wasLockedAfter(String resourceId, Date time) {
        return getJdbcOperations().queryForObject("select count(*) from locks where resource_id = ? and lock_time > ?", Long.class, resourceId, time) > 0;
    }

    @Override
    public void unlock(String resourceId) {
        getJdbcOperations().update("delete from locks where resouce_id = ?", resourceId);
    }

    @Override
    public Date getLastLockTime(String resourceId) {
        return getJdbcOperations().queryForObject("select lock_time from locks where resource_id = ?", Date.class, resourceId);
    }
}
