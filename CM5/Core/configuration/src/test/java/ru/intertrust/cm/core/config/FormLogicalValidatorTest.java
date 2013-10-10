package ru.intertrust.cm.core.config;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.intertrust.cm.core.config.model.base.Configuration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static ru.intertrust.cm.core.config.Constants.*;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath*:beans.xml"})
public class FormLogicalValidatorTest {
    private static final String FORM_XML_PATH = "config/forms-test.xml";
    private static final String INVALID_FORM_XML_PATH = "config/form-with-three-errors.xml";
    @Autowired
    ApplicationContext context;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testValidate() throws Exception {
        ConfigurationExplorer configurationExplorer = createConfigurationExplorer(FORM_XML_PATH);

        FormLogicalValidator formLogicalValidator = (FormLogicalValidator) context.getBean("formLogicalValidator");
        formLogicalValidator.setConfigurationExplorer(configurationExplorer);
        formLogicalValidator.validate();
    }

    @Test
    public void testWithThreeErrors() throws Exception {
        expectedException.expect(ConfigurationException.class);
        expectedException.expectMessage("Configuration of form with name 'incoming_document_base_form'"
                + " was validated with errors.Count: 3 Content:\n"
                + "Dimension '50pxx' is incorrect\n"
                + "Couldn't find widget with id '9'\n"
                + "v-align 'middle' is incorrect");
        ConfigurationExplorer configurationExplorer = createConfigurationExplorer(INVALID_FORM_XML_PATH);

        FormLogicalValidator formLogicalValidator = (FormLogicalValidator) context.getBean("formLogicalValidator");
        formLogicalValidator.setConfigurationExplorer(configurationExplorer);
        formLogicalValidator.validate();

    }

    private ConfigurationExplorer createConfigurationExplorer(String configPath) throws Exception {
        TopLevelConfigurationCache.getInstance().build();
        ConfigurationSerializer configurationSerializer = new ConfigurationSerializer();

        Set<String> configPaths = new HashSet<>(Arrays.asList(configPath, DOMAIN_OBJECTS_CONFIG_PATH));

        configurationSerializer.setCoreConfigurationFilePaths(configPaths);
        configurationSerializer.setCoreConfigurationSchemaFilePath(CONFIGURATION_SCHEMA_PATH);

        configurationSerializer.setModulesConfigurationFolder(MODULES_CONFIG_FOLDER);
        configurationSerializer.setModulesConfigurationPath(MODULES_CONFIG_PATH);
        configurationSerializer.setModulesConfigurationSchemaPath(MODULES_CONFIG_SCHEMA_PATH);

        Configuration configuration = configurationSerializer.deserializeConfiguration();

        ConfigurationExplorerImpl configurationExplorer = new ConfigurationExplorerImpl();
        configurationExplorer.setConfiguration(configuration);
        configurationExplorer.build();
        return configurationExplorer;
    }

}

