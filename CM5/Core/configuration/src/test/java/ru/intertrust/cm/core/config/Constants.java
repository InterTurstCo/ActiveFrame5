package ru.intertrust.cm.core.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Константы для тестов
 * @author vmatsukevich
 *         Date: 7/22/13
 *         Time: 10:53 PM
 */
public interface Constants {
    String NAVIGATION_PANEL_SCHEMA_PATH = "config/navigation-panel.xsd";
    String CONFIGURATION_SCHEMA_PATH = "config/configuration.xsd";
    String DOMAIN_OBJECTS_CONFIG_PATH = "config/domain-objects-test.xml";
    String DOMAIN_OBJECTS_TEST_SERIALIZER_CONFIG_PATH = "config/domain-objects-serializer-test.xml";
    
    String DOMAIN_OBJECTS_LOOP_IN_HIERARCHY_CONFIG_PATH = "config/domain-objects-loop-in-hierarchy-test.xml";
    String SYSTEM_DOMAIN_OBJECTS_CONFIG_PATH = "config/system-domain-objects-test.xml";
    String COLLECTIONS_CONFIG_PATH = "config/collections-test.xml";

    String MODULES_CONFIG_FOLDER = "modules-configuration";
    String MODULES_CONFIG_PATH = "/modules-configuration-test.xml";
    String MODULES_CONFIG_SCHEMA_PATH = "config/modules-configuration-test.xsd";

    String MODULES_CUSTOM_CONFIG = MODULES_CONFIG_FOLDER + "/test-module/custom-config-test.xml";
    String MODULES_DOMAIN_OBJECTS = MODULES_CONFIG_FOLDER + "/test-module/domain-objects-test.xml";
    String MODULES_CUSTOM_SCHEMA = MODULES_CONFIG_FOLDER + "/test-module/custom-configuration-test.xsd";
    String FORM_EXTENSION_CONFIG = "config/form-extension-test.xml";
    String FORM_EXTENSION_INVALID_CONFIG = "config/form-extension-invalid-test.xml";
    String FORM_TEMPLATES_CONFIG = "config/form-templates.xml";
    Set<String> CONFIG_PATHS =
            new HashSet<>(Arrays.asList(SYSTEM_DOMAIN_OBJECTS_CONFIG_PATH, DOMAIN_OBJECTS_CONFIG_PATH, COLLECTIONS_CONFIG_PATH));

}
