package ru.intertrust.cm.core.gui.model.validation;

/**
 * @author Lesia Puhova
 *         Date: 25.02.14
 *         Time: 13:17
 */
public class RangeValidator<T> extends AbstractValidator {
    private final Comparable<T> rangeStart;
    private final Comparable<T> rangeEnd;
    private final Converter<T> converter;

    public RangeValidator(Comparable<T> rangeStart, Comparable<T> rangeEnd, Converter<T> converter) {
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
        this.converter = converter;
    }

    @Override
    void doValidation(CanBeValidated canBeValidated, ValidationResult validationResult) {
        T value = converter.convert((String)canBeValidated.getValue());
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
