package ru.intertrust.cm.core.gui.impl.client.validation;

import ru.intertrust.cm.core.business.api.dto.Constraint;
import ru.intertrust.cm.core.gui.impl.client.localization.PlatformDateTimeFormat;
import ru.intertrust.cm.core.gui.model.validation.CanBeValidated;
import ru.intertrust.cm.core.gui.model.validation.ValidationResult;
import ru.intertrust.cm.core.gui.model.validation.Validator;

import java.util.logging.Logger;


/**
 * @author Lesia Puhova
 *         Date: 24.02.14
 *         Time: 12:04
 */
public class DateValidator implements Validator {

    private final String pattern;
    private static Logger log = Logger.getLogger("ClientValidator");

    public DateValidator(Constraint constraint) {
        pattern = constraint.param(Constraint.PARAM_DATE_PATTERN);
    }

    @Override
    public ValidationResult validate(final CanBeValidated canBeValidated, Object info) {
        final ValidationResult validationResult = new ValidationResult();
        if (canBeValidated != null && canBeValidated.getValue() instanceof String &&
                !((String)canBeValidated.getValue()).isEmpty()) {
            try {
                PlatformDateTimeFormat.getFormat(pattern).parse((String)canBeValidated.getValue());
            } catch (IllegalArgumentException e) {
                validationResult.addError("Неверный формат даты");
            }
//            Dto request = new DateValidationRequest(canBeValidated.getValue(), pattern);
//            Command command = new Command("validate", "date-validation", request);
//            BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<ValidationResult>() {
//                @Override
//                public void onSuccess(ValidationResult result) {
//                    GWT.log("Validation Result got from server: " + result);
//                    if (!result.isEmpty()) {
//                        canBeValidated.showErrors(result);
//                        validationResult.append(result);
//                    }
//                }
//
//                @Override
//                public void onFailure(Throwable caught) {
//                    GWT.log("Async call to server during validation failed:", caught);
//                }
//            });
        }
        if (!validationResult.isEmpty()) {
            log.info("Client validator '" + this + "' found an error while validating object: " + canBeValidated);
            if (canBeValidated != null) {
                canBeValidated.showErrors(validationResult);
            }
        }
        return validationResult; // how to return correct result? async call may complete after the method returning
    }

}
