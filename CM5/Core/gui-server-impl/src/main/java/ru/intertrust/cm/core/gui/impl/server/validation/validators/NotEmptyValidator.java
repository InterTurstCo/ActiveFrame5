package ru.intertrust.cm.core.gui.impl.server.validation.validators;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.gui.model.validation.ValidationResult;

/**
 * @author Lesia Puhova
 *         Date: 05.03.14
 *         Time: 17:05
 */
public class NotEmptyValidator implements ServerValidator {

    @Override
    public ValidationResult validate(Dto dtoToValidate) {
        ValidationResult validationResult = new ValidationResult();
        boolean containsError = false;
        if (dtoToValidate == null ) {
            containsError = true;
        }
        else if (dtoToValidate instanceof Value) {
            if (((Value)dtoToValidate).isEmpty()) {
               containsError = true;
            }
        }
        if (containsError) {
            validationResult.addError("validate.not-empty");
        }
        return validationResult;
    }


}
