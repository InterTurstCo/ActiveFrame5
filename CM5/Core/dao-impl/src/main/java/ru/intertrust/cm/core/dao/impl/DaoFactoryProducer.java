package ru.intertrust.cm.core.dao.impl;

/**
 * Created by vmatsukevich on 2/18/14.
 */
public class DaoFactoryProducer {

    public DaoFactory createDaoFactory() {
        String dbVendor = System.getProperty("db.vendor");
        if ("oracle".equalsIgnoreCase(dbVendor)) {
            return new OracleDaoFactoryImpl();
        } else {
            return new PostgreSqlDaoFactoryImpl();
        }
    }
}
