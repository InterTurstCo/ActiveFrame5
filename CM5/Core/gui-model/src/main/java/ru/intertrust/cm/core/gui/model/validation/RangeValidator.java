package ru.intertrust.cm.core.gui.model.validation;

import ru.intertrust.cm.core.business.api.dto.Constraint;

/**
 * Валидатор диапазона значений.
 * @author Lesia Puhova
 *         Date: 25.02.14
 *         Time: 13:17
 */
public class RangeValidator<T extends Comparable> extends AbstractValidator {
    private final String rangeStartStr;
    private final String rangeEndStr;
    private final Converter<T> converter;

    public RangeValidator(Constraint constraint, Converter<T> converter) {
        this.rangeStartStr = constraint.param(Constraint.PARAM_RANGE_START);
        this.rangeEndStr = constraint.param(Constraint.PARAM_RANGE_END);
        this.converter = converter;
    }

    @Override
    void doValidation(CanBeValidated canBeValidated, ValidationResult validationResult) {
        T value = converter.convert((String)canBeValidated.getValue());
        T rangeStart = converter.convert(rangeStartStr);
        T rangeEnd = converter.convert(rangeEndStr);
        if (value != null) {
            if (rangeStart != null && rangeStart.compareTo(value) > 0) {
                validationResult.addError("validate.range.too-small");
            }
            if (rangeEnd != null && rangeEnd.compareTo(value) < 0) {
                validationResult.addError("validate.range.too-big");
            }
        }
    }

    public static interface Converter<T> {
        public T convert(String s);
    }
}
