package ru.intertrust.cm.core.gui.model.validation;

import java.util.logging.Logger;

/**
 * @author Lesia Puhova
 *         Date: 25.02.14
 *         Time: 16:53
 */
public abstract class AbstractValidator implements Validator {

    private static Logger log = Logger.getLogger("ClientValidator");

    @Override
    public ValidationResult validate(CanBeValidated canBeValidated, Object info) {
        ValidationResult validationResult = new ValidationResult();
        if (canBeValidated != null && canBeValidated.getValue() != null) {
            doValidation(canBeValidated, validationResult);
        }
        if (!validationResult.isEmpty()) {
            log.info("Client validator '" + this + "' found an error while validating object: " + canBeValidated);
            canBeValidated.showErrors(validationResult);

        }
        return validationResult;
    }

    abstract void doValidation(CanBeValidated canBeValidated, ValidationResult validationResult);

}
