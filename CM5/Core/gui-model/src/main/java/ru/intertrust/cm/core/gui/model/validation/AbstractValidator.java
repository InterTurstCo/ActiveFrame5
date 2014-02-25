package ru.intertrust.cm.core.gui.model.validation;

/**
 * @author Lesia Puhova
 *         Date: 25.02.14
 *         Time: 16:53
 */
public abstract class AbstractValidator implements Validator {

    @Override
    public ValidationResult validate(CanBeValidated canBeValidated, Object info) {
        ValidationResult validationResult = new ValidationResult();
        if (canBeValidated != null && canBeValidated.getValue() != null) {
            doValidation(canBeValidated, validationResult);
        }
        if (!validationResult.isEmpty()) {
            canBeValidated.showErrors(validationResult);
        }
        return validationResult;
    }

    abstract void doValidation(CanBeValidated canBeValidated, ValidationResult validationResult);

}
