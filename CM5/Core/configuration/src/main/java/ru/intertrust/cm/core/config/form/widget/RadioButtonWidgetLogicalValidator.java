package ru.intertrust.cm.core.config.form.widget;

import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.config.gui.form.widget.PatternConfig;
import ru.intertrust.cm.core.config.gui.form.widget.RadioButtonConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 16.08.2015
 *         Time: 17:53
 */
public class RadioButtonWidgetLogicalValidator extends AbstractWidgetLogicalValidator {
    private static final Pattern PATTERN_VALIDATE = Pattern.compile("^[^{}]*(\\{[^{}]+\\}[^{}]*)*$");
    private static final Pattern PATTERN_FIND_PLACEHOLDERS = Pattern.compile("\\{([^{}]+)\\}");

    @Override
    public void validate(WidgetConfigurationToValidate widget, LogicalErrors logicalErrors) {
        validatePattern(widget, logicalErrors);
    }

    private void validatePattern(WidgetConfigurationToValidate widget, LogicalErrors logicalErrors) {
        if (widget.getNumberOfParts() > 0) {
            return;
        }
        RadioButtonConfig config = (RadioButtonConfig) widget.getWidgetConfig();
        PatternConfig patternConfig = config.getPatternConfig();
        String pattern = patternConfig.getValue();

        String widgetName = widget.getWidgetConfig().getComponentName();
        String widgetId = widget.getWidgetConfig().getId();
        if (pattern.isEmpty()) {
            String error = String.format("Pattern is empty for %s with id '%s'", widgetName, widgetId);
            logger.error(error);
            logicalErrors.addError(error);
        } else if (!patternFormatIsValid(pattern)) {
            String error = String.format("Pattern format is not valid for %s with id '%s'", widgetName, widgetId);
            logger.error(error);
            logicalErrors.addError(error);
        } else {
            ReferenceFieldConfig fieldConfig = (ReferenceFieldConfig) widget.getFieldConfigToValidate();
            String domainObjectType = fieldConfig.getType();

            List<String> fieldNames = getFieldNames(domainObjectType);

            List<String> placeholders = findPatternPlaceholders(pattern);
            for (String placeholder : placeholders) {
                if (!fieldNames.contains(placeholder)) {
                    String error = String.format("Incorrect pattern placeholder '%s' found for domain object '%s' in %s with id '%s'", placeholder, domainObjectType, widgetName, widgetId);
                    logger.error(error);
                    logicalErrors.addError(error);
                }
            }
        }
    }

    private boolean patternFormatIsValid(String patternString) {
        return PATTERN_VALIDATE.matcher(patternString).matches();
    }

    private List<String> findPatternPlaceholders(String patternString) {
        Matcher m = PATTERN_FIND_PLACEHOLDERS.matcher(patternString);
        List<String> placeholders = new ArrayList<String>();
        while (m.find()) {
            placeholders.add(m.group(1));
        }
        return placeholders;
    }

    private List<String> getFieldNames(String domainObjectType) {
        DomainObjectTypeConfig domainObjectConfig = configurationExplorer.getConfig(DomainObjectTypeConfig.class,
                domainObjectType);
        List<FieldConfig> fieldsConfigs = domainObjectConfig.getFieldConfigs();

        List<String> fieldNames = new ArrayList<>(fieldsConfigs.size());
        for (FieldConfig fieldConfig : fieldsConfigs) {
            fieldNames.add(fieldConfig.getName());
        }
        return fieldNames;
    }

}
