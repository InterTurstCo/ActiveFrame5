package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.dao.api.DataStructureDao;
import ru.intertrust.cm.core.dao.api.IdGenerator;

/**
 * PostgreSql-специфичная имплементация {@link DaoFactory}
 * @author vmatsukevich
 */
public class PostgreSqlDaoFactoryImpl extends AbstractDaoFactory {
    @Override
    public DataStructureDao createDataStructureDao() {
        return new PostgreSqlDataStructureDaoImpl();
    }

    @Override
    public IdGenerator createIdGenerator() {
        return new PostgreSqlSequenceIdGenerator();
    }
}
