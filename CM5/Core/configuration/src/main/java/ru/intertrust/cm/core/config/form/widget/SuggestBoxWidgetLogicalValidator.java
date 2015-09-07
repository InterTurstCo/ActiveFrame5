package ru.intertrust.cm.core.config.form.widget;

import ru.intertrust.cm.core.config.LogicalErrors;
import ru.intertrust.cm.core.config.WidgetConfigurationToValidate;
import ru.intertrust.cm.core.config.base.CollectionConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SuggestBoxConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 16.08.2015
 *         Time: 17:10
 */
public class SuggestBoxWidgetLogicalValidator extends AbstractWidgetLogicalValidator {

    @Override
    public void validate(WidgetConfigurationToValidate widget, LogicalErrors logicalErrors) {
        SuggestBoxConfig config = (SuggestBoxConfig) widget.getWidgetConfig();

        CollectionConfig collectionConfig = validateIfCollectionExists(widget, config.getCollectionRefConfig(), logicalErrors);
        if(collectionConfig != null && config.getInputTextFilterConfig() != null){
            validateIfFiltersExist(collectionConfig, config.getInputTextFilterConfig().getName(), logicalErrors);
        }
        validateIfFormsExist(widget, config, logicalErrors);
    }

}
