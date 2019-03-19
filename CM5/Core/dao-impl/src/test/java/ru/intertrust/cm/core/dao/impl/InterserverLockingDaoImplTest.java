package ru.intertrust.cm.core.dao.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.jdbc.core.JdbcOperations;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;

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
        when(jdbcOperations.update(anyString(), any(Object[].class))).then(new Answer<Object>() {
            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                CCJSqlParserUtil.parse(invocation.getArgumentAt(0, String.class));
                return 0;
            }
        });
        when(jdbcOperations.queryForObject(anyString(), eq(Date.class), any())).then(new Answer<Object>() {
            @Override
            public Date answer(InvocationOnMock invocation) throws Throwable {
                CCJSqlParserUtil.parse(invocation.getArgumentAt(0, String.class));
                return new Date();
            }
        });
    }

    @Test
    public void testLockNoTable() {
        Date time = new Date();
        interserverLockingDaoImpl.lock("abc", time);
        verify(jdbcOperations).execute(
                "create table if not exists locks (resource_id varchar(256), lock_time timestamp, constraint pk_locks primary key (resource_id))");
        verify(jdbcOperations).update("insert into locks values(?, ?)", "abc", time);
    }

    @Test
    public void testCreateTableOnlyOnce() {
        Date time = new Date();
        interserverLockingDaoImpl.lock("abc", time);
        interserverLockingDaoImpl.lock("def", time);
        verify(jdbcOperations, times(1)).execute(
                "create table if not exists locks (resource_id varchar(256), lock_time timestamp, constraint pk_locks primary key (resource_id))");
        verify(jdbcOperations).update("insert into locks values(?, ?)", "abc", time);
        verify(jdbcOperations).update("insert into locks values(?, ?)", "def", time);
    }

    @Test
    public void testUnlock() {
        interserverLockingDaoImpl.unlock("abc");
        verify(jdbcOperations).update("delete from locks where resource_id = ?", "abc");
    }

    @Test
    public void testUnlockWithDateSpecified() {
        Date time = new Date();
        interserverLockingDaoImpl.unlock("abc", time);
        verify(jdbcOperations).update("delete from locks where resource_id = ? and lock_time = ?", "abc", time);
    }

    @Test
    public void testUpdateLock() {
        Date time = new Date();
        interserverLockingDaoImpl.updateLock("abc", time);
        verify(jdbcOperations).update("update locks set lock_time = ? where resource_id = ?", time, "abc");
    }

    @Test
    public void testGetLastLockTime() {
        interserverLockingDaoImpl.getLastLockTime("abc");
        verify(jdbcOperations).execute(
                "create table if not exists locks (resource_id varchar(256), lock_time timestamp, constraint pk_locks primary key (resource_id))");
        verify(jdbcOperations).queryForObject("select max(lock_time) from locks where resource_id = ?", Date.class, "abc");
    }
}
