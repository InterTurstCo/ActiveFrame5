package ru.intertrust.cm.core.gui.impl.client.validation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.validation.CanBeValidated;
import ru.intertrust.cm.core.gui.model.validation.ValidationResult;
import ru.intertrust.cm.core.gui.model.validation.Validator;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

/**
 * @author Lesia Puhova
 *         Date: 24.02.14
 *         Time: 12:04
 */
public class DateValidator implements Validator {

    @Override
    public ValidationResult validate(final CanBeValidated canBeValidated, Object info) {
        ValidationResult validationResult = new ValidationResult();
        if (canBeValidated != null && canBeValidated.getValue() instanceof String) {
            Dto request = new DateValidationRequest(canBeValidated.getValue());
            Command command = new Command("validate", "date-validation", request);
            BusinessUniverseServiceAsync.Impl.getInstance().executeCommand(command, new AsyncCallback<ValidationResult>() {
                @Override
                public void onSuccess(ValidationResult result) {
                    GWT.log("Validation Result got from server: " + result);
                    if (!result.isEmpty()) {
                        canBeValidated.showErrors(result);
                    }
                }

                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("Async call to server during validation failed:", caught);
                }
            });
        }
        return validationResult; // how to return correct result? async call may complete after the method returning
    }

}
