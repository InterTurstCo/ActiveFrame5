package ru.intertrust.cm.core.service.it;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.ConfigurationControlService;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.GlobalSettingsConfig;
import ru.intertrust.cm.core.config.base.CollectionConfig;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionViewConfig;
import ru.intertrust.cm.webcontext.ApplicationContextProvider;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Иитеграционный тест для {@link ConfigurationService} и {@link ConfigurationControlService}
 * Created by vmatsukevich on 2/4/14.
 *
 */
@RunWith(Arquillian.class)
public class ConfigurationIT extends IntegrationTestBase {

    //private ConfigurationControlService configurationControlService;
    private ConfigurationService configurationService;

    /**
     * Предотвращает загрузку данных для каждого теста. Данные загружаются один раз для всех тестов в данном классе.
     */
    private boolean isDataLoaded = false;

    @Deployment
    public static Archive<EnterpriseArchive> createDeployment() {
        return createDeployment(new Class[] {ConfigurationIT.class, ApplicationContextProvider.class },
                new String[] { "beans.xml" });
    }

    @Before
    public void init() throws IOException, LoginException {
        initializeSpringBeans();
        //configurationControlService.loadConfiguration();
    }

    @Test
    public void testConfigurationLoaded() {
        Configuration configuration = configurationService.getConfiguration();
        assertNotNull(configuration);
        assertTrue(configuration.getConfigurationList() != null && !configuration.getConfigurationList().isEmpty());
    }

    @Test
    public void testConfigurationModification() {
        Configuration configuration = configurationService.getConfiguration();
        configuration.getConfigurationList().clear();

        Configuration configuration2 = configurationService.getConfiguration();
        assertTrue(configuration2.getConfigurationList() != null && !configuration2.getConfigurationList().isEmpty());
    }

    @Test
    public void testGlobalConfiguration() {
        GlobalSettingsConfig globalSettingsConfig = configurationService.getGlobalSettings();

        assertNotNull(globalSettingsConfig);
        assertNotNull(globalSettingsConfig.getAuditLog());
        assertNotNull(globalSettingsConfig.getSqlTrace());

        Collection<GlobalSettingsConfig> globalSettingsConfigs = configurationService.getConfigs(GlobalSettingsConfig.class);
        assertNotNull(globalSettingsConfigs);
        assertTrue(globalSettingsConfigs.size() == 1);
        assertEquals(globalSettingsConfig, globalSettingsConfigs.iterator().next());
    }

    @Test
    public void testDomainObjectTypeConfigurations() {
        Collection<DomainObjectTypeConfig> domainObjectTypeConfigs = configurationService.getConfigs(DomainObjectTypeConfig.class);
        assertNotNull(domainObjectTypeConfigs);
        assertFalse(domainObjectTypeConfigs.isEmpty());

        DomainObjectTypeConfig domainObjectTypeConfig = domainObjectTypeConfigs.iterator().next();
        assertEquals(domainObjectTypeConfig, configurationService.getConfig(DomainObjectTypeConfig.class, domainObjectTypeConfig.getName()));

        if (!domainObjectTypeConfig.getFieldConfigs().isEmpty()) {
            FieldConfig fieldConfig = domainObjectTypeConfig.getFieldConfigs().get(0);
            assertEquals(fieldConfig, configurationService.getFieldConfig(domainObjectTypeConfig.getName(), fieldConfig.getName()));
        }

        // Проверка получения конфигурации поля родительского тида ДО
        for (DomainObjectTypeConfig doTypeConfig : domainObjectTypeConfigs) {
            if (doTypeConfig.getExtendsAttribute() != null) {
                DomainObjectTypeConfig parentDOTypeConfig =
                        configurationService.getConfig(DomainObjectTypeConfig.class, doTypeConfig.getExtendsAttribute());
                assertNotNull(parentDOTypeConfig);

                if (!parentDOTypeConfig.getFieldConfigs().isEmpty()) {
                    FieldConfig fieldConfig = parentDOTypeConfig.getFieldConfigs().get(0);
                    assertNotNull(configurationService.getFieldConfig(doTypeConfig.getName(), fieldConfig.getName()));
                    assertNull(configurationService.getFieldConfig(doTypeConfig.getName(), fieldConfig.getName(), false));
                }

                break;
            }
        }
    }

    @Test
    public void testCollectionConfigurations() {
        Collection<CollectionViewConfig> collectionConfigs = configurationService.getConfigs(CollectionViewConfig.class);

        if (collectionConfigs != null && !collectionConfigs.isEmpty()) {
            CollectionViewConfig collectionConfig = collectionConfigs.iterator().next();

            if (collectionConfig.getCollectionDisplayConfig() != null &&
                    collectionConfig.getCollectionDisplayConfig().getColumnConfig() != null &&
                    !collectionConfig.getCollectionDisplayConfig().getColumnConfig().isEmpty()) {
                CollectionColumnConfig columnConfig =
                        collectionConfig.getCollectionDisplayConfig().getColumnConfig().get(0);

                assertEquals(columnConfig,
                        configurationService.getCollectionColumnConfig(collectionConfig.getName(), columnConfig.getName()));
            }
        }
    }

    private void initializeSpringBeans() {
        ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
        //configurationControlService = applicationContext.getBean(ConfigurationControlService.class);
        configurationService = applicationContext.getBean(ConfigurationService.class);
    }
}
