package ru.intertrust.cm.core.gui.impl.server.validation.validators;

import java.util.logging.Logger;

import ru.intertrust.cm.core.business.api.dto.Constraint;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.validation.ValidationResult;

/**
 * @author Lesia Puhova
 *         Date: 06.03.14
 *         Time: 14:05
 */
public class LengthValidator implements ServerValidator {

    private static Logger log = Logger.getLogger(LengthValidator.class.getName());

    private final Integer length;
    private final Integer minLength;
    private final Integer maxLength;

    public LengthValidator(Constraint constraint) {
        String lengthStr = constraint.param(Constraint.PARAM_LENGTH);
        String minLengthStr = constraint.param(Constraint.PARAM_MIN_LENGTH);
        String maxLengthStr = constraint.param(Constraint.PARAM_MAX_LENGTH);
        this.length = parseInt(lengthStr);
        this.minLength = parseInt(minLengthStr);
        this.maxLength = parseInt(maxLengthStr);
    }

    @Override
    public ValidationResult validate(Dto dtoToValidate) {
        ValidationResult validationResult = new ValidationResult();
        if (dtoToValidate instanceof Value) {
            Value value = (Value)dtoToValidate;
            if (value.get() != null) {
                int valueLength = value.toString().length();
                if (valueLength != 0) {
                    if (length != null && valueLength != length) {
                        validationResult.addError("validate.length.not-equal");
                        log.info("Server validator '" + this + "' found an error while validation DTO: " + dtoToValidate);
                    } else {
                        if (minLength != null && valueLength < minLength) {
                            validationResult.addError("validate.length.too-small");
                            log.info("Server validator '" + this + "' found an error while validation DTO: " + dtoToValidate);
                        }
                        if (maxLength != null && valueLength > maxLength) {
                            validationResult.addError("validate.length.too-big");
                            log.info("Server validator '" + this + "' found an error while validation DTO: " + dtoToValidate);
                        }
                    }
                }
            }
        }
        return validationResult;
    }

    private static Integer parseInt(String str) {
        return str != null ? Integer.parseInt(str) : null;
    }

    @Override
    public void init(final FormState formState) {
        // do nothing
    }

    @Override
    public String toString() {
        return "Server length validator: "
                + (length != null ? "length = " + length : "")
                + (minLength != null ? "minLength = " + minLength : "")
                + (maxLength != null ? "maxLength = " + maxLength : "");
    }
}
