package ru.intertrust.cm.core.model;

/**
 * 
 * @author atsvetkov
 *
 */
public class MailNotificationException extends NotificationException {

    public MailNotificationException() {
    }

    public MailNotificationException(String message) {
        super(message);
    }

    public MailNotificationException(String message, Throwable cause) {
        super(message, cause);
    }

    public MailNotificationException(Throwable cause) {
        super(cause);
    }

    public MailNotificationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
