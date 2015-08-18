package ru.intertrust.cm.core.config;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 17/12/13
 *         Time: 15:05 PM
 */
public interface WidgetConfigurationLogicalValidator {
    void validate(FormToValidate data, LogicalErrors logicalErrors);
}
