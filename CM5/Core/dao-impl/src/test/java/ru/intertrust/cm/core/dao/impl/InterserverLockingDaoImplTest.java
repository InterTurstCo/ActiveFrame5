package ru.intertrust.cm.core.dao.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcOperations;

public class InterserverLockingDaoImplTest {

    private InterserverLockingDaoImpl interserverLockingDaoImpl = new InterserverLockingDaoImpl() {
        @Override
        protected JdbcOperations getJdbcOperations() {
            return jdbcOperations;
        }
    };
    private JdbcOperations jdbcOperations = mock(JdbcOperations.class);

    @Before
    public void setUp() {

    }

    @Test
    public void testLockNoTable() {
        Date time = new Date();
        interserverLockingDaoImpl.lock("abc", time);
        verify(jdbcOperations).execute(
                "create table if not exists locks (resource_id varchar(256), lock_time timestamp, constraint pk_locks primary key (resource_id))");
        verify(jdbcOperations).update("insert into locks values(?, ?) where not exists (select * from locks where resource_id = ?)", "abc", time, "abc");
    }

    @Test
    public void testCreateTableOnlyOnce() {
        Date time = new Date();
        interserverLockingDaoImpl.lock("abc", time);
        interserverLockingDaoImpl.lock("def", time);
        verify(jdbcOperations, times(1)).execute(
                "create table if not exists locks (resource_id varchar(256), lock_time timestamp, constraint pk_locks primary key (resource_id))");
        verify(jdbcOperations).update("insert into locks values(?, ?) where not exists (select * from locks where resource_id = ?)", "abc", time, "abc");
        verify(jdbcOperations).update("insert into locks values(?, ?) where not exists (select * from locks where resource_id = ?)", "def", time, "def");
    }

    @Test
    public void testUnlock() {
        interserverLockingDaoImpl.unlock("abc");
        verify(jdbcOperations).update("delete from locks where resouce_id = ?", "abc");
    }

    @Test
    public void testUnlockWithDateSpecified() {
        Date time = new Date();
        interserverLockingDaoImpl.unlock("abc", time);
        verify(jdbcOperations).update("delete from locks where resouce_id = ? and lock_time = ?", "abc", time);
    }

    @Test
    public void testUpdateLock() {
        Date time = new Date();
        interserverLockingDaoImpl.updateLock("abc", time);
        verify(jdbcOperations).update("update locks set lock_time = ?", time);
    }

    @Test
    public void testGetLastLockTime() {
        interserverLockingDaoImpl.getLastLockTime("abc");
        verify(jdbcOperations).execute(
                "create table if not exists locks (resource_id varchar(256), lock_time timestamp, constraint pk_locks primary key (resource_id))");
        verify(jdbcOperations).queryForObject("select lock_time from locks where resource_id = ?", Date.class, "abc");
    }
}
