package ru.intertrust.cm.core.config.form.processor.impl;

import ru.intertrust.cm.core.business.api.util.ObjectCloner;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.form.processor.FormProcessingUtil;
import ru.intertrust.cm.core.config.form.processor.FormTemplateProcessor;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.TabConfig;
import ru.intertrust.cm.core.config.gui.form.TabConfigMarker;
import ru.intertrust.cm.core.config.gui.form.template.FormTabTemplateConfig;
import ru.intertrust.cm.core.config.gui.form.template.TemplateBasedTabConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 24.08.2015
 *         Time: 15:28
 */
public class TabTemplateProcessor implements FormTemplateProcessor {

    private ConfigurationExplorer configurationExplorer;

    public TabTemplateProcessor() {
    }

    public TabTemplateProcessor(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    @Override
    public FormConfig processTemplates(FormConfig formConfig) {
        List<TabConfigMarker> tabConfigMarkers = formConfig.getMarkup().getBody().getTabConfigMarkers();
        List<TabConfig> tabs = new ArrayList<>(tabConfigMarkers.size());
        List<String> errors = new ArrayList<>();
        List<WidgetConfig> formWidgetConfigs = formConfig.getWidgetConfigurationConfig().getWidgetConfigList();
        for (TabConfigMarker tabConfigMarker : tabConfigMarkers) {
            if(isTemplateBasedTab(tabConfigMarker)){
                TemplateBasedTabConfig templateBasedTabConfig = (TemplateBasedTabConfig) tabConfigMarker;
                processTemplate(templateBasedTabConfig, formWidgetConfigs, tabs, errors);

            } else {
                //don't check type, it could be only TabConfig
                TabConfig tabConfig = (TabConfig) tabConfigMarker;
                tabs.add(tabConfig);

            }

        }
        FormProcessingUtil.failIfErrors(formConfig.getName(), errors);
        formConfig.getMarkup().getBody().setTabs(tabs);
        return formConfig;
    }

    @Override
    public boolean hasTemplateBasedElements(FormConfig formConfig) {
        List<TabConfigMarker> tabConfigMarkers = formConfig.getMarkup().getBody().getTabConfigMarkers();
        for (TabConfigMarker tabConfigMarker : tabConfigMarkers) {
            if(isTemplateBasedTab(tabConfigMarker)){
                return true;
            }
        }
        return false;
    }

    private void processTemplate(TemplateBasedTabConfig templateBasedTabConfig,List<WidgetConfig> formWidgetConfigs,
                                 List<TabConfig> tabConfigs, List<String> errors){
        String templateName = templateBasedTabConfig.getTemplate();
        FormTabTemplateConfig template = configurationExplorer.getConfig(FormTabTemplateConfig.class, templateName);
        if(template == null){
            errors.add(String.format("Tab template with name '%s' wasn't found\n", templateName));
        } else {
            String idsPrefix = templateBasedTabConfig.getIdsPrefix();
            processTabTemplate(template.getTabConfig(), idsPrefix, tabConfigs);
            FormProcessingUtil.processWidgetConfigs(idsPrefix, formWidgetConfigs,
                    template.getWidgetConfigurationConfig().getWidgetConfigList());
        }

    }

    private void processTabTemplate(TabConfig template, String idsPrefix, List<TabConfig> tabConfigs){
        TabConfig tabConfig = ObjectCloner.getInstance().cloneObject(template);
        if(idsPrefix != null){
            FormProcessingUtil.processTabIds(idsPrefix, tabConfig);
        }
        tabConfigs.add(tabConfig);
    }

    private boolean isTemplateBasedTab(TabConfigMarker tabConfigMarker){
        return TemplateBasedTabConfig.class == tabConfigMarker.getClass();
    }
}
