package ru.intertrust.cm.core.gui.impl.server.validation.validators;

import ru.intertrust.cm.core.business.api.dto.Constraint;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.gui.model.validation.ValidationResult;

/**
 * @author Lesia Puhova
 *         Date: 06.03.14
 *         Time: 15:30
 */
public abstract class RangeValidator<T extends Comparable> implements ServerValidator{

    private final T rangeStart;
    private final T rangeEnd;

    public RangeValidator(Constraint constraint) {
        this.rangeStart = convert(constraint.param(Constraint.PARAM_RANGE_START));
        this.rangeEnd = convert(constraint.param(Constraint.PARAM_RANGE_END));
    }

    @Override
    public ValidationResult validate(Dto dtoToValidate) {
        ValidationResult validationResult = new ValidationResult();
        if (dtoToValidate instanceof Value) {
            Value value = (Value)dtoToValidate;
            if (value.get() != null) {
                T valueToValidate = (T)value.get();
                if (rangeStart != null && rangeStart.compareTo(valueToValidate) > 0) {
                    validationResult.addError("validate.range.too-small");
                }
                if (rangeEnd != null && rangeEnd.compareTo(valueToValidate) < 0) {
                    validationResult.addError("validate.range.too-big");
                }
            }
        }
        return validationResult;
    }

    abstract T convert(String s);

    @Override
    public String toString() {
        return "Server range validator: "
                + rangeStart != null ? "rangeStart = " + rangeStart : ""
                + rangeEnd != null ? "rangeEnd = " + rangeEnd : "";
    }
}
