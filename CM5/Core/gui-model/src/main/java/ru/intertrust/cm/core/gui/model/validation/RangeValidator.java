package ru.intertrust.cm.core.gui.model.validation;

import ru.intertrust.cm.core.business.api.dto.Constraint;

import java.math.BigDecimal;

/**
 * Валидатор диапазона значений.
 * @author Lesia Puhova
 *         Date: 25.02.14
 *         Time: 13:17
 */
public class RangeValidator extends AbstractValidator {
    private final Comparable rangeStart;
    private final Comparable rangeEnd;
    private final String fieldType;

    public RangeValidator(Constraint constraint) {
        this.fieldType = constraint.param(Constraint.PARAM_FIELD_TYPE);
        this.rangeStart = convert(constraint.param(Constraint.PARAM_RANGE_START), fieldType);
        this.rangeEnd = convert(constraint.param(Constraint.PARAM_RANGE_END), fieldType);

    }

    @Override
    void doValidation(CanBeValidated canBeValidated, ValidationResult validationResult) {
        Comparable value = convert((String) canBeValidated.getValue(), fieldType);
        if (value != null) {
            if (rangeStart != null && rangeStart.compareTo(value) > 0) {
                validationResult.addError("validate.range.too-small");
            }
            if (rangeEnd != null && rangeEnd.compareTo(value) < 0) {
                validationResult.addError("validate.range.too-big");
            }
        }
    }

    private static Comparable convert(String s, String type) {
        if (s == null) {
            return null;
        }
        if (Constraint.TYPE_LONG.equals(type)) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException nfe) {
                return null;
            }
        }
        if (Constraint.TYPE_DECIMAL.equals(type)) {
            try {
                return new BigDecimal(s);
            } catch (NumberFormatException nfe) {
                return null;
            }
        } else if (Constraint.TYPE_DATE.equals(type)) {
            //TODO: [validation]
        }
        return s;
    }

}
