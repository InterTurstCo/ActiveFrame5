package ru.intertrust.cm.core.gui.model.validation;

import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 17.02.14
 *         Time: 14:24
 *
 *  Содержит информацию о результатах валидации.
 */
public class ValidationResult implements Dto {
    private ArrayList<ValidationMessage> messages = new ArrayList<ValidationMessage>(); // declared as ArrayList because List is not Serializable

    public List<ValidationMessage> getMessages() {
        return messages;
    }

    public void addError(String message) {
        messages.add(new ValidationMessage(message, ValidationMessage.Severity.ERROR));
    }

    public void addWarning(String message) {
        messages.add(new ValidationMessage(message, ValidationMessage.Severity.WARNING));
    }

    public void addInfo(String message) {
        messages.add(new ValidationMessage(message, ValidationMessage.Severity.INFO));
    }

    public boolean isEmpty() {
        return messages.isEmpty();
    }

    public List<ValidationMessage> getErrors() {
        return getMessages(ValidationMessage.Severity.ERROR);
    }

    public List<ValidationMessage> getWarnings() {
        return getMessages(ValidationMessage.Severity.WARNING);
    }

    public List<ValidationMessage> getInfos() {
        return getMessages(ValidationMessage.Severity.INFO);
    }

    public boolean hasErrors() {
        return hasMessages(ValidationMessage.Severity.ERROR);
    }

    public boolean hasWarnings() {
        return hasMessages(ValidationMessage.Severity.WARNING);
    }

    public boolean hasInfos() {
        return hasMessages(ValidationMessage.Severity.INFO);
    }

    public ValidationResult append(ValidationResult validationResult) {
        messages.addAll(validationResult.getMessages());
        return this;
    }

    private List<ValidationMessage> getMessages(ValidationMessage.Severity severity) {
        List<ValidationMessage> result = new ArrayList<ValidationMessage>();
        for (ValidationMessage msg : messages) {
            if (msg.getSeverity() == severity) {
                result.add(msg);
            }
        }
        return result;
    }

    private boolean hasMessages(ValidationMessage.Severity severity) {
        for (ValidationMessage msg : messages) {
            if (msg.getSeverity() == severity) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "ValidationResult: " + messages;
    }
}
