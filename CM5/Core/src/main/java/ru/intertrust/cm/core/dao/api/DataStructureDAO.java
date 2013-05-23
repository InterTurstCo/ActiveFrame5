package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.config.BusinessObjectConfig;

/**
 * @author vmatsukevich
 *         Date: 5/15/13
 *         Time: 4:27 PM
 */
public interface DataStructureDAO {

    void createTable(BusinessObjectConfig config);
    Integer countTables();
    void createServiceTables();
}
