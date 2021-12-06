package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import ru.intertrust.cm.core.dao.api.DatabaseInfo;

/**
 * Класс, служащий для создания фабрики ДАО-сервисов
 * @author vmatsukevich
 */
public class DaoFactoryProducer {

    @Autowired
    private DatabaseInfo databaseInfo;

    @Value("${use.test.pg.domain.object.helper:false}")
    private boolean useOptimizedHelper;

    public DaoFactory createDaoFactory() {
        DatabaseInfo.Vendor dbVendor = databaseInfo.getDatabaseVendor();
        if (DatabaseInfo.Vendor.ORACLE.equals(dbVendor)) {
            return new OracleDaoFactoryImpl();
        } else {
            return new PostgreSqlDaoFactoryImpl(useOptimizedHelper);
        }
    }
}
