package ru.intertrust.cm.core.business.impl.test;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import ru.intertrust.cm.core.business.impl.ConfigurationHelper;
import ru.intertrust.cm.core.config.BusinessObjectConfig;
import ru.intertrust.cm.core.config.Configuration;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.StringFieldConfig;

import static org.junit.Assert.assertEquals;

/**
 * @author vmatsukevich
 *         Date: 5/24/13
 *         Time: 11:38 AM
 */
@RunWith(JUnit4.class)
public class ConfigurationHelperTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private Configuration configuration;

    private BusinessObjectConfig businessObjectConfig1;
    private BusinessObjectConfig businessObjectConfig2;
    private BusinessObjectConfig businessObjectConfig3;

    private FieldConfig fieldConfig1;
    private FieldConfig fieldConfig2;
    private FieldConfig fieldConfig3;

    @Before
    public void setUp() {
        initializeTestConfiguration();
    }

    @Test
    public void testFindBusinessObjectConfigByName() throws Exception {
        // проверяем, что конфигурация находится по имени
        BusinessObjectConfig searchResultConfig = ConfigurationHelper.findBusinessObjectConfigByName(configuration, "business object 2");
        assertEquals(businessObjectConfig2, searchResultConfig);

        // проверяем, что, если конфигурация не найдена, бросается RuntimeException
        exception.expect(RuntimeException.class);
        ConfigurationHelper.findBusinessObjectConfigByName(configuration, "business object 4");
    }

    @Test
    public void testFindFieldConfigForBusinessObject() throws Exception {
        // проверяем, что конфигурация находится по имени
        FieldConfig searchResultField = ConfigurationHelper.findFieldConfigForBusinessObject(businessObjectConfig1, "field 2");
        assertEquals(fieldConfig2, searchResultField);

        // проверяем, что, если конфигурация не найдена, бросается RuntimeException
        exception.expect(RuntimeException.class);
        ConfigurationHelper.findFieldConfigForBusinessObject(businessObjectConfig1, "field 4");
    }

    private void initializeTestConfiguration() {
        configuration = new Configuration();

        initializeBusinessObjectConfig1();
        configuration.getBusinessObjectConfigs().add(businessObjectConfig1);

        businessObjectConfig2 = new BusinessObjectConfig();
        businessObjectConfig2.setName("business object 2");
        configuration.getBusinessObjectConfigs().add(businessObjectConfig2);

        businessObjectConfig3 = new BusinessObjectConfig();
        businessObjectConfig3.setName("business object 3");
        configuration.getBusinessObjectConfigs().add(businessObjectConfig3);
    }

    private void initializeBusinessObjectConfig1() {
        businessObjectConfig1 = new BusinessObjectConfig();
        businessObjectConfig1.setName("business object 1");

        fieldConfig1 = new StringFieldConfig();
        fieldConfig1.setName("field 1");
        businessObjectConfig1.getFieldConfigs().add(fieldConfig1);

        fieldConfig2 = new StringFieldConfig();
        fieldConfig2.setName("field 2");
        businessObjectConfig1.getFieldConfigs().add(fieldConfig2);

        fieldConfig3 = new StringFieldConfig();
        fieldConfig3.setName("field 3");
        businessObjectConfig1.getFieldConfigs().add(fieldConfig3);
    }
}
