package ru.intertrust.cm.core.gui.model.validation;

import ru.intertrust.cm.core.business.api.dto.Constraint;

/**
 * Валидатор диапазона значений.
 * @author Lesia Puhova
 *         Date: 25.02.14
 *         Time: 13:17
 */
public abstract class RangeValidator<T extends Comparable> extends AbstractValidator {
    private final String rangeStartStr;
    private final String rangeEndStr;

    public RangeValidator(Constraint constraint) {
        this(constraint, Constraint.PARAM_RANGE_START, Constraint.PARAM_RANGE_END);
    }

    public RangeValidator(Constraint constraint, String paramNameStart, String paramNameEnd) {
        rangeStartStr = constraint.param(paramNameStart);
        rangeEndStr = constraint.param(paramNameEnd);
    }

    @Override
    void doValidation(CanBeValidated canBeValidated, ValidationResult validationResult) {
        Comparable value = convert((String) canBeValidated.getValue());
        T rangeStart = convert(rangeStartStr);
        T rangeEnd = convert(rangeEndStr);
        if (value != null) {
            if (rangeStart != null && rangeStart.compareTo(value) > 0) {
                validationResult.addError("validate.range.too-small");
            }
            if (rangeEnd != null && rangeEnd.compareTo(value) < 0) {
                validationResult.addError("validate.range.too-big");
            }
        }
    }

    abstract protected T convert(String s);

    @Override
    public String toString() {
        return "Client range validator: "
                + (rangeStartStr != null ? "rangeStart = " + rangeStartStr : "")
                + (rangeEndStr != null ? "rangeEnd = " + rangeEndStr : "");
    }
}
