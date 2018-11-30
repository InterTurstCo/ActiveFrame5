package ru.intertrust.cm.core.business.api.dto.universalentity;

public class ConfigurationException extends RuntimeException {

    public ConfigurationException (final String reason) {
        super(reason);
    }

}