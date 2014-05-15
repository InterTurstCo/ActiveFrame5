package ru.intertrust.cm.core.service.it;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.ejb.EJB;
import javax.security.auth.login.LoginException;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.ConfigurationControlService;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.base.CollectionConfig;
import ru.intertrust.cm.webcontext.ApplicationContextProvider;

/**
 * Иитеграционный тест для {@link ru.intertrust.cm.core.business.api.ConfigurationService} и {@link ru.intertrust.cm.core.business.api.ConfigurationControlService}
 * Created by vmatsukevich on 2/4/14.
 *
 */
//@RunWith(Arquillian.class)
public class ConfigurationControlIT extends IntegrationTestBase {

    private ConfigurationControlService configurationControlService;

//    @EJB
    private ConfigurationService.Remote configurationService;

//    @Before
    public void init() throws IOException, LoginException {
        initializeSpringBeans();        
    }
    private void initializeSpringBeans() {
        ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
        configurationControlService = applicationContext.getBean(ConfigurationControlService.class);
    }
    
//    @Test
    public void testConfigurationLoaded() throws IOException {
        byte[] fileBytes = Files.readAllBytes(Paths.get("test-data/configuration-test.xml"));
        String configuration = new String(fileBytes);
        configurationControlService.updateConfiguration(configuration);

        fileBytes = Files.readAllBytes(Paths.get("test-data/actions-test.xml"));
        String toolBarConfiguration = new String(fileBytes);
        configurationControlService.updateConfiguration(toolBarConfiguration);

        // Check that new domain object type is not loaded
        assertNull(configurationService.getDomainObjectTypeConfig("NewDOType12345"));

        // Check that existing domain object type is not modified
        DomainObjectTypeConfig doTypeConfig  = configurationService.getDomainObjectTypeConfig("notification");
        assertTrue(doTypeConfig.getFieldConfigs().size() > 0);

        CollectionConfig collectionConfig = configurationService.getConfig(CollectionConfig.class, "NewCollection12345");
        //Check that new configuration config is loaded
        assertNotNull(collectionConfig);

        // Check that existing collection config is modified
        collectionConfig = configurationService.getConfig(CollectionConfig.class, "Countries");
        assertTrue(collectionConfig.getFilters().size() == 1);

        // Check that new collection view config is loaded
        assertNotNull(configurationService.getCollectionColumnConfig("NewCollectionView12345", "independence_day"));

        // Check that existing collection view is updated
        assertNull(configurationService.getCollectionColumnConfig("countries_default_view", "independence_day"));
        assertNotNull(configurationService.getCollectionColumnConfig("countries_default_view", "id"));
    }
}
