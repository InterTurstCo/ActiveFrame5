package ru.intertrust.cm.core.config.form.widget;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.LogicalErrors;
import ru.intertrust.cm.core.config.WidgetConfigurationToValidate;
import ru.intertrust.cm.core.config.base.CollectionConfig;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.HasLinkedFormMappings;
import ru.intertrust.cm.core.config.gui.form.widget.LinkedFormConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionRefConfig;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 16.08.2015
 *         Time: 17:02
 */
public abstract class AbstractWidgetLogicalValidator implements WidgetLogicalValidator {

    protected final static Logger logger = LoggerFactory.getLogger(AbstractWidgetLogicalValidator.class);

    protected ConfigurationExplorer configurationExplorer;

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    protected CollectionConfig validateIfCollectionExists(WidgetConfigurationToValidate widget,
                                                          CollectionRefConfig collectionRefConfig, LogicalErrors logicalErrors) {
        if (collectionRefConfig != null) {
            String collectionName = collectionRefConfig.getName();
            return validateIfCollectionExists(widget, collectionName, logicalErrors);
        }

        return null;
    }

    protected CollectionConfig validateIfCollectionExists(WidgetConfigurationToValidate widget,
                                                          String collectionName, LogicalErrors logicalErrors) {
        CollectionConfig config = (CollectionConfig) findRequiredConfigByClassAndName(CollectionConfig.class, collectionName);
        if (config == null) {
            String error = String.format("Collection '%s' for %s with id '%s' wasn't found",
                    collectionName, widget.getWidgetConfig().getComponentName(), widget.getWidgetConfig().getId());
            logger.error(error);
            logicalErrors.addError(error);

        }
        return config;
    }


    protected void validateIfFormsExist(WidgetConfigurationToValidate widget, HasLinkedFormMappings config, LogicalErrors logicalErrors) {
        if (!widget.isMethodValidated("validateExistingForms")) {
            widget.addValidatedMethod("validateExistingForms");
            String widgetId = widget.getWidgetConfig().getId();
            if (config.getLinkedFormMappingConfig() != null) {
                List<LinkedFormConfig> linkedFormConfigs = config.getLinkedFormMappingConfig().getLinkedFormConfigs();
                for (LinkedFormConfig linkedFormConfig : linkedFormConfigs) {
                    validateIfFormExist(widgetId, linkedFormConfig.getName(), logicalErrors);
                }
            }
            if (config.getLinkedFormConfig() != null) {
                validateIfFormExist(widgetId, config.getLinkedFormConfig().getName(), logicalErrors);
            }
        }
    }

    protected void validateIfFormExist(String widgetId, String formName, LogicalErrors logicalErrors) {
        FormConfig formConfig = configurationExplorer.getConfig(FormConfig.class, formName);
        if (formConfig == null) {
            String error = String.format("Linked form '%s' for widget with id '%s' wasn't found", formName, widgetId);
            logger.error(error);
            logicalErrors.addError(error);
        }
    }

    protected void validateIfFiltersExist(CollectionConfig collectionConfig, String filterName, LogicalErrors logicalErrors) {
        if (filterName == null) {
            return;
        }
        List<String> filtersFromCollectionConfig = WidgetLogicalValidatorHelper.getFiltersFromCollectionConfig(collectionConfig);
        if (!filtersFromCollectionConfig.contains(filterName)) {
            String error = String.format("Collection '%s' has no filter '%s'", collectionConfig.getName(), filterName);
            logicalErrors.addError(error);
        }
    }

    protected TopLevelConfig findRequiredConfigByClassAndName(Class classOfConfig, String name) {
        return (TopLevelConfig) configurationExplorer.getConfig(classOfConfig, name);
    }


}
