package ru.intertrust.cm.core.config;

import java.util.ArrayList;
import java.util.List;
/**
 * @author Yaroslav Bondacrhuk
 *         Date: 10/9/13
 *         Time: 17:05 PM
 */
public class LogicalErrors {

    private String configurationName;
    private String configurationType;
    private int errorCount;

    private List<String> errorsInConfiguration = new ArrayList<String>();;

    public static LogicalErrors getInstance(String name, String type){
        LogicalErrors logicalErrors = new LogicalErrors();
        logicalErrors.setConfigurationName(name);
        logicalErrors.setConfigurationType(type);

        return logicalErrors;
    }

    public static String toString(List<LogicalErrors> logicalErrorsList) {
        if (logicalErrorsList == null || logicalErrorsList.isEmpty()) {
            return "";
        }

        StringBuilder errorLogBuilder = new StringBuilder();
        for (LogicalErrors errors : logicalErrorsList) {
            if (errors.getErrorCount() > 0) {
                errorLogBuilder.append(errors.toString()).append("\n");
            }
        }

        return errorLogBuilder.toString();
    }

    public int getErrorCount() {
        return errorCount;
    }

    private String getConfigurationType() {
        return configurationType;
    }

    private void setConfigurationType(String configurationType) {
        this.configurationType = configurationType;
    }

    private String getConfigurationName() {
        return configurationName;
    }

    private void setConfigurationName(String configurationName) {
        this.configurationName = configurationName;
    }

    public void addError(String error){
        errorsInConfiguration.add(error);
        errorCount++;
    }

    public void addErrors(List<String> errors){
        errorsInConfiguration.addAll(errors);
        errorCount += errors.size();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Configuration of ");
        builder.append(configurationType);
        builder.append(" with name '");
        builder.append(configurationName);
        builder.append("' ");
        if(errorsInConfiguration.isEmpty()) {
            builder.append("was validated without errors");
        }  else {
            builder.append("was validated with errors.");
            builder.append("Count: ");
            builder.append(errorCount);
            builder.append(" Content:");
            for (String error :errorsInConfiguration) {
                builder.append("\n");
                builder.append(error);

            }
        }
        return builder.toString();
    }
}

