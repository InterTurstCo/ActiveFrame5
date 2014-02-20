package ru.intertrust.cm.core.gui.model.validation;

/**
 * @author Lesia Puhova
 *         Date: 17.02.14
 *         Time: 14:20
 */
public class ValidationMessage {
    private final String message;
    private final Severity severity;

    public ValidationMessage(String message, Severity severity) {
        this.message = message;
        this.severity = severity;
    }

    public ValidationMessage(String message) {
        this.message = message;
        this.severity = Severity.ERROR;
    }

    public String getMessage() {
        return message;
    }

    public Severity getSeverity() {
        return severity;
    }

    public enum Severity {
        ERROR,
        WARNING,
        INFO
    }

    @Override
    public String toString() {
        return message;
    }
}
