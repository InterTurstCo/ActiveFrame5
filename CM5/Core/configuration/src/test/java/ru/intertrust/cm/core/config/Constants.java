package ru.intertrust.cm.core.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author vmatsukevich
 *         Date: 7/22/13
 *         Time: 10:53 PM
 */
public interface Constants {

    String CONFIGURATION_SCHEMA_PATH = "config/configuration-test.xsd";
    String DOMAIN_OBJECTS_CONFIG_PATH = "config/domain-objects-test.xml";
    String SYSTEM_DOMAIN_OBJECTS_CONFIG_PATH = "test-config/system-domain-objects-test.xml";
    String COLLECTIONS_CONFIG_PATH = "config/collections-test.xml";

    String MODULES_CONFIG_FOLDER = "modules-configuration";
    String MODULES_CONFIG_PATH = "/modules-configuration-test.xml";
    String MODULES_CONFIG_SCHEMA_PATH = "config/modules-configuration-test.xsd";

    Set<String> CONFIG_PATHS =
            new HashSet<>(Arrays.asList(SYSTEM_DOMAIN_OBJECTS_CONFIG_PATH, DOMAIN_OBJECTS_CONFIG_PATH, COLLECTIONS_CONFIG_PATH));

}
