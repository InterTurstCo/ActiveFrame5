package ru.intertrust.cm.core.config.form.widget;

import ru.intertrust.cm.core.config.LogicalErrors;
import ru.intertrust.cm.core.config.WidgetConfigurationToValidate;

import static ru.intertrust.cm.core.config.form.widget.WidgetLogicalValidatorHelper.fieldTypeIsBoolean;
import static ru.intertrust.cm.core.config.form.widget.WidgetLogicalValidatorHelper.fieldTypeIsThruReference;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 16.08.2015
 *         Time: 17:42
 */
public class CheckBoxWidgetLogicalValidator extends AbstractWidgetLogicalValidator {
    @Override
    public void validate(WidgetConfigurationToValidate widget, LogicalErrors logicalErrors) {
        String fieldType = widget.getFieldConfigToValidate().getFieldType().name();
        if (fieldTypeIsBoolean(fieldType) || fieldType.equals("REFERENCE")) {
            return;
        }
        String error = String.format("Field '%s' in  domain object '%s' isn't a boolean type",
                widget.getFieldConfigToValidate().getName(), widget.getDomainObjectTypeToValidate());
        logger.error(error);
        logicalErrors.addError(error);
    }

}
