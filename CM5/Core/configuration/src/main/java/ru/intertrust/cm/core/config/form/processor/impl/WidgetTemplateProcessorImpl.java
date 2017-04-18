package ru.intertrust.cm.core.config.form.processor.impl;

import ru.intertrust.cm.core.business.api.util.ObjectCloner;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.form.processor.FormProcessingUtil;
import ru.intertrust.cm.core.config.form.processor.WidgetTemplateProcessor;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.config.gui.form.widget.template.OverrideConfig;
import ru.intertrust.cm.core.config.gui.form.widget.template.TemplateBasedWidgetConfig;
import ru.intertrust.cm.core.config.gui.form.widget.template.WidgetTemplateConfig;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 10.08.2015
 *         Time: 9:44
 */
public class WidgetTemplateProcessorImpl implements WidgetTemplateProcessor {

    private ConfigurationExplorer configurationExplorer;

    public WidgetTemplateProcessorImpl() {
    }

    public WidgetTemplateProcessorImpl(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    @Override
    public boolean hasTemplateBasedWidgets(List<WidgetConfig> widgetConfigs) {
        boolean result = false;
        for (WidgetConfig widgetConfig : widgetConfigs) {
            if (isTemplateBased(widgetConfig)) {
                result = true;
                break;
            }
        }
        return result;
    }

    @Override
    public List<WidgetConfig> processTemplates(String formName, List<WidgetConfig> widgetConfigs) {
        Iterator<WidgetConfig> iterator = widgetConfigs.iterator();
        List<WidgetConfig> processedWidgets = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        while (iterator.hasNext()) {
            WidgetConfig widgetConfig = iterator.next();
            if (isTemplateBased(widgetConfig)) {
                iterator.remove();
                addProcessed(widgetConfig, processedWidgets, errors);
            }
        }
        FormProcessingUtil.failIfErrors(formName, errors);
        widgetConfigs.addAll(processedWidgets);
        return widgetConfigs;
    }

    private void addProcessed(WidgetConfig widgetConfig, List<WidgetConfig> processedWidgets, List<String> errors) {
        TemplateBasedWidgetConfig templateBasedWidgetConfig = (TemplateBasedWidgetConfig) widgetConfig;
        String templateName = templateBasedWidgetConfig.getTemplateName();
        WidgetTemplateConfig templateConfig = configurationExplorer.getConfig(WidgetTemplateConfig.class, templateName);
        if (templateConfig == null) {
            errors.add(String.format("Widget template with name '%s' wasn't found\n", templateName));
        } else if (areFieldsNoIntersecting(templateBasedWidgetConfig, errors)) {
            WidgetConfig processed = processTemplate(templateConfig, templateBasedWidgetConfig);
            processedWidgets.add(processed);
        }

    }

    private String getId(TemplateBasedWidgetConfig templateBasedWidgetConfig) {
        String idFromOverride = templateBasedWidgetConfig.getOverrideConfig() == null ? null
                : templateBasedWidgetConfig.getOverrideConfig().getWidgetConfig().getId();
        return idFromOverride == null ? templateBasedWidgetConfig.getId() : idFromOverride;
    }

    private boolean areFieldsNoIntersecting(TemplateBasedWidgetConfig templateBasedWidgetConfig, List<String> errors)  {
        OverrideConfig overrideConfig = templateBasedWidgetConfig.getOverrideConfig();
        if(overrideConfig == null){
            return true;
        }
        Field[] fields = WidgetConfig.class.getDeclaredFields(); //checking intersection only on WidgetConfig fields
        for (Field field : fields) {
            if(Modifier.isFinal(field.getModifiers())){
                continue; // no need to check constant intersection
            }
            checkFieldIntersecting(field, templateBasedWidgetConfig, errors);
        }
        return errors.isEmpty();
    }

    private void checkFieldIntersecting(Field templateField, TemplateBasedWidgetConfig templateBasedWidgetConfig, List<String> errors){
        WidgetConfig widgetConfig = templateBasedWidgetConfig.getOverrideConfig().getWidgetConfig();
        Class widgetConfigClass = WidgetConfig.class;
        String fieldName = templateField.getName();
        try {
            templateField.setAccessible(true);
            Object templateFieldValue = templateField.get(templateBasedWidgetConfig);
            Field overrideWidgetField =  widgetConfigClass.getDeclaredField(fieldName);
            overrideWidgetField.setAccessible(true);
            Object overrideWidgetFieldValue = overrideWidgetField.get(widgetConfig);
            if(templateFieldValue != null && overrideWidgetFieldValue != null){
                errors.add(String.format("Template based widget with id '%s' has field '%s' conflict\n", getId(templateBasedWidgetConfig), fieldName));
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            //do nothing - no intersection
            e.printStackTrace();
        }
    }

    private WidgetConfig processTemplate(WidgetTemplateConfig templateConfig,
                                         TemplateBasedWidgetConfig templateBasedWidgetConfig) {
        WidgetConfig widgetConfig = ObjectCloner.getInstance().cloneObject(templateConfig.getWidgetConfig());
        FormProcessingUtil.copyNotNullProperties(templateBasedWidgetConfig, widgetConfig);
        if (templateBasedWidgetConfig.getOverrideConfig() != null) {
           FormProcessingUtil.copyNotNullProperties(templateBasedWidgetConfig.getOverrideConfig().getWidgetConfig(), widgetConfig);
        }
        return widgetConfig;
    }

    private boolean isTemplateBased(WidgetConfig widgetConfig) {
        return TemplateBasedWidgetConfig.COMPONENT_NAME.equalsIgnoreCase(widgetConfig.getComponentName());
    }

    @Override
    public FormConfig processTemplates(FormConfig formConfig) {
        List<WidgetConfig> widgetConfigs = processTemplates(formConfig.getName(),
                formConfig.getWidgetConfigurationConfig().getWidgetConfigList());
        formConfig.getWidgetConfigurationConfig().setWidgetConfigList(widgetConfigs);
        return formConfig;
    }

    @Override
    public boolean hasTemplateBasedElements(FormConfig formConfig) {
        return hasTemplateBasedWidgets(formConfig.getWidgetConfigurationConfig().getWidgetConfigList());
    }
}
