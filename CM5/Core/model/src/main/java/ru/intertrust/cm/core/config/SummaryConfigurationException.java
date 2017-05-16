package ru.intertrust.cm.core.config;

import java.util.Collection;

/**
 * @author Denis Mitavskiy
 *         Date: 16.05.2017
 *         Time: 21:45
 */
public class SummaryConfigurationException extends ConfigurationException {
    private Collection<ConfigurationException> problemDetails;

    public SummaryConfigurationException() {
    }

    public SummaryConfigurationException(Collection<ConfigurationException> problemDetails) {
        this.problemDetails = problemDetails;
    }

    public SummaryConfigurationException(String message) {
        super(message);
    }

    public SummaryConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SummaryConfigurationException(Throwable cause) {
        super(cause);
    }

    public SummaryConfigurationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public Collection<ConfigurationException> getProblemDetails() {
        return problemDetails;
    }

    @Override
    public String getMessage() {
        return getSummaryMessage();
    }

    public String getSummaryMessage() {
        StringBuilder resultMessage = new StringBuilder();
        for (ConfigurationException configurationException : problemDetails) {
            resultMessage.append(configurationException.getMessage()).append("\n");
        }
        return resultMessage.toString();
    }
}
