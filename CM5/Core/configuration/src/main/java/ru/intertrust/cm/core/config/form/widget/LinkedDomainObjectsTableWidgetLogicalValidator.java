package ru.intertrust.cm.core.config.form.widget;

import ru.intertrust.cm.core.config.LogicalErrors;
import ru.intertrust.cm.core.config.WidgetConfigurationToValidate;
import ru.intertrust.cm.core.config.gui.form.widget.HasLinkedFormMappings;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 16.08.2015
 *         Time: 18:41
 */
public class LinkedDomainObjectsTableWidgetLogicalValidator extends AbstractWidgetLogicalValidator {

    @Override
    public void validate(WidgetConfigurationToValidate widget, LogicalErrors logicalErrors) {
        HasLinkedFormMappings config = (HasLinkedFormMappings) widget.getWidgetConfig();
        validateIfFormsExist(widget, config, logicalErrors);
    }

}
