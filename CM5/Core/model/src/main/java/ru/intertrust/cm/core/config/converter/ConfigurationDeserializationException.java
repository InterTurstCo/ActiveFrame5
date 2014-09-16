package ru.intertrust.cm.core.config.converter;

import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents exception that occurs during configuration deserialization. Contains list of all errors occurred.
 */
public class ConfigurationDeserializationException extends ConfigurationException {

    private String configurationFilePath;
    private List<String> errorList = new ArrayList<>();

    public ConfigurationDeserializationException(List<String> errorList) {
        this.errorList.addAll(errorList);
    }

    public void setConfigurationFilePath(String configurationFilePath) {
        this.configurationFilePath = configurationFilePath;
    }

    @Override
    public String getMessage() {
        StringBuilder errorMessage = new StringBuilder("Configuration deserialization errors were encountered");
        if (configurationFilePath != null) {
            errorMessage.append(" in ").append(configurationFilePath);
        }

        errorMessage.append(":\n");

        for (String error : errorList) {
            errorMessage.append(error).append("\n");
        }

        return errorMessage.toString();
    }

}
