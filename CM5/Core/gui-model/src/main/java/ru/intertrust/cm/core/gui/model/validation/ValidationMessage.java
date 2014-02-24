package ru.intertrust.cm.core.gui.model.validation;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Lesia Puhova
 *         Date: 17.02.14
 *         Time: 14:20
 */
public class ValidationMessage implements Dto {
    private String message;
    private Severity severity;

    public ValidationMessage() {
    }

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
