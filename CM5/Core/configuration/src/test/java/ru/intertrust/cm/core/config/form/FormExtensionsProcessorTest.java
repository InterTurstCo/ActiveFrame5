package ru.intertrust.cm.core.config.form;

import org.junit.Test;
import ru.intertrust.cm.core.config.ConfigurationException;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.form.processor.impl.FormExtensionsProcessorImpl;
import ru.intertrust.cm.core.config.gui.form.*;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;

import java.util.List;

import static org.junit.Assert.*;
import static ru.intertrust.cm.core.config.Constants.FORM_EXTENSION_CONFIG;
import static ru.intertrust.cm.core.config.Constants.FORM_EXTENSION_INVALID_CONFIG;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 17.05.2015
 *         Time: 10:41
 */

public class FormExtensionsProcessorTest extends AbstractConfigProcessingTest {

    private FormExtensionsProcessorImpl getFormExtensionsProcessor(ConfigurationExplorer explorer) {
        return new FormExtensionsProcessorImpl(explorer);
    }

    @Test
    public void testProcessCityForm() throws Exception {
        ConfigurationExplorer customConfigExplorer = createConfigurationExplorer(FORM_EXTENSION_CONFIG);

        FormConfig rawFormConfig = customConfigExplorer.getConfig(FormConfig.class, "city_form");
        FormExtensionsProcessorImpl formExtensionsProcessor = getFormExtensionsProcessor(customConfigExplorer);
        formExtensionsProcessor.setConfigurationExplorer(customConfigExplorer);
        FormConfig plainFormConfig = formExtensionsProcessor.processExtensions(rawFormConfig);

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
    public void testProcessParentCityForm() throws Exception {

        ConfigurationExplorer customConfigExplorer = createConfigurationExplorer(FORM_EXTENSION_CONFIG);

        FormConfig rawFormConfig = customConfigExplorer.getConfig(FormConfig.class, "parent_city_form");
        FormExtensionsProcessorImpl formExtensionsProcessor = getFormExtensionsProcessor(customConfigExplorer);
        formExtensionsProcessor.setConfigurationExplorer(customConfigExplorer);
        FormConfig plainFormConfig = formExtensionsProcessor.processExtensions(rawFormConfig);

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
    public void testProcessInvalidParentCityForm() throws Exception {
        expectedException.expect(ConfigurationException.class);
        expectedException.expectMessage("Configuration of form with name 'parent_city_form' " +
                "was built with errors. Count: 2 Content:\n" +
                "Could not replace config with id 'tb6'\n" +
                "Could not replace config with id 'w18'");

        ConfigurationExplorer customConfigExplorer = createConfigurationExplorer(FORM_EXTENSION_INVALID_CONFIG);
        FormConfig rawFormConfig = customConfigExplorer.getConfig(FormConfig.class, "parent_city_form");
        FormExtensionsProcessorImpl formExtensionsProcessor = getFormExtensionsProcessor(customConfigExplorer);
        formExtensionsProcessor.setConfigurationExplorer(customConfigExplorer);
        formExtensionsProcessor.processExtensions(rawFormConfig);
    }

    @Test
    public void testProcessInvalidCityForm() throws Exception {
        expectedException.expect(ConfigurationException.class);
        expectedException.expectMessage("Configuration of form with name 'city_form' " +
                "was built with errors. Count: 3 Content:\n" +
                "Could not replace config with id 'tb6'\n" +
                "Could not replace config with id 'w18'\n" +
                "Could not add configs after config with id 'tr20'");

        ConfigurationExplorer customConfigExplorer = createConfigurationExplorer(FORM_EXTENSION_INVALID_CONFIG);
        FormConfig rawFormConfig = customConfigExplorer.getConfig(FormConfig.class, "city_form");
        FormExtensionsProcessorImpl formExtensionsProcessor = getFormExtensionsProcessor(customConfigExplorer);
        formExtensionsProcessor.setConfigurationExplorer(customConfigExplorer);
        formExtensionsProcessor.processExtensions(rawFormConfig);
    }

    @Test
    public void testProcessFormWithoutParent() throws Exception {
        ConfigurationExplorer customConfigExplorer = createConfigurationExplorer(FORM_EXTENSION_CONFIG);

        FormConfig rawFormConfig = customConfigExplorer.getConfig(FormConfig.class, "coordination_form");
        FormExtensionsProcessorImpl formExtensionsProcessor = getFormExtensionsProcessor(customConfigExplorer);
        formExtensionsProcessor.setConfigurationExplorer(customConfigExplorer);
        FormConfig plainFormConfig = formExtensionsProcessor.processExtensions(rawFormConfig);

        List<RowConfig> headerRowConfigs = plainFormConfig.getMarkup().getHeader().getTableLayout().getRows();
        List<CellConfig> cellConfigs = headerRowConfigs.get(0).getCells();
        assertEquals(8, cellConfigs.size());
        assertEquals("td_added", cellConfigs.get(1).getId());
    }

    @Test
    public void testProcessFormWithMissingParent() throws Exception {
        expectedException.expect(ConfigurationException.class);
        expectedException.expectMessage("Configuration of form with name 'coordination_task_form' " +
                "was built with errors. Count: 1 Content:\n" +
                "Parent form with name 'execution_form' was not found");
        ConfigurationExplorer customConfigExplorer = createConfigurationExplorer(FORM_EXTENSION_INVALID_CONFIG);
        FormExtensionsProcessorImpl formExtensionsProcessor = getFormExtensionsProcessor(customConfigExplorer);
        FormConfig rawFormConfig = customConfigExplorer.getConfig(FormConfig.class, "coordination_task_form");
        formExtensionsProcessor.setConfigurationExplorer(customConfigExplorer);
        formExtensionsProcessor.processExtensions(rawFormConfig);

    }

}
