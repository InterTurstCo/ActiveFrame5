package ru.intertrust.cm.core.gui.model.validation;

import ru.intertrust.cm.core.business.api.dto.Constraint;

/**
 * Валидатор длины строкового представления значения.
 * @author Lesia Puhova
 *         Date: 25.02.14
 *         Time: 12:52
 */
public class LengthValidator extends AbstractValidator {

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

    private static Integer parseInt(String str) {
        return str != null ? Integer.parseInt(str) : null;
    }

    @Override
    void doValidation(CanBeValidated canBeValidated, ValidationResult validationResult) {
        if (canBeValidated.getValue() != null) {
            int valueLength = canBeValidated.getValue().toString().length();
            if (valueLength == 0) {
                return; //considering we use "not-empty" validator
            }
            if (length != null && valueLength != length) {
                validationResult.addError("validate.length.not-equal");
            } else {
                if (minLength != null && valueLength < minLength) {
                    validationResult.addError("validate.length.too-small");
                }
                if (maxLength != null && valueLength > maxLength) {
                    validationResult.addError("validate.length.too-big");
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Client length validator: "
                + (length != null ? "length = " + length : "")
                + (minLength != null ? "minLength = " + minLength : "")
                + (maxLength != null ? "maxLength = " + maxLength : "");
    }
}
