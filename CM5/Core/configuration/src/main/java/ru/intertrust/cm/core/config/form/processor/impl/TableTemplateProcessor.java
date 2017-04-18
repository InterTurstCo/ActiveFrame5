package ru.intertrust.cm.core.config.form.processor.impl;

import ru.intertrust.cm.core.business.api.util.ObjectCloner;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.form.processor.FormProcessingUtil;
import ru.intertrust.cm.core.config.form.processor.FormTemplateProcessor;
import ru.intertrust.cm.core.config.gui.form.*;
import ru.intertrust.cm.core.config.gui.form.template.FormTableTemplateConfig;
import ru.intertrust.cm.core.config.gui.form.template.TemplateBasedTableConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 25.08.2015
 *         Time: 9:02
 */
public class TableTemplateProcessor implements FormTemplateProcessor {

    private ConfigurationExplorer configurationExplorer;

    public TableTemplateProcessor() {
    }

    public TableTemplateProcessor(ConfigurationExplorer explorer) {
        configurationExplorer = explorer;
    }

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    @Override
    public FormConfig processTemplates(FormConfig formConfig) {
        List<String> errors = new ArrayList<>();
        processHeader(formConfig, errors);
        processBody(formConfig, errors);
        FormProcessingUtil.failIfErrors(formConfig.getName(), errors);
        return formConfig;
    }

    private void processHeader(FormConfig formConfig, List<String> errors){
        HeaderConfig headerConfig = formConfig.getMarkup().getHeader();
        List<WidgetConfig> formWidgetConfigs = formConfig.getWidgetConfigurationConfig().getWidgetConfigList();
        if (isHeaderTemplateBased(headerConfig)) {
            TableLayoutConfig headerTable = processTableTemplate(headerConfig.getTemplateBasedTableConfig(),
                    formWidgetConfigs, errors);
            headerConfig.setTableLayout(headerTable);
            headerConfig.setTemplateBasedTableConfig(null);
        }
    }

    private void processBody(FormConfig formConfig, List<String> errors){
        BodyConfig bodyConfig = formConfig.getMarkup().getBody();
        List<WidgetConfig> formWidgetConfigs = formConfig.getWidgetConfigurationConfig().getWidgetConfigList();
        if (isBodyTemplateBased(bodyConfig)) {
            List<TabConfig> tabConfigs = bodyConfig.getTabs();
            for (TabConfig tabConfig : tabConfigs) {
                processTab(tabConfig, formWidgetConfigs, errors);
            }
        }
    }

    private void processTab(TabConfig tabConfig, List<WidgetConfig> formWidgetConfigs, List<String> errors){
        List<TabGroupConfig> tabGroupConfigs = tabConfig.getGroupList().getTabGroupConfigs();
        for (TabGroupConfig tabGroupConfig : tabGroupConfigs) {
            if (isTabGroupTemplateBased(tabGroupConfig)) {
                TableLayoutConfig tableLayoutConfig = processTableTemplate(tabGroupConfig.getTemplateBasedTableConfig(),
                        formWidgetConfigs, errors);
                tabGroupConfig.setLayout(tableLayoutConfig);
                tabGroupConfig.setTemplateBasedTableConfig(null);
            }
        }
    }

    @Override
    public boolean hasTemplateBasedElements(FormConfig formConfig) {
        MarkupConfig markupConfig = formConfig.getMarkup();
        return isHeaderTemplateBased(markupConfig.getHeader())
                || isBodyTemplateBased(markupConfig.getBody());
    }

    private boolean isHeaderTemplateBased(HeaderConfig headerConfig) {
        return headerConfig.getTemplateBasedTableConfig() != null;
    }

    private boolean isBodyTemplateBased(BodyConfig bodyConfig) {
        boolean result = false;
        List<TabConfig> tabConfigs = bodyConfig.getTabs();
        for (TabConfig tabConfig : tabConfigs) {
            if (isTabTemplateBased(tabConfig)) {
                result = true;
                break;
            }
        }
        return result;
    }

    private boolean isTabTemplateBased(TabConfig tabConfig) {
        boolean result = false;
        List<TabGroupConfig> tabGroupConfigs = tabConfig.getGroupList().getTabGroupConfigs();
        for (TabGroupConfig tabGroupConfig : tabGroupConfigs) {
            if (isTabGroupTemplateBased(tabGroupConfig)) {
                result = true;
                break;
            }
        }
        return result;
    }

    private boolean isTabGroupTemplateBased(TabGroupConfig tabGroupConfig) {
        return tabGroupConfig.getTemplateBasedTableConfig() != null;
    }

    private TableLayoutConfig processTableTemplate(TemplateBasedTableConfig templateBasedTableConfig,
                                                   List<WidgetConfig> formWidgetConfigs, List<String> errors) {
        TableLayoutConfig result = null;
        String templateName = templateBasedTableConfig.getTemplate();
        FormTableTemplateConfig template = configurationExplorer.getConfig(FormTableTemplateConfig.class, templateName);
        if (template == null) {
            errors.add(String.format("Table template with name '%s' wasn't found\n", templateName));
        } else {
            String idsPrefix = templateBasedTableConfig.getIdsPrefix();
            result = processTableTemplate(template.getTableLayoutConfig(), idsPrefix);
            FormProcessingUtil.processWidgetConfigs(idsPrefix, formWidgetConfigs,
                    template.getWidgetConfigurationConfig().getWidgetConfigList());
        }
        return result;
    }

    private TableLayoutConfig processTableTemplate(TableLayoutConfig template, String idsPrefix) {
        TableLayoutConfig result = ObjectCloner.getInstance().cloneObject(template);
        if (idsPrefix != null) {
            FormProcessingUtil.processTableIds(idsPrefix, result);
        }
        return result;
    }
}
