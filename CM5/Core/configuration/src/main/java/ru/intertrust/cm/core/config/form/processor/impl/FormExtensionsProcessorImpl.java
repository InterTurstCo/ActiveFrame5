package ru.intertrust.cm.core.config.form.processor.impl;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import ru.intertrust.cm.core.business.api.util.ObjectCloner;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ConfigurationExplorerImpl;
import ru.intertrust.cm.core.config.form.processor.FormExtensionProcessor;
import ru.intertrust.cm.core.config.form.processor.FormExtensionsProcessor;
import ru.intertrust.cm.core.config.form.processor.FormProcessingUtil;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.MarkupConfig;
import ru.intertrust.cm.core.config.gui.form.WidgetGroupsConfig;
import ru.intertrust.cm.core.config.gui.form.extension.FormExtensionOperation;
import ru.intertrust.cm.core.config.gui.form.extension.markup.MarkupExtensionConfig;
import ru.intertrust.cm.core.config.gui.form.extension.widget.configuration.WidgetConfigurationExtensionConfig;
import ru.intertrust.cm.core.config.gui.form.extension.widget.groups.WidgetGroupsExtensionConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfigurationConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 10.08.2015
 *         Time: 9:54
 */
public class FormExtensionsProcessorImpl implements FormExtensionsProcessor, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private ConfigurationExplorer configurationExplorer;

    public FormExtensionsProcessorImpl() {
    }

    public FormExtensionsProcessorImpl(ConfigurationExplorer configurationExplorer) {
        setConfigurationExplorer(configurationExplorer);
    }

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
        this.applicationContext = ((ConfigurationExplorerImpl) configurationExplorer).getContext();
    }

    @Override
    public FormConfig processExtensions(FormConfig formConfig) {
        List<FormConfig> parentFormConfigs = configurationExplorer.getParentFormConfigs(formConfig);
        return processExtensions(formConfig, parentFormConfigs);
    }

    @Override
    public boolean hasExtensions(FormConfig formConfig) {
        boolean result = false;
        if (formConfig != null) {
            result = formConfig.getExtends() != null
                    || formConfig.getMarkupExtensionConfig() != null
                    || formConfig.getWidgetConfigurationExtensionConfig() != null
                    || formConfig.getWidgetGroupsExtensionConfig() != null;
        }
        return result;
    }

    private FormConfig processExtensions(FormConfig rawFormConfig, List<FormConfig> formConfigs) {
        FormConfig result = null;
        List<String> errors = new ArrayList<>();
        if (requiredParentExists(rawFormConfig, formConfigs)) {
            result = buildPlainForm(rawFormConfig, formConfigs, errors);
        } else {
            errors.add(String.format("Parent form with name '%s' was not found", rawFormConfig.getExtends()));
        }
        FormProcessingUtil.failIfErrors(rawFormConfig.getName(), errors);

        return result;
    }

    private FormConfig buildPlainForm(FormConfig rawFormConfig, List<FormConfig> formConfigs, List<String> errors){
        FormConfig result = null;
        if (formConfigs.isEmpty()) {
            result = applyExtensions(rawFormConfig, errors);
        } else {
            result = buildInheritanceFormConfig(rawFormConfig, formConfigs, errors);
        }
        return result;
    }

    private boolean requiredParentExists(FormConfig rawFormConfig, List<FormConfig> formConfigs) {
        String parentName = rawFormConfig.getExtends();
        int parentFormsCount = formConfigs.size();
        return parentName == null
                || (parentFormsCount != 0 && formConfigs.get(parentFormsCount - 1).getName().equalsIgnoreCase(parentName));
    }

    private FormConfig buildInheritanceFormConfig(FormConfig rawConfig, List<FormConfig> formConfigs, List<String> errors) {
        FormConfig parentFormConfig = null;
        int size = formConfigs.size();
        for (int i = 0; i < size; i++) {
            FormConfig currentFormConfig = ObjectCloner.getInstance().cloneObject(formConfigs.get(i));
            if (parentFormConfig == null) {
                parentFormConfig = applyExtensions(currentFormConfig, errors);
            } else {
                if (parentFormConfig.getName().equalsIgnoreCase(currentFormConfig.getExtends())) {
                    parentFormConfig = applyExtensions(currentFormConfig, parentFormConfig, errors);
                } else {
                    errors.add(String.format("Parent form with name '%s' was not found", currentFormConfig.getExtends()));
                    return null;
                }
            }
        }

        return applyExtensions(rawConfig, parentFormConfig, errors);

    }

    private FormConfig applyExtensions(FormConfig rawForm, List<String> errors) {
        FormConfig formConfig = ObjectCloner.getInstance().cloneObject(rawForm);
        processMarkupExtension(formConfig.getMarkup(), formConfig.getMarkupExtensionConfig(), errors);
        processWidgetConfigurationExtension(formConfig.getWidgetConfigurationConfig(), formConfig.getWidgetConfigurationExtensionConfig(), errors);
        processWidgetGroupsExtension(formConfig.getWidgetGroupsConfig(), formConfig.getWidgetGroupsExtensionConfig(), errors);
        cleanExtensionConfigs(formConfig);
        return formConfig;
    }

    private FormConfig applyExtensions(FormConfig rawForm, FormConfig formConfig, List<String> errors) {
        applyMarkupExtension(rawForm, formConfig, errors);
        applyWidgetConfigurationExtension(rawForm, formConfig, errors);
        applyWidgetGroupsExtension(rawForm, formConfig, errors);
        applyToolbarExtension(rawForm, formConfig);
        applyFormSaveExtension(rawForm, formConfig);
        applyFormObjectsRemover(rawForm, formConfig);
        applyFormAttributes(rawForm, formConfig);
        cleanExtensionConfigs(formConfig);
        return formConfig;
    }

    private void applyMarkupExtension(FormConfig rawForm, FormConfig formConfig, List<String> errors) {
        if (rawForm.getMarkup() == null) {
            processMarkupExtension(formConfig.getMarkup(), rawForm.getMarkupExtensionConfig(), errors);
        } else {
            formConfig.setMarkup(rawForm.getMarkup());
            processMarkupExtension(formConfig.getMarkup(), rawForm.getMarkupExtensionConfig(), errors);
        }
    }

    private void applyWidgetConfigurationExtension(FormConfig rawForm, FormConfig formConfig, List<String> errors) {
        if (rawForm.getWidgetConfigurationConfig() == null) {
            processWidgetConfigurationExtension(formConfig.getWidgetConfigurationConfig(), rawForm.getWidgetConfigurationExtensionConfig(), errors);
        } else {
            formConfig.setWidgetConfigurationConfig(rawForm.getWidgetConfigurationConfig());
            processWidgetConfigurationExtension(formConfig.getWidgetConfigurationConfig(), rawForm.getWidgetConfigurationExtensionConfig(), errors);
        }
    }

    private void applyWidgetGroupsExtension(FormConfig rawForm, FormConfig formConfig, List<String> errors) {
        if (rawForm.getWidgetGroupsConfig() == null) {
            processWidgetGroupsExtension(formConfig.getWidgetGroupsConfig(), rawForm.getWidgetGroupsExtensionConfig(), errors);
        } else {
            formConfig.setWidgetGroupsConfig(rawForm.getWidgetGroupsConfig());
            processWidgetGroupsExtension(formConfig.getWidgetGroupsConfig(), rawForm.getWidgetGroupsExtensionConfig(), errors);
        }
    }

    private void applyToolbarExtension(FormConfig rawForm, FormConfig formConfig) {
        if (rawForm.getToolbarConfig() != null) {
            formConfig.setToolbarConfig(rawForm.getToolbarConfig());
        }
    }

    private void cleanExtensionConfigs(FormConfig formConfig) {
        formConfig.setMarkupExtensionConfig(null);
        formConfig.setWidgetConfigurationExtensionConfig(null);
        formConfig.setWidgetConfigurationExtensionConfig(null);
    }

    private void processMarkupExtension(MarkupConfig markupConfig, MarkupExtensionConfig markupExtensionConfig, List<String> errors) {
        if (markupExtensionConfig != null) {
            if (markupConfig == null) {
                errors.add("Could not process nullable markup config\n");
            } else {
                List<FormExtensionOperation> operations = markupExtensionConfig.getOperations();
                for (FormExtensionOperation operation : operations) {
                    FormExtensionProcessor processor = new MarkupExtensionProcessor(markupConfig, operation, errors);
                    processor.processFormExtension();

                }
            }
        }
    }

    private void processWidgetConfigurationExtension(WidgetConfigurationConfig widgetConfiguration, WidgetConfigurationExtensionConfig widgetConfigurationExtensionConfig,
                                                     List<String> errors) {
        if (widgetConfigurationExtensionConfig != null) {
            if (widgetConfiguration == null) {
                errors.add("Could not process nullable widget configuration config\n");
            } else {
                List<FormExtensionOperation> operations =
                        widgetConfigurationExtensionConfig.getWidgetConfigurationExtensionOperations();
                for (FormExtensionOperation operation : operations) {
                    FormExtensionProcessor processor = new WidgetConfigurationExtensionProcessor(widgetConfiguration, operation, errors);
                    processor.processFormExtension();
                }
            }
        }
    }

    private void processWidgetGroupsExtension(WidgetGroupsConfig widgetGroupsConfig, WidgetGroupsExtensionConfig widgetGroupsExtensionConfig,
                                              List<String> errors) {
        if (widgetGroupsExtensionConfig != null) {
            if (widgetGroupsConfig == null) {
                errors.add("Could not process nullable widget groups config\n");
            } else {
                List<FormExtensionOperation> operations =
                        widgetGroupsExtensionConfig.getWidgetGroupsExtensionOperations();
                for (FormExtensionOperation operation : operations) {
                    FormExtensionProcessor processor = new WidgetGroupsExtensionProcessor(widgetGroupsConfig, operation, errors);
                    processor.processFormExtension();

                }
            }
        }
    }

    private void applyFormSaveExtension(FormConfig rawForm, FormConfig formConfig) {
        if (rawForm.getFormSaveExtensionConfig() != null) {
            if (rawForm.getFormSaveExtensionConfig().getAfterSaveComponent() == null
                    && rawForm.getFormSaveExtensionConfig().getBeforeSaveComponent() == null) {
                formConfig.setFormSaveExtensionConfig(null);
            } else {
                formConfig.setFormSaveExtensionConfig(rawForm.getFormSaveExtensionConfig());
            }
        }
    }

    private void applyFormObjectsRemover(FormConfig rawForm, FormConfig formConfig) {
        if (rawForm.getFormObjectsRemoverConfig() != null) {
            if (rawForm.getFormObjectsRemoverConfig().getHandler() == null) {
                formConfig.setFormObjectsRemoverConfig(null);
            } else {
                formConfig.setFormObjectsRemoverConfig(rawForm.getFormObjectsRemoverConfig());
            }
        }
    }

    private void applyFormAttributes(FormConfig rawForm, FormConfig formConfig) {
        formConfig.setName(rawForm.getName());
        if (rawForm.getDomainObjectType() != null) {
            formConfig.setDomainObjectType(rawForm.getDomainObjectType());
        }
        if (rawForm.getNotSafeIsDefault() != null) {
            formConfig.setDefault(rawForm.isDefault());
        }
        if (rawForm.getNotSafeIsDebug() != null) {
            formConfig.setDebug(rawForm.getDebug());
        }
        if (rawForm.getMinWidth() != null) {
            formConfig.setMinWidth(rawForm.getMinWidth());
        }
        if (rawForm.getType() != null) {
            formConfig.setType(rawForm.getType());
        }
        if (rawForm.getReportTemplate() != null) {
            formConfig.setReportTemplate(rawForm.getReportTemplate());
        }
        if (rawForm.getDefaultValueSetter() != null) {
            formConfig.setDefaultValueSetter(rawForm.getDefaultValueSetter());
        }
        if (rawForm.reReadInSameTransaction() != null) {
            formConfig.setReReadInSameTransaction(rawForm.reReadInSameTransaction());
        }
        formConfig.setExtends(null);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}

