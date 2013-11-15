package ru.intertrust.cm.core.dao.impl.access;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.dao.impl.StatusDaoImpl;

/**
 * Тест для StatusServiceDao.
 * @author atsvetkov
 *
 */

@RunWith(MockitoJUnitRunner.class)
public class StatusServiceDaoTest extends BaseDaoTest {

    protected static StatusDaoImpl statusDao;
    
    @BeforeClass
    public static void setUp2() throws Exception {
        statusDao = new StatusDaoImpl();
        statusDao.setDataSource(dataSource);
        statusDao.setDomainObjectTypeIdCache(domainObjectTypeIdCache);
        statusDao.setConfigurationExplorer(configurationExplorer);
    }

    @Test
    public void testGetStatusIdByName() {
        String statusName = "Active";
        Id statusId = statusDao.getStatusIdByName(statusName);
        assertNotNull(statusId);

        Id statusIdToFind = new RdbmsId(domainObjectTypeIdCache.getId("Status"), 1);
        String foundStatusName = statusDao.getStatusNameById(statusIdToFind);
        assertNotNull(foundStatusName);

    }
}
