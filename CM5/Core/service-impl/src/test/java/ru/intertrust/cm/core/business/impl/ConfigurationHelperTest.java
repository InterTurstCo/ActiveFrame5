package ru.intertrust.cm.core.business.impl;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import ru.intertrust.cm.core.config.model.Configuration;
import ru.intertrust.cm.core.config.model.DomainObjectConfig;
import ru.intertrust.cm.core.config.model.FieldConfig;
import ru.intertrust.cm.core.config.model.StringFieldConfig;

/**
 * @author vmatsukevich
 *         Date: 5/24/13
 *         Time: 11:38 AM
 */
@RunWith(JUnit4.class)
public class ConfigurationHelperTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private Configuration configuration;

    private DomainObjectConfig domainObjectConfig1;
    private DomainObjectConfig domainObjectConfig2;
    private DomainObjectConfig domainObjectConfig3;

    private FieldConfig fieldConfig1;
    private FieldConfig fieldConfig2;
    private FieldConfig fieldConfig3;

    @Before
    public void setUp() {
        initializeTestConfiguration();
    }

    @Test
    public void testFindDomainObjectConfigByName() throws Exception {
        /*// проверяем, что конфигурация находится по имени
        DomainObjectConfig searchResultConfig = ConfigurationHelper.findDomainObjectConfigByName(configuration, "domain object 2");
        assertEquals(domainObjectConfig2, searchResultConfig);

        // проверяем, что, если конфигурация не найдена, бросается RuntimeException
        expectedException.expect(RuntimeException.class);
        ConfigurationHelper.findDomainObjectConfigByName(configuration, "domain object 4");*/
    }

    @Test
    public void testFindFieldConfigForDomainObject() throws Exception {
        /*// проверяем, что конфигурация находится по имени
        FieldConfig searchResultField = ConfigurationHelper.findFieldConfigForDomainObject(domainObjectConfig1, "field 2");
        assertEquals(fieldConfig2, searchResultField);

        // проверяем, что, если конфигурация не найдена, бросается RuntimeException
        expectedException.expect(RuntimeException.class);
        ConfigurationHelper.findFieldConfigForDomainObject(domainObjectConfig1, "field 4");*/
    }

    private void initializeTestConfiguration() {
        configuration = new Configuration();

        initializeDomainObjectConfig1();
        configuration.getConfigurationList().add(domainObjectConfig1);

        domainObjectConfig2 = new DomainObjectConfig();
        domainObjectConfig2.setName("domain object 2");
        configuration.getConfigurationList().add(domainObjectConfig2);

        domainObjectConfig3 = new DomainObjectConfig();
        domainObjectConfig3.setName("domain object 3");
        configuration.getConfigurationList().add(domainObjectConfig3);
    }

    private void initializeDomainObjectConfig1() {
        domainObjectConfig1 = new DomainObjectConfig();
        domainObjectConfig1.setName("domain object 1");

        fieldConfig1 = new StringFieldConfig();
        fieldConfig1.setName("field 1");
        domainObjectConfig1.getFieldConfigs().add(fieldConfig1);

        fieldConfig2 = new StringFieldConfig();
        fieldConfig2.setName("field 2");
        domainObjectConfig1.getFieldConfigs().add(fieldConfig2);

        fieldConfig3 = new StringFieldConfig();
        fieldConfig3.setName("field 3");
        domainObjectConfig1.getFieldConfigs().add(fieldConfig3);
    }
}
