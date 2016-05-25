package ru.intertrust.cm.core.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.CallableStatementCreator;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao;
import ru.intertrust.cm.core.dao.api.MD5Service;
import ru.intertrust.cm.core.dao.api.SqlLoggerEnforcer;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;

/**
 * Реализация {@link ru.intertrust.cm.core.dao.api.DataStructureDao} для PostgreSQL
 * @author vmatsukevich Date: 5/15/13 Time: 4:27 PM
 */
public class OracleDataStructureDaoImpl extends BasicDataStructureDaoImpl {

    private static final Logger logger = LoggerFactory.getLogger(OracleDataStructureDaoImpl.class);

    @Autowired
    private SqlLoggerEnforcer sqlLoggerEnforcer;

    /**
     * Смотри {@link ru.intertrust.cm.core.dao.api.DataStructureDao#gatherStatistics()}
     */
    @Override
    public void gatherStatistics() {
        sqlLoggerEnforcer.forceSqlLogging();

        jdbcTemplate.call(new CallableStatementCreator() {
            @Override
            public CallableStatement createCallableStatement(Connection connection) throws SQLException {

                return connection.prepareCall(getQueryHelper().generateGatherStatisticsQuery());
            }
        }, Collections.EMPTY_LIST);

        sqlLoggerEnforcer.cancelSqlLoggingEnforcement();
    }

    @Override
    protected BasicQueryHelper createQueryHelper(DomainObjectTypeIdDao domainObjectTypeIdDao, MD5Service md5Service) {
        return new OracleQueryHelper(domainObjectTypeIdDao, md5Service);
    }

    @Override
    protected String generateSelectTableIndexes() {
        return "select i.index_name, i.column_name from all_ind_columns i where i.table_name = ?";
    }

    @Override
    protected String generateCountTableIndexes() {
        return "select count(distinct i.index_name) as indexes_count from all_ind_columns i where i.table_name = ?";
    }

    @Override
    protected String generateCountTableUniqueKeys() {
        return "select count(distinct uc.constraint_name) as unique_keys_count from user_constraints uc " +
                "where uc.constraint_type = 'U' and uc.table_name = ?";
    }

    @Override
    protected String generateCountTableForeignKeys() {
        return "select count(distinct uc.constraint_name) as foreign_keys_count from user_constraints uc " +
                "where uc.constraint_type = 'R' and uc.table_name = ?";
    }

    protected String generateSelectTableForeignKeys() {
        return "select distinct uc.constraint_name as foreign_key_name from user_constraints uc " +
                "where uc.constraint_type = 'R' and uc.table_name = ?";
    }
}
