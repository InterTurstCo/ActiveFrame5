package ru.intertrust.cm.core.gui.model.validation;

import ru.intertrust.cm.core.gui.model.GuiException;

/**
 * @author Lesia Puhova
 *         Date: 05.03.14
 *         Time: 14:39
 */
public class ValidationException extends GuiException {

    private ValidationResult validationResult;

    public ValidationException(){}

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidationException(Throwable cause) {
        super(cause);
    }

    public ValidationException(ValidationResult validationResult){
        this.validationResult = validationResult;
    }

    public ValidationException(String message, ValidationResult validationResult) {
        super(message);
        this.validationResult = validationResult;
    }

    public ValidationException(String message, Throwable cause, ValidationResult validationResult) {
        super(message, cause);
        this.validationResult = validationResult;
    }

    public ValidationException(Throwable cause, ValidationResult validationResult) {
        super(cause);
        this.validationResult = validationResult;
    }

    public ValidationResult getValidationResult() {
        return validationResult;
    }

    public void setValidationResult(ValidationResult validationResult) {
        this.validationResult = validationResult;
    }
}
