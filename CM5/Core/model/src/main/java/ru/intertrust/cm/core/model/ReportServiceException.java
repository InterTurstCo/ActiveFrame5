package ru.intertrust.cm.core.model;

public class ReportServiceException extends NonRollingBackException {
    public ReportServiceException() {
    }

    public ReportServiceException(String message) {
        super(message);
    }

    public ReportServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReportServiceException(Throwable cause) {
        super(cause);
    }

    public ReportServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
