package ru.intertrust.cm.core.config.form.widget;

import ru.intertrust.cm.core.config.LogicalErrors;
import ru.intertrust.cm.core.config.WidgetConfigurationToValidate;
import ru.intertrust.cm.core.config.gui.form.widget.EnumBoxConfig;
import ru.intertrust.cm.core.config.gui.form.widget.EnumMapConfig;
import ru.intertrust.cm.core.config.gui.form.widget.EnumMappingConfig;

import java.math.BigDecimal;

import static ru.intertrust.cm.core.config.form.widget.WidgetLogicalValidatorHelper.*;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 16.08.2015
 *         Time: 17:59
 */
public class EnumBoxWidgetLogicalValidator extends AbstractWidgetLogicalValidator {
    @Override
    public void validate(WidgetConfigurationToValidate widget, LogicalErrors logicalErrors) {
        String fieldType = widget.getFieldConfigToValidate().getFieldType().name();
        EnumBoxConfig config = (EnumBoxConfig) widget.getWidgetConfig();
        if (!fieldTypeIsString(fieldType) && !fieldTypeIsBoolean(fieldType) &&
                !fieldTypeIsLong(fieldType) && !fieldTypeIsDecimal(fieldType)
                && !fieldTypeIsThruReference(fieldType) ) {
            String error = String.format("Invalid field type '%s' for enumeration-box with id '%s'." +
                    "Only String, Boolean, Long and Decimal are allowed", fieldType, config.getId());
            logger.error(error);
            logicalErrors.addError(error);
        }
        EnumMappingConfig mappingConfig = config.getEnumMappingConfig();
        if (mappingConfig != null) {
            for (EnumMapConfig mapConfig : mappingConfig.getEnumMapConfigs()) {
                String value = mapConfig.getValue();

                checkTypeForEnumBoxMapping(value, fieldType, config.getId(), logicalErrors);

                if (fieldTypeIsString(fieldType) && mapConfig.isNullValue() && value != null) {
                    String error = String.format("Mapping for enumeration-box with id '%s' contains both 'null-value' " +
                            "and 'value' attributes", config.getId());
                    logger.error(error);
                    logicalErrors.addError(error);
                } else if (!fieldTypeIsString(fieldType) && mapConfig.isNullValue() && value != null &&
                        !"".equals(value)) {
                    String error = String.format("Mapping for enumeration-box with id '%s' contains both 'null-value' " +
                            "and non-empty 'value' attributes", config.getId());
                    logger.error(error);
                    logicalErrors.addError(error);
                }
                if (value == null && mapConfig.getDisplayText() == null && !mapConfig.isNullValue()) {
                    String error = String.format("Mapping for enumeration-box with id '%s' contains neither value " +
                            "nor display-text, and is not-null", config.getId());
                    logger.error(error);
                    logicalErrors.addError(error);
                }
            }
        }
        validateMapping(config, logicalErrors);
    }

    private void checkTypeForEnumBoxMapping(String value, String fieldType, String widgetId, LogicalErrors logicalErrors) {
        boolean wrongType = false;
        if (value != null && !value.isEmpty()) {
            if (fieldTypeIsLong(fieldType)) {
                try {
                    Long.parseLong(value);
                } catch (NumberFormatException nfe) {
                    wrongType = true;
                }
            } else if (fieldTypeIsDecimal(fieldType)) {
                try {
                    new BigDecimal(value);
                } catch (NumberFormatException nfe) {
                    wrongType = true;
                }
            } else if (fieldTypeIsBoolean(fieldType)) {
                if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
                    wrongType = true;
                }
            }

            if (wrongType) {
                String error = String.format("Mapping for enumeration-box with id '%s' contains value of wrong type: %s.' " +
                        "Expected data type is %s.", widgetId, value, fieldType);
                logger.error(error);
                logicalErrors.addError(error);
            }
        }
    }

    private void validateMapping(EnumBoxConfig config,  LogicalErrors logicalErrors){
        if(config.getEnumMappingConfig() == null && config.getEnumMapProviderConfig() == null){
            String error = String.format("There is no tag 'mapping' or 'mapping-provider' in the 'enumeration-box with id '%s'",
                    config.getId());
            logicalErrors.addError(error);
        }
    }
}
