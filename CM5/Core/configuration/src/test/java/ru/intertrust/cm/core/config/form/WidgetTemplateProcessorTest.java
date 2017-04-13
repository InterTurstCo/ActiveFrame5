package ru.intertrust.cm.core.config.form;

import org.junit.Assert;
import org.junit.Test;
import ru.intertrust.cm.core.config.ConfigurationException;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.form.processor.impl.WidgetTemplateProcessorImpl;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SuggestBoxConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;

import java.util.List;

import static ru.intertrust.cm.core.config.Constants.FORM_EXTENSION_CONFIG;
import static ru.intertrust.cm.core.config.Constants.FORM_EXTENSION_INVALID_CONFIG;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 12.08.2015
 *         Time: 0:18
 */

public class WidgetTemplateProcessorTest extends AbstractConfigProcessingTest{

    @Test
    public void testProcessFormWithTemplates() throws Exception {

        ConfigurationExplorer customConfigExplorer = createConfigurationExplorer(FORM_EXTENSION_CONFIG);

        FormConfig formWithTemplates = customConfigExplorer.getConfig(FormConfig.class, "form_with_templates");
        WidgetTemplateProcessorImpl widgetTemplateProcessor = new WidgetTemplateProcessorImpl(customConfigExplorer);
        List<WidgetConfig> widgetConfigs = widgetTemplateProcessor.processTemplates(formWithTemplates.getName(),
                formWithTemplates.getWidgetConfigurationConfig().getWidgetConfigList());

        Assert.assertEquals(2, widgetConfigs.size());
        WidgetConfig processed = widgetConfigs.get(1);
        Assert.assertTrue(processed instanceof SuggestBoxConfig);
        SuggestBoxConfig suggestBoxConfig = (SuggestBoxConfig) processed;
        Assert.assertEquals("w1", suggestBoxConfig.getId());
        Assert.assertEquals("handler", suggestBoxConfig.getHandler());

        Assert.assertEquals("300px", suggestBoxConfig.getMaxTooltipHeight());
        Assert.assertEquals("300px", suggestBoxConfig.getMaxTooltipWidth());
        Assert.assertTrue(suggestBoxConfig.isReadOnly());
        Assert.assertFalse(suggestBoxConfig.getFieldPathConfig() == null);
        Assert.assertEquals("name", suggestBoxConfig.getFieldPathConfig().getValue());

    }

    @Test
    public void testProcessFormWithInvalidTemplates() throws Exception {
        expectedException.expect(ConfigurationException.class);
        expectedException.expectMessage("Configuration of form with name 'form_with_templates' " +
                "was built with errors. Count: 3 Content:\n" +
                "Widget template with name 'suggest-box-template1' wasn't found\n" +
                "Template based widget with id 'w2' has field 'readOnly' conflict\n" +
                "Template based widget with id 'w2' has field 'fieldPathConfig' conflict\n");

        ConfigurationExplorer customConfigExplorer = createConfigurationExplorer(FORM_EXTENSION_INVALID_CONFIG);
        FormConfig formWithTemplates = customConfigExplorer.getConfig(FormConfig.class, "form_with_templates");
        WidgetTemplateProcessorImpl widgetTemplateProcessor = new WidgetTemplateProcessorImpl(customConfigExplorer);
        widgetTemplateProcessor.processTemplates(formWithTemplates.getName(),
                formWithTemplates.getWidgetConfigurationConfig().getWidgetConfigList());
    }

}


