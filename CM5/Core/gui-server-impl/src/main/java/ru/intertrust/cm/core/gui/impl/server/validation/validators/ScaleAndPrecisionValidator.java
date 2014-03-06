package ru.intertrust.cm.core.gui.impl.server.validation.validators;

import ru.intertrust.cm.core.business.api.dto.Constraint;
import ru.intertrust.cm.core.business.api.dto.DecimalValue;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.model.validation.ValidationResult;

import java.math.BigDecimal;

/**
 * @author Lesia Puhova
 *         Date: 06.03.14
 *         Time: 14:25
 */
public class ScaleAndPrecisionValidator implements ServerValidator {

    private static final String DECIMAL_POINT = ".";
    private final Integer precision; // total number of digits, for instance 12.34567 - precision is 7, scale is 5
    private final Integer scale;

    public ScaleAndPrecisionValidator(Constraint constraint) {
        String scaleStr = constraint.param(Constraint.PARAM_SCALE);
        String precisionStr = constraint.param(Constraint.PARAM_PRECISION);
        this.scale = parseInt(scaleStr);
        this.precision = parseInt(precisionStr);
    }

    @Override
    public ValidationResult validate(Dto dtoToValidate) {
        ValidationResult validationResult = new ValidationResult();
        if (dtoToValidate instanceof DecimalValue) {
            BigDecimal decimalValue = ((DecimalValue)dtoToValidate).get();
            if (decimalValue != null) {
                if (precision != null) {
                    int actualPrecision = decimalValue.precision();
                    if (actualPrecision > precision) {
                        validationResult.addError("validate.precision");
                    }
                }
                if (scale != null) {
                    int actualScale = decimalValue.scale();
                    if (actualScale > scale) {
                        validationResult.addError("validate.scale");
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
    public String toString() {
        return "Server scale & precision validator: "
                + precision != null ? "precision = " + precision : ""
                + scale != null ? "scale = " + scale : "";
    }
}
