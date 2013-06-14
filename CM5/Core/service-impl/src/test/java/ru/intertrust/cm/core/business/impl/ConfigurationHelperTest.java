package ru.intertrust.cm.core.business.impl;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import ru.intertrust.cm.core.config.model.BusinessObjectConfig;
import ru.intertrust.cm.core.config.model.BusinessObjectsConfiguration;
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

    private BusinessObjectsConfiguration businessObjectsConfiguration;

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
        /*// проверяем, что конфигурация находится по имени
        BusinessObjectConfig searchResultConfig = ConfigurationHelper.findBusinessObjectConfigByName(configuration, "business object 2");
        assertEquals(businessObjectConfig2, searchResultConfig);

        // проверяем, что, если конфигурация не найдена, бросается RuntimeException
        expectedException.expect(RuntimeException.class);
        ConfigurationHelper.findBusinessObjectConfigByName(configuration, "business object 4");*/
    }

    @Test
    public void testFindFieldConfigForBusinessObject() throws Exception {
        /*// проверяем, что конфигурация находится по имени
        FieldConfig searchResultField = ConfigurationHelper.findFieldConfigForBusinessObject(businessObjectConfig1, "field 2");
        assertEquals(fieldConfig2, searchResultField);

        // проверяем, что, если конфигурация не найдена, бросается RuntimeException
        expectedException.expect(RuntimeException.class);
        ConfigurationHelper.findFieldConfigForBusinessObject(businessObjectConfig1, "field 4");*/
    }

    private void initializeTestConfiguration() {
        businessObjectsConfiguration = new BusinessObjectsConfiguration();

        initializeBusinessObjectConfig1();
        businessObjectsConfiguration.getBusinessObjectConfigs().add(businessObjectConfig1);

        businessObjectConfig2 = new BusinessObjectConfig();
        businessObjectConfig2.setName("business object 2");
        businessObjectsConfiguration.getBusinessObjectConfigs().add(businessObjectConfig2);

        businessObjectConfig3 = new BusinessObjectConfig();
        businessObjectConfig3.setName("business object 3");
        businessObjectsConfiguration.getBusinessObjectConfigs().add(businessObjectConfig3);
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
