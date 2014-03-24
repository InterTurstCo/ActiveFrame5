package ru.intertrust.cm.core.dao.impl;

/**
 * Класс, служащий для создания фабрики ДАО-сервисов
 * @author vmatsukevich
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
