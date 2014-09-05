package ru.intertrust.cm.core.service.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import javax.ejb.EJB;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.GlobalSettingsConfig;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionViewConfig;

/**
 * Иитеграционный тест для {@link ConfigurationService} и {@link ru.intertrust.cm.core.business.api.ConfigurationLoadService}
 * Created by vmatsukevich on 2/4/14.
 *
 */
@RunWith(Arquillian.class)
public class ConfigurationIT extends IntegrationTestBase {

    @EJB
    private ConfigurationService.Remote configurationService;

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
}
