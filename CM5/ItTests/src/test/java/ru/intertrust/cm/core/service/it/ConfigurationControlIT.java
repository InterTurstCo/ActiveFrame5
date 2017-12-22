package ru.intertrust.cm.core.service.it;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.ejb.EJB;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import ru.intertrust.cm.core.business.api.ConfigurationControlService;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.base.CollectionConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;

/**
 * Иитеграционный тест для
 * {@link ru.intertrust.cm.core.business.api.ConfigurationControlService}
 * Created by vmatsukevich on 2/4/14.
 *
 */
@RunWith(Arquillian.class)
public class ConfigurationControlIT extends IntegrationTestBase {

    @EJB
    private ConfigurationControlService.Remote configurationControlService;

    @EJB
    private ConfigurationService.Remote configurationService;

    @Before
    public void init() throws IOException, LoginException {
        LoginContext lc = login("admin", "admin");
        lc.login();
    }

    @After
    public void after() throws LoginException {
        LoginContext lc = login("admin", "admin");
        lc.logout();
    }

    @Test
    public void testConfigurationLoaded() throws IOException {
        byte[] fileBytes = readFile("test-data/configuration-test-new.xml");
        String configuration = new String(fileBytes);
        configurationControlService.activateFromString(configuration);

        DomainObjectTypeConfig domainObjectTypeConfig = configurationService.getDomainObjectTypeConfig("AbsentNewTestDO");
        assertNull(domainObjectTypeConfig);

        CollectionConfig collectionConfig = configurationService.getConfig(CollectionConfig.class, "NewTestCollection");
        assertNotNull(collectionConfig);
        assertTrue(collectionConfig.getFilters().size() > 0);

        CollectionColumnConfig collectionColumnConfig =
                configurationService.getCollectionColumnConfig("NewTestCollectionView", "independence_day");
        assertNotNull(collectionColumnConfig);

        collectionConfig = configurationService.getConfig(CollectionConfig.class, "AbsentNewTestCollection");
        assertNull(collectionConfig);

        collectionColumnConfig =
                configurationService.getCollectionColumnConfig("NewTestCollectionView", "absent_column");
        assertNull(collectionColumnConfig);
    }
}
