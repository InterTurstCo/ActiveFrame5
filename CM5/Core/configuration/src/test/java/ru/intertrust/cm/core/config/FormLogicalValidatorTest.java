package ru.intertrust.cm.core.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.converter.ConfigurationClassesCache;
import ru.intertrust.cm.core.config.module.ModuleConfiguration;
import ru.intertrust.cm.core.config.module.ModuleService;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static ru.intertrust.cm.core.config.Constants.CONFIGURATION_SCHEMA_PATH;
import static ru.intertrust.cm.core.config.Constants.DOMAIN_OBJECTS_CONFIG_PATH;
import static ru.intertrust.cm.core.config.Constants.SYSTEM_DOMAIN_OBJECTS_CONFIG_PATH;


/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
/*@RunWith(PowerMockRunner.class)*/

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/beans-test.xml"})
@PrepareForTest(FormLogicalValidator.class)
public class FormLogicalValidatorTest {

    private static final String FORM_XML_PATH = "config/forms-test.xml";
    private static final String INVALID_FORM_XML_PATH = "config/form-with-errors.xml";
    private static final String GLOBAL_XML_PATH = "config/global-test.xml";
    @Autowired
    private FormLogicalValidator formValidator;

    @Test
    public void validateCorrectForm() throws Exception {

        ConfigurationExplorer configurationExplorer = createConfigurationExplorer(FORM_XML_PATH);
        formValidator.setConfigurationExplorer(configurationExplorer);
        formValidator.validate();
        List<LogicalErrors> errors = formValidator.validate();
        assertEquals(0, errors.size());
    }

    @Test
    public void validateIncorrectForm() throws Exception {
        String expectedMessageForCountryChildForm = "Configuration of form with name 'child_country_form' was validated with errors.Count: 1 Content:\n" +
                "Configuration of form extension with name 'child_country_form' was built with errors.Count: 1 Content:\n" +
                "Could not delete config with id '77'\n";

        String expectedMessageForCountryForm = "Configuration of form with name 'country_form' was validated with errors.Count: 13 Content:\n"
                + "h-align 'righ' is incorrect\n"
                + "v-align 'middle' is incorrect\n"
                + "Widget with id '1' has redundant tag <pattern>\n"
                + "Widget with id '1' has redundant tag <renderer>\n"
                + "Widget with id '3' has redundant tag <pattern>\n"
                + "Pattern is empty for radio-button with id '8a'\n"
                + "Pattern format is not valid for radio-button with id '8b'\n"
                + "Incorrect pattern placeholder 'governor' found for domain object 'city' in radio-button with id '8c'\n"
                + "Incorrect pattern placeholder 'governor' found for domain object 'federal_unit' in radio-button with id '8d'\n"
                + "Field 'is_old' in  domain object 'country' isn't a boolean type\n"
                + "Collection 'Streets' for table-browser with id '17a' wasn't found\n"
                + "Collection view 'cities_default' for table-browser with id '17a' wasn't found\n"
                + "Collection 'cities' for suggest-box with id '8a' wasn't found";

        String expectedMessageForCityForm = "Configuration of form with name 'city_form' was validated with errors.Count: 5 Content:\n"
                + "Could not find field 'city'  in path 'country^federal_unit.city.population'\n"
                + "Path part 'year_of_foundation' in  'year_of_foundation.date' isn't a reference type\n"
                + "Could not find field 'letter'  in path 'organization_addressee^letter.organization'\n"
                + "Collection 'Departments' has no filter 'byOrganization'\n"
                + "Collection 'Employees' for hierarchy-browser with id '33d' wasn't found";

        String expectedMessageForSoDepartmentForm = "Configuration of form with name 'SO_Department_newForm' was validated with errors.Count: 4 Content:\n"
                + "Couldn't find widget with id '36'\n"
                + "Couldn't find widget with id '30'\n"
                + "Couldn't find widget with id '31'\n"
                + "Collection 'SO_StructureUnit_Collection' for hierarchy-browser with id 'SO_Parent_SU' wasn't found";
        ConfigurationExplorer configurationExplorer = createConfigurationExplorer(INVALID_FORM_XML_PATH);
        formValidator.setConfigurationExplorer(configurationExplorer);
        List<LogicalErrors> errors = formValidator.validate();
        assertEquals(4, errors.size());

        assertEquals(13, errors.get(0).getErrorCount());
        assertEquals(expectedMessageForCountryForm, errors.get(0).toString());

        assertEquals(1, errors.get(1).getErrorCount());
        assertEquals(expectedMessageForCountryChildForm, errors.get(1).toString());

        assertEquals(5, errors.get(2).getErrorCount());
        assertEquals(expectedMessageForCityForm, errors.get(2).toString());

        assertEquals(4, errors.get(3).getErrorCount());
        assertEquals(expectedMessageForSoDepartmentForm, errors.get(3).toString());

    }

    private ConfigurationExplorer createConfigurationExplorer(String configPath) throws Exception {
        ConfigurationClassesCache.getInstance().build();
        ConfigurationSerializer configurationSerializer = new ConfigurationSerializer();

        configurationSerializer.setModuleService(createModuleService(configPath));

        Configuration configuration = configurationSerializer.deserializeConfiguration();

        return new ConfigurationExplorerImpl(configuration);
    }

    private ModuleService createModuleService(String configPath) throws MalformedURLException {
        ModuleService result = new ModuleService();
        ModuleConfiguration conf = new ModuleConfiguration();
        result.getModuleList().add(conf);
        conf.setConfigurationPaths(new ArrayList<String>());
        conf.getConfigurationPaths().add(DOMAIN_OBJECTS_CONFIG_PATH);
        conf.getConfigurationPaths().add(GLOBAL_XML_PATH);
        conf.getConfigurationPaths().add(configPath);
        conf.getConfigurationPaths().add(SYSTEM_DOMAIN_OBJECTS_CONFIG_PATH);
        conf.setConfigurationSchemaPath(CONFIGURATION_SCHEMA_PATH);
        final URL moduleUrl = getClass().getClassLoader().getResource(".");
        conf.setModuleUrl(moduleUrl);
        return result;
    }
}

