package ru.intertrust.cm.core.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao;

/**
 * Реализация {@link ru.intertrust.cm.core.dao.api.DataStructureDao} для PostgreSQL
 * @author vmatsukevich Date: 5/15/13 Time: 4:27 PM
 */
public class PostgreSqlDataStructureDaoImpl extends BasicDataStructureDaoImpl {
    private static final Logger logger = LoggerFactory.getLogger(PostgreSqlDataStructureDaoImpl.class);

    @Override
    protected BasicQueryHelper createQueryHelper(DomainObjectTypeIdDao domainObjectTypeIdDao) {
        return new PostgreSqlQueryHelper(domainObjectTypeIdDao);
    }

    @Override
    protected String generateDoesTableExistQuery() {
        return "select count(*) FROM information_schema.tables WHERE table_schema = 'public' and table_name = ?";
    }

    @Override
    protected String generateSelectTableIndexes() {
        return "select i.relname as index_name, a.attname as column_name" +
                "   from pg_class t, pg_class i, pg_index ix, pg_attribute a" +
                "   where t.oid = ix.indrelid and i.oid = ix.indexrelid and a.attrelid = t.oid and" +
                "       a.attnum = ANY(ix.indkey) and t.relkind = 'r' and t.relname = ?";
    }

    @Override
    protected String generateCountTableIndexes() {
        return "select count(i.relname) indexes_count " +
                "from pg_class t, pg_class i, pg_index ix" +
                "   where t.oid = ix.indrelid and i.oid = ix.indexrelid and t.relkind = 'r' and t.relname = ?";
    }

    @Override
    protected String generateCountTableUniqueKeys() {
        return "select count(tc.constraint_name) unique_keys_count from information_schema.table_constraints tc " +
                "where constraint_type = 'UNIQUE' and tc.table_name = ?";
    }

    @Override
    protected String generateCountTableForeignKeys() {
        return "select count(tc.constraint_name) foreign_keys_count from information_schema.table_constraints tc " +
                "where constraint_type = 'FOREIGN KEY' and tc.table_name = ?";
    }
}
