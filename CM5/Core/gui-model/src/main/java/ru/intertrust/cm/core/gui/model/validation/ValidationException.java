package ru.intertrust.cm.core.gui.model.validation;

import ru.intertrust.cm.core.model.NonRollingBackException;
import ru.intertrust.cm.core.model.SystemException;

import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 05.03.14
 *         Time: 14:39
 */
public class ValidationException extends NonRollingBackException {

    private List<String> validationErrors;

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

    public ValidationException(List<String> validationErrors){
        this.validationErrors = validationErrors;
    }

    public ValidationException(String message, List<String> validationErrors) {
        super(message);
        this.validationErrors = validationErrors;
    }

    public ValidationException(String message, Throwable cause, List<String> validationErrors) {
        super(message, cause);
        this.validationErrors = validationErrors;
    }

    public ValidationException(Throwable cause, List<String> validationErrors) {
        super(cause);
        this.validationErrors = validationErrors;
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(List<String> validationErrors) {
        this.validationErrors = validationErrors;
    }
}
