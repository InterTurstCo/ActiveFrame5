package ru.intertrust.cm.core.business.api.dto;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.06.14
 *         Time: 17:15
 */
public class ConfigurationDeployedItem implements Dto {
    private boolean restartRequired;
    private boolean success;
    private String message;
    private String fileName;
    private static final String SUCCESS_BUT_RESTART_REQUIRED = "Кофнигурация загружена успешно. \n" +
            "Требуется перезагрузка сервера, чтобы изменения прменились.";
    private static final String SUCCESS = "Кофнигурация загружена успещно.";
    public boolean isRestartRequired() {
        return restartRequired;
    }

    public void setRestartRequired(boolean restartRequired) {
        this.restartRequired = restartRequired;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        if (success && restartRequired) {
            return SUCCESS_BUT_RESTART_REQUIRED;
        }
        if(success) {
            return SUCCESS;
        }
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ConfigurationDeployedItem that = (ConfigurationDeployedItem) o;

        if (restartRequired != that.restartRequired) {
            return false;
        }
        if (success != that.success) {
            return false;
        }
        if (fileName != null ? !fileName.equals(that.fileName) : that.fileName != null) {
            return false;
        }
        if (message != null ? !message.equals(that.message) : that.message != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = (restartRequired ? 1 : 0);
        result = 31 * result + (success ? 1 : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
        return result;
    }
}
