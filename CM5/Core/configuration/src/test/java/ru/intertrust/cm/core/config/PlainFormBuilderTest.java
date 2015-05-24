package ru.intertrust.cm.core.config;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.converter.ConfigurationClassesCache;
import ru.intertrust.cm.core.config.form.PlainFormBuilder;
import ru.intertrust.cm.core.config.gui.form.*;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.config.module.ModuleConfiguration;
import ru.intertrust.cm.core.config.module.ModuleService;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static ru.intertrust.cm.core.config.Constants.*;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 17.05.2015
 *         Time: 10:41
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/beans-test.xml"})
public class PlainFormBuilderTest {

    private ConfigurationExplorerImpl configExplorer;
    @Autowired
    private PlainFormBuilder plainFormBuilder;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testGetPlainCityForm() throws Exception {
        ConfigurationSerializer configurationSerializer = createConfigurationSerializer(FORM_EXTENSION_CONFIG);
        Configuration config = configurationSerializer.deserializeConfiguration();
        configExplorer = new ConfigurationExplorerImpl(config, true);

        FormConfig rawFormConfig = configExplorer.getConfig(FormConfig.class, "city_form");
        List<FormConfig> parentConfigs = configExplorer.getParentFormConfigs(rawFormConfig);
        FormConfig plainFormConfig = plainFormBuilder.buildPlainForm(rawFormConfig, parentConfigs);

        List<RowConfig> headerRowConfigs = plainFormConfig.getMarkup().getHeader().getTableLayout().getRows();
        assertEquals(3, headerRowConfigs.size());

        RowConfig rowConfigFirst = headerRowConfigs.get(0);
        assertEquals("tr0", rowConfigFirst.getId());

        RowConfig rowConfigSecond = headerRowConfigs.get(1);
        assertEquals("tr1", rowConfigSecond.getId());

        List<CellConfig> cellConfigsOfSecondRow = rowConfigSecond.getCells();
        assertEquals(5, cellConfigsOfSecondRow.size());
        assertEquals("w4", cellConfigsOfSecondRow.get(0).getWidgetDisplayConfig().getId());
        assertEquals("w2", cellConfigsOfSecondRow.get(1).getWidgetDisplayConfig().getId());
        assertEquals("w5", cellConfigsOfSecondRow.get(2).getWidgetDisplayConfig().getId());

        List<TabConfig> tabConfigs = plainFormConfig.getMarkup().getBody().getTabs();
        assertEquals(3, tabConfigs.size());
        assertEquals(2, tabConfigs.get(2).getGroupList().getTabGroupConfigs().size());

        List<WidgetConfig> widgetConfigs = plainFormConfig.getWidgetConfigurationConfig().getWidgetConfigList();
        assertEquals(10, widgetConfigs.size());
        assertEquals("w10", widgetConfigs.get(8).getId());
        assertEquals("w11", widgetConfigs.get(9).getId());

        assertEquals(1, plainFormConfig.getWidgetGroupsConfig().getWidgetGroupConfigList().size());
        List<WidgetRefConfig> widgetRefConfigs = plainFormConfig.getWidgetGroupsConfig().getWidgetGroupConfigList().get(0).getWidgetRefConfigList();
        assertEquals(2, widgetRefConfigs.size());
        assertEquals("w6", widgetRefConfigs.get(0).getId());

        assertNull(plainFormConfig.getFormObjectsRemoverConfig());
        assertNull(plainFormConfig.getFormSaveExtensionConfig());
        assertNotNull(plainFormConfig.getToolbarConfig());


    }

    @Test
    public void testGetPlainParentCityForm() throws Exception {
        ConfigurationSerializer configurationSerializer = createConfigurationSerializer(FORM_EXTENSION_CONFIG);
        Configuration config = configurationSerializer.deserializeConfiguration();
        configExplorer = new ConfigurationExplorerImpl(config, true);

        FormConfig rawFormConfig = configExplorer.getConfig(FormConfig.class, "parent_city_form");
        List<FormConfig> parentConfigs = configExplorer.getParentFormConfigs(rawFormConfig);
        FormConfig plainFormConfig = plainFormBuilder.buildPlainForm(rawFormConfig, parentConfigs);

        List<RowConfig> headerRowConfigs = plainFormConfig.getMarkup().getHeader().getTableLayout().getRows();
        assertEquals(1, headerRowConfigs.size());

        RowConfig rowConfig = headerRowConfigs.get(0);
        assertEquals("tr1", rowConfig.getId());

        List<TabConfig> tabConfigs = plainFormConfig.getMarkup().getBody().getTabs();
        assertEquals(3, tabConfigs.size());
        assertEquals(2, tabConfigs.get(2).getGroupList().getTabGroupConfigs().size());

        List<WidgetConfig> widgetConfigs = plainFormConfig.getWidgetConfigurationConfig().getWidgetConfigList();
        assertEquals(10, widgetConfigs.size());
        assertEquals("w10", widgetConfigs.get(8).getId());
        assertEquals("w11", widgetConfigs.get(9).getId());

        assertEquals(1, plainFormConfig.getWidgetGroupsConfig().getWidgetGroupConfigList().size());
        List<WidgetRefConfig> widgetRefConfigs = plainFormConfig.getWidgetGroupsConfig().getWidgetGroupConfigList().get(0).getWidgetRefConfigList();
        assertEquals(2, widgetRefConfigs.size());
        assertEquals("w3", widgetRefConfigs.get(0).getId());

        assertNotNull(plainFormConfig.getFormObjectsRemoverConfig());
        assertNotNull(plainFormConfig.getFormSaveExtensionConfig());


    }

    @Test
    public void testInvalidPlainParentCityForm() throws Exception {
        ConfigurationSerializer configurationSerializer = createConfigurationSerializer(FORM_EXTENSION_INVALID_CONFIG);

        Configuration config = configurationSerializer.deserializeConfiguration();
        expectedException.expect(ConfigurationException.class);
        expectedException.expectMessage("Configuration of form extension with name 'parent_city_form' " +
                "was built with errors.Count: 2 Content:\n" +
                "Could not replace config with id 'tb6'\n" +
                "Could not replace config with id 'w18'");

        configExplorer = new ConfigurationExplorerImpl(config, true);
        FormConfig rawFormConfig = configExplorer.getConfig(FormConfig.class, "parent_city_form");
        List<FormConfig> parentConfigs = configExplorer.getParentFormConfigs(rawFormConfig);
        plainFormBuilder.buildPlainForm(rawFormConfig, parentConfigs);
    }

    @Test
    public void testInvalidPlainCityForm() throws Exception {
        ConfigurationSerializer configurationSerializer = createConfigurationSerializer(FORM_EXTENSION_INVALID_CONFIG);

        Configuration config = configurationSerializer.deserializeConfiguration();
        expectedException.expect(ConfigurationException.class);
        expectedException.expectMessage("Configuration of form extension with name 'city_form' " +
                "was built with errors.Count: 3 Content:\n" +
                "Could not replace config with id 'tb6'\n" +
                "Could not replace config with id 'w18'\n" +
                "Could not add configs after config with id 'tr20'");

        configExplorer = new ConfigurationExplorerImpl(config, true);
        FormConfig rawFormConfig = configExplorer.getConfig(FormConfig.class, "city_form");
        List<FormConfig> parentConfigs = configExplorer.getParentFormConfigs(rawFormConfig);
        plainFormBuilder.buildPlainForm(rawFormConfig, parentConfigs);
    }

    private ConfigurationSerializer createConfigurationSerializer(String confPath) throws Exception {
        ConfigurationClassesCache.getInstance().build(); // Инициализируем кэш конфигурации тэг-класс

        ConfigurationSerializer configurationSerializer = new ConfigurationSerializer();
        configurationSerializer.setModuleService(createModuleService(confPath));

        return configurationSerializer;
    }

    private ModuleService createModuleService(String confPath) throws MalformedURLException {
        URL moduleUrl = getClass().getClassLoader().getResource(".");

        ModuleService result = new ModuleService();
        ModuleConfiguration confCore = new ModuleConfiguration();
        confCore.setName("core");
        result.getModuleList().add(confCore);
        confCore.setConfigurationPaths(new ArrayList<String>());
        confCore.getConfigurationPaths().add(confPath);
        confCore.setConfigurationSchemaPath(CONFIGURATION_SCHEMA_PATH);
        confCore.setModuleUrl(moduleUrl);

        return result;
    }

}
