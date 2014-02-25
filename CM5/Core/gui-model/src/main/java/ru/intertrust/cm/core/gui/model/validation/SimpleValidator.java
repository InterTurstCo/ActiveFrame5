package ru.intertrust.cm.core.gui.model.validation;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lesia Puhova
 *         Date: 17.02.14
 *         Time: 15:28
 */
public class SimpleValidator implements Validator {

    private final String constraint;

    public SimpleValidator(String constraint) {
        this.constraint = constraint;
    }

    private static final Map<String, String> wordsToPatterns = new HashMap<String, String>();

    static {
        wordsToPatterns.put("validate.not-empty", "^[^\\s]+$");
        wordsToPatterns.put("validate.integer", "^([-+]?\\d+)?$");
        wordsToPatterns.put("validate.positive-int", "^([+]?\\d+)?$");
        wordsToPatterns.put("validate.decimal", "^([-+]?[0-9]+(\\.[0-9]+)?)?$");
        wordsToPatterns.put("validate.positive-dec", "^([+]?[0-9]+(\\.[0-9]+)?)?$");
    }

    @Override
    public ValidationResult validate(CanBeValidated canBeValidated, Object info) {
        ValidationResult validationResult = new ValidationResult();
        if (canBeValidated != null) {
            String pattern = wordsToPatterns.get(constraint);
            String messageKey = constraint;
            if (pattern == null) {
                pattern = constraint;
                messageKey = "validate.pattern";
            }
            String value = (String) canBeValidated.getValue();
            if (!value.matches(pattern)) {
                validationResult.addError(messageKey);
            }
        }
        if (!validationResult.isEmpty()) {
            canBeValidated.showErrors(validationResult);
        }
        return validationResult;
    }

    @Override
    public String toString() {
        return constraint;
    }
}
