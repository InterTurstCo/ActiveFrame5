package ru.intertrust.cm.core.config.form;

import junit.framework.Assert;
import org.junit.Test;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.form.processor.impl.TableTemplateProcessor;
import ru.intertrust.cm.core.config.gui.form.*;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;

import java.util.List;

import static ru.intertrust.cm.core.config.Constants.FORM_TEMPLATES_CONFIG;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 25.08.2015
 *         Time: 10:16
 */
public class TableTemplateProcessorTest extends AbstractConfigProcessingTest {
    /**
     * important
     * tab processor was not applied, so we have raw tab template
     * assumption about widget-config number and tab number are based on that condition
     * @throws Exception
     */
    @Test
    public void testProcessTemplates() throws Exception {
        ConfigurationExplorer customConfigExplorer = createConfigurationExplorer(FORM_TEMPLATES_CONFIG);
        FormConfig formWithTemplates = customConfigExplorer.getConfig(FormConfig.class, "ex1_form_with_templates");
        TableTemplateProcessor tableTemplateProcessor = new TableTemplateProcessor(customConfigExplorer);

        FormConfig processed = tableTemplateProcessor.processTemplates(formWithTemplates);

        List<WidgetConfig> widgetConfigs = processed.getWidgetConfigurationConfig().getWidgetConfigList();
        Assert.assertEquals(6, widgetConfigs.size());

        HeaderConfig headerConfig = processed.getMarkup().getHeader();
        Assert.assertNotNull(headerConfig.getTableLayout());
        TableLayoutConfig headerTableLayoutConfig = headerConfig.getTableLayout();
        Assert.assertEquals(1, headerTableLayoutConfig.getRows().size());
        Assert.assertEquals("prefix_tr1", headerTableLayoutConfig.getRows().get(0).getId());

        BodyConfig bodyConfig = processed.getMarkup().getBody();
        List<TabConfig> tabConfigs = bodyConfig.getTabs();
        Assert.assertEquals(2, tabConfigs.size());
        TabConfig tabConfig = tabConfigs.get(1);
        Assert.assertEquals(2, tabConfig.getGroupList().getTabGroupConfigs().size());
        TabGroupConfig tabGroupConfig = tabConfig.getGroupList().getTabGroupConfigs().get(1);
        Assert.assertNotNull(tabGroupConfig.getLayout());
        TableLayoutConfig bodyTableLayoutConfig = tabGroupConfig.getLayout();
        Assert.assertTrue(bodyTableLayoutConfig.getRows().size() > 0);
        Assert.assertEquals("prefix_tr5", bodyTableLayoutConfig.getRows().get(0).getId());


    }
}