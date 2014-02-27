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
        this.length = (Integer) constraint.param(Constraint.PARAM_LENGTH);
        this.minLength = (Integer) constraint.param(Constraint.PARAM_MIN_LENGTH);
        this.maxLength = (Integer) constraint.param(Constraint.PARAM_MAX_LENGTH);
    }

    @Override
    void doValidation(CanBeValidated canBeValidated, ValidationResult validationResult) {
        String value = canBeValidated.getValue().toString();
        if (length != null && value.length() != length) {
            validationResult.addError("validate.length.not-equal");
        } else {
            if (minLength != null && value.length() < minLength) {
                validationResult.addError("validate.length.too-small");
            }
            if (maxLength != null && value.length() > maxLength) {
                validationResult.addError("validate.length.too-big");
            }
        }
    }
}
