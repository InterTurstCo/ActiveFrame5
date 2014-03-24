package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DataStructureDao;
import ru.intertrust.cm.core.dao.api.IdGenerator;

/**
 * Oracle-специфичная имплементация {@link DaoFactory}
 * @author vmatsukevich
 */
public class OracleDaoFactoryImpl extends AbstractDaoFactory {
    @Override
    public DataStructureDao createDataStructureDao() {
        return new OracleDataStructureDaoImpl();
    }

    @Override
    public CollectionsDao createCollectionsDao() {
        return new OracleCollectionsDaoImpl();
    }

    @Override
    public IdGenerator createIdGenerator() {
        return new OracleSequenceIdGenerator();
    }


}
