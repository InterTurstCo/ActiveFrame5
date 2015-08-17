package ru.intertrust.cm.core.config.form.widget;

import ru.intertrust.cm.core.config.LogicalErrors;
import ru.intertrust.cm.core.config.WidgetConfigurationToValidate;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 16.08.2015
 *         Time: 17:44
 */
public interface WidgetLogicalValidator {
    void validate(WidgetConfigurationToValidate widget, LogicalErrors logicalErrors);
}
