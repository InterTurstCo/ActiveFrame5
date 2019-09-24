package ru.intertrust.cm.core.config.form;

import org.junit.Assert;
import org.junit.Test;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.form.processor.impl.TabTemplateProcessor;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.TabConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;

import java.util.List;

import static ru.intertrust.cm.core.config.Constants.FORM_TEMPLATES_CONFIG;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 24.08.2015
 *         Time: 15:31
 */
public class TabTemplateProcessorTest extends AbstractConfigProcessingTest{
    /**
     * important
     * table processor was not applied, so we have raw tab template
     * assumption about widget-config number and tab number are based on that condition
     * @throws Exception
     */
    @Test
    public void testProcessTemplates() throws Exception {
        ConfigurationExplorer customConfigExplorer = createConfigurationExplorer(FORM_TEMPLATES_CONFIG);
        FormConfig formWithTemplates = customConfigExplorer.getConfig(FormConfig.class, "ex1_form_with_templates");
        TabTemplateProcessor tabTemplateProcessor = new TabTemplateProcessor(customConfigExplorer);
        tabTemplateProcessor.setConfigurationExplorer(customConfigExplorer);

        FormConfig processed = tabTemplateProcessor.processTemplates(formWithTemplates);
        List<WidgetConfig> widgetConfigs = processed.getWidgetConfigurationConfig().getWidgetConfigList();
        Assert.assertEquals(9, widgetConfigs.size());
        Assert.assertEquals(3, processed.getMarkup().getBody().getTabs().size());
        TabConfig tabConfig = processed.getMarkup().getBody().getTabs().get(1);
        Assert.assertEquals("prefix_tb1", tabConfig.getId());
        Assert.assertEquals(1, tabConfig.getGroupList().getTabGroupConfigs().size());
        Assert.assertEquals("prefix_", tabConfig.getGroupList().getTabGroupConfigs().get(0).getId());

    }
}
