package ru.intertrust.cm.core.gui.model.validation;

/**
 * @author Lesia Puhova
 *         Date: 25.02.14
 *         Time: 13:35
 */
public class ScaleAndPrecisionValidator extends AbstractValidator {
    private final Integer precision; // total number of digits, for instance 12.34567 - precision is 7, scale is 5
    private final Integer scale;

    public ScaleAndPrecisionValidator(Integer scale, Integer precision) {
        this.scale = scale;
        this.precision = precision;
    }

    @Override
    void doValidation(CanBeValidated canBeValidated, ValidationResult validationResult) {
        String value = (String) canBeValidated.getValue();
        if (precision != null) {
            int actualPrecision = value.indexOf(".") != 0 ? value.length() - 1 : value.length();
            if (actualPrecision > precision) {
                validationResult.addError("validate.precision");
            }
        }
        if (scale != null && value.indexOf(".") != 0) {
            int actualScale = value.length() - value.indexOf(".") - 1;
            if (actualScale > scale) {
                validationResult.addError("validate.scale");
            }
        }
    }
}

