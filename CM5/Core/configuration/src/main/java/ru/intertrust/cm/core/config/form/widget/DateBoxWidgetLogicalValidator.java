package ru.intertrust.cm.core.config.form.widget;

import ru.intertrust.cm.core.config.LogicalErrors;
import ru.intertrust.cm.core.config.WidgetConfigurationToValidate;
import ru.intertrust.cm.core.config.gui.form.widget.datebox.DateBoxConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 16.08.2015
 *         Time: 18:39
 */
public class DateBoxWidgetLogicalValidator extends AbstractWidgetLogicalValidator {
    @Override
    public void validate(WidgetConfigurationToValidate widget, LogicalErrors logicalErrors) {
        DateBoxConfig config = (DateBoxConfig) widget.getWidgetConfig();
        if (config.getUnmanagedType() != null) {
            String error = String.format("Attribute 'unmanaged-type' is only allowed for report form", config.getId());
            logger.error(error);
            logicalErrors.addError(error);
        }
    }
}
