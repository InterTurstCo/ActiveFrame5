package ru.intertrust.cm.core.dao.impl.clusterlock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import ru.intertrust.cm.core.dao.api.clusterlock.ClusteredLock;
import ru.intertrust.cm.core.dao.api.clusterlock.ClusteredLockDao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClusteredLockDaoImpl implements ClusteredLockDao {
    @Autowired
    @Qualifier("jdbcTemplate")
    private JdbcOperations jdbcOperations;

    @Autowired
    public void init() {
        String query = "create table if not exists clustered_lock (";
        query += "category varchar(512),";
        query += "name varchar(512),";
        query += "tag varchar(512),";
        query += "owner varchar(512),";
        query += "lock_time timestamp,";
        query += "duration bigint,";
        query += "stamp_info text,";
        query += "constraint pk_clustered_lock primary key (category, name)";
        query += ")";
        jdbcOperations.execute(query);
    }

    @Override
    public ClusteredLock create(String category, String name, String tag, String owner, Instant lockTime, Duration duration, String stampInfo) {
        String query = "insert into clustered_lock ";
        query += "(category, name, tag, owner, lock_time, duration, stamp_info)";
        query += " values";
        query += "(?, ?, ?, ?, ?, ?, ?)";

        jdbcOperations.update(query, category, name, tag, owner, lockTime, duration.toMillis(), stampInfo);

        return new ClusteredLockImpl(name, category, lockTime != null, owner, lockTime, duration, tag, stampInfo);
    }

    @Override
    public ClusteredLock update(String category, String name, String tag, String owner, Instant lockTime, Duration duration, String stampInfo) {
        String query = "update clustered_lock ";
        query += "set tag=?, owner=?, lock_time=?, duration=?, stamp_info=?";
        query += " where ";
        query += "category=? and name=?";

        jdbcOperations.update(query, tag, owner, lockTime, duration, stampInfo, category, name);

        return new ClusteredLockImpl(name, category, lockTime != null, owner, lockTime, duration, tag, stampInfo);
    }

    @Override
    public ClusteredLock find(String category, String name, boolean lock) {
        String query = "select category, name, tag, owner, lock_time, duration, stamp_info ";
        query += "from clustered_lock";
        query += " where ";
        query += "category=? and name=?";
        if (lock){
            query += " for update";
        }

        List<ClusteredLock> result = jdbcOperations.query(query, new ClusteredLockRowMapper(), category, name);
        return result.size() > 0 ? result.get(0) : null;
    }

    @Override
    public Set<ClusteredLock> findAll(String category) {
        String query = "select category, name, tag, owner, lock_time, duration, stamp_info ";
        query += "from clustered_lock";
        query += " where ";
        query += "category=?";

        List<ClusteredLock> result = jdbcOperations.query(query, new ClusteredLockRowMapper(), category);
        return new HashSet<ClusteredLock>(result);
    }

    @Override
    public void delete(String category, String name) {
        String query = "delete from clustered_lock ";
        query += " where ";
        query += "category=? and name=?";

        jdbcOperations.update(query, category, name);
    }

    public static class ClusteredLockRowMapper implements RowMapper<ClusteredLock> {

        @Override
        public ClusteredLock mapRow(ResultSet rs, int rowNum) throws SQLException {
            ClusteredLockImpl result = new ClusteredLockImpl();
            result.setName(rs.getString("name"));
            result.setCategory(rs.getString("category"));
            result.setOwner(rs.getString("owner"));
            result.setAutoUnlockTimeout(Duration.ofMillis(rs.getLong("duration")));
            Timestamp lockTime = rs.getTimestamp("lock_time");
            if (lockTime != null) {
                result.setLockTime(Instant.ofEpochMilli(lockTime.getTime()));
                result.setLocked(true);
            } else {
                result.setLocked(false);
            }
            result.setTag(rs.getString("tag"));
            result.setStampInfo(rs.getString("stamp_info"));
            return result;
        }
    }
}
