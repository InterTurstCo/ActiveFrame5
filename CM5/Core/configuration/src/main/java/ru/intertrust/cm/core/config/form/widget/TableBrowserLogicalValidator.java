package ru.intertrust.cm.core.config.form.widget;

import ru.intertrust.cm.core.config.LogicalErrors;
import ru.intertrust.cm.core.config.WidgetConfigurationToValidate;
import ru.intertrust.cm.core.config.base.CollectionConfig;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionViewConfig;
import ru.intertrust.cm.core.config.gui.form.widget.TableBrowserConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 16.08.2015
 *         Time: 17:44
 */
public class TableBrowserLogicalValidator extends AbstractWidgetLogicalValidator {
    @Override
    public void validate(WidgetConfigurationToValidate widget, LogicalErrors logicalErrors) {
        TableBrowserConfig config = (TableBrowserConfig) widget.getWidgetConfig();
        CollectionConfig collectionConfig = validateIfCollectionExists(widget, config.getCollectionRefConfig(), logicalErrors);
        if(collectionConfig != null && config.getInputTextFilterConfig() != null){
            validateIfFiltersExist(collectionConfig, config.getInputTextFilterConfig().getName(), logicalErrors);
        }
        String collectionViewName = config.getCollectionViewRefConfig().getName();
        validateIfCollectionViewExists(widget, collectionViewName, logicalErrors);
        validateIfFormsExist(widget, config, logicalErrors);
    }

    private void validateIfCollectionViewExists(WidgetConfigurationToValidate widget,
                                                String collectionViewName, LogicalErrors logicalErrors) {
        TopLevelConfig config = findRequiredConfigByClassAndName(CollectionViewConfig.class, collectionViewName);
        if (config == null) {
            String error = String.format("Collection view '%s' for %s with id '%s' wasn't found",
                    collectionViewName, widget.getWidgetConfig().getComponentName(), widget.getWidgetConfig().getId());
            logger.error(error);
            logicalErrors.addError(error);
        }

    }

}
