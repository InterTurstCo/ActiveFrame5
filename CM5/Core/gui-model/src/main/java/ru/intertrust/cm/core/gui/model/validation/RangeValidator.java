package ru.intertrust.cm.core.gui.model.validation;

import ru.intertrust.cm.core.business.api.dto.Constraint;

/**
 * Валидатор диапазона значений.
 * @author Lesia Puhova
 *         Date: 25.02.14
 *         Time: 13:17
 */
public abstract class RangeValidator<T extends Comparable> extends AbstractValidator {
    private final T rangeStart;
    private final T rangeEnd;

    public RangeValidator(Constraint constraint) {
        this.rangeStart = convert(constraint.param(Constraint.PARAM_RANGE_START));
        this.rangeEnd = convert(constraint.param(Constraint.PARAM_RANGE_END));

    }

    @Override
    void doValidation(CanBeValidated canBeValidated, ValidationResult validationResult) {
        Comparable value = convert((String) canBeValidated.getValue());
        if (value != null) {
            if (rangeStart != null && rangeStart.compareTo(value) > 0) {
                validationResult.addError("validate.range.too-small");
            }
            if (rangeEnd != null && rangeEnd.compareTo(value) < 0) {
                validationResult.addError("validate.range.too-big");
            }
        }
    }

     abstract T convert(String s);

    @Override
    public String toString() {
        return "Client range validator: "
                + rangeStart != null ? "rangeStart = " + rangeStart : ""
                + rangeEnd != null ? "rangeEnd = " + rangeEnd : "";
    }
}
