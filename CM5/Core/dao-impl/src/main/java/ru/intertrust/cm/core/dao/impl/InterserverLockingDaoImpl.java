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
        
        int rowCount = getJdbcOperations().update("update locks set lock_time = ? where resource_id = ? and lock_time is null", lockTime, resourceId);
        if (rowCount == 0) {
            rowCount = getJdbcOperations().update("insert into locks values(?, ?)", resourceId, lockTime);
        }
        
        return rowCount > 0;
    }

    private void createTableIfNeeded() {
        if (!isTableCreated) {
            getJdbcOperations().execute(
                    "create table if not exists locks (resource_id varchar(256), lock_time timestamp, stamp_info text, constraint pk_locks primary key (resource_id))");
            
            // Добавление stamp_info.
            // Так как миграция выполняется после установки блокировки не возможно использовать migration.xml, приходится изменять таблицу здесь
            String query = "do $$ begin  \r\n" + 
                    "alter table locks add column stamp_info text;\r\n" + 
                    "exception when duplicate_column then raise notice 'column \"stamp_info\" already exists in \"locks\"'; \r\n" + 
                    "end; $$";                      
            getJdbcOperations().execute(query);
            
            isTableCreated = true;
        }
    }

    protected JdbcOperations getJdbcOperations() {
        return jdbcOperations;
    }

    @Override
    public void unlock(String resourceId, String stampInfo) {
        getJdbcOperations().update("update locks set stamp_info = ?, lock_time = null where resource_id = ?", stampInfo, resourceId);
    }

    @Override
    public Date getLastLockTime(String resourceId) {
        createTableIfNeeded();
        return getJdbcOperations().queryForObject("select max(lock_time) from locks where resource_id = ? and lock_time is not null", Date.class, resourceId);
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
        return getJdbcOperations().update("update locks set lock_time = null where resource_id = ? and lock_time = ?", resourceId, lockTime) > 0;
    }

    @Override
    public String getStampInfo(String resourceId) {
        return getJdbcOperations().queryForObject("select max(stamp_info) from locks where resource_id = ?", String.class, resourceId);
    }
}
