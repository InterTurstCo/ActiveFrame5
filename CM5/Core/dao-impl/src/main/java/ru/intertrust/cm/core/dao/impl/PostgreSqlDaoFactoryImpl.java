package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.dao.api.DataStructureDao;

/**
 * Created by vmatsukevich on 2/18/14.
 */
public class PostgreSqlDaoFactoryImpl extends AbstractDaoFactory {
    @Override
    public DataStructureDao createDataStructureDao() {
        return new PostgreSqlDataStructureDaoImpl();
    }
}
