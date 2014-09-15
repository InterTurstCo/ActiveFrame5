package ru.intertrust.cm.core.config;

import java.util.List;

/**
 * Common interface for Configuration validators
 */
public interface ConfigurationValidator {

    List<LogicalErrors> validate();
}
