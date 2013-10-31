package ru.intertrust.cm.core.config;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import ru.intertrust.cm.core.config.model.base.Configuration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.suppress;
import static ru.intertrust.cm.core.config.Constants.*;
/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(FormLogicalValidator.class)
public class FormLogicalValidatorTest {

    private static final String FORM_XML_PATH = "config/forms-test.xml";
    private static final String INVALID_FORM_XML_PATH = "config/form-with-errors.xml";
    private static final String GLOBAL_XML_PATH = "config/global-test.xml";
    /**
     * Вызов метода validateWidgetsHandlers исключается на время тестов
     * Для корректной работы validateWidgetsHandlers требуется спринг контекст
     * @throws Exception
     */
    @Before
     public void SetUp() throws Exception {
        suppress(method(FormLogicalValidator.class, "validateWidgetsHandlers"));

     }
    @Test
    public void validateCorrectForm() throws Exception {

        FormLogicalValidator formValidator = new FormLogicalValidator();
        ConfigurationExplorer configurationExplorer = createConfigurationExplorer(FORM_XML_PATH);
        formValidator.setConfigurationExplorer(configurationExplorer);
        formValidator.validate();
    }

    @Test
    public void validateIncorrectForm() throws Exception {

        String exceptedMessage = ("Configuration of form with "
                + "name 'city_form' was validated with errors.Count: 2 Content:\n"
                + "Could not find field 'city'  in path 'country^federal_unit.city.population'\n"
                + "Path part 'year_of_foundation' in  'year_of_foundation.date' isn't a reference type\n"
                + "Configuration of form with name 'country_form' was validated with errors.Count: 2 Content:\n"
                + "h-align 'righ' is incorrect\n"
                + "v-align 'middle' is incorrect\n");
       ConfigurationExplorer configurationExplorer = createConfigurationExplorer(INVALID_FORM_XML_PATH);

       FormLogicalValidator formValidator = new FormLogicalValidator();
       formValidator.setConfigurationExplorer(configurationExplorer);
       try {
       formValidator.validate();
       } catch(ConfigurationException e) {
        assertEquals(exceptedMessage, e.getMessage());
    }
    }

    private ConfigurationExplorer createConfigurationExplorer(String configPath) throws Exception {
        TopLevelConfigurationCache.getInstance().build();
        ConfigurationSerializer configurationSerializer = new ConfigurationSerializer();

        Set<String> configPaths = new HashSet<>(Arrays.asList(configPath, DOMAIN_OBJECTS_CONFIG_PATH, GLOBAL_XML_PATH));

        configurationSerializer.setCoreConfigurationFilePaths(configPaths);
        configurationSerializer.setCoreConfigurationSchemaFilePath(CONFIGURATION_SCHEMA_PATH);

        configurationSerializer.setModulesConfigurationFolder(MODULES_CONFIG_FOLDER);
        configurationSerializer.setModulesConfigurationPath(MODULES_CONFIG_PATH);
        configurationSerializer.setModulesConfigurationSchemaPath(MODULES_CONFIG_SCHEMA_PATH);

        Configuration configuration = configurationSerializer.deserializeConfiguration();

        ConfigurationExplorerImpl configurationExplorer = new ConfigurationExplorerImpl(configuration);
        configurationExplorer.build();
        return configurationExplorer;
    }

}

