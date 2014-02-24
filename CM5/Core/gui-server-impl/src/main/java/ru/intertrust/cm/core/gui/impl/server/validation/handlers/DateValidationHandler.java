package ru.intertrust.cm.core.gui.impl.server.validation.handlers;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.validation.ValidationResult;

/**
 * @author Lesia Puhova
 *         Date: 24.02.14
 *         Time: 12:23
 */
@ComponentName("date-validation")
public class DateValidationHandler implements ComponentHandler {

    public ValidationResult validate(Dto request) {
        ValidationResult validationResult = new ValidationResult();

        //TODO: [validation] implement validation
        //...
        return validationResult;
    }
}
