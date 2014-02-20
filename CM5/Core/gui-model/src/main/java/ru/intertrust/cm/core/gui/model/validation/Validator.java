package ru.intertrust.cm.core.gui.model.validation;

/**
 * @author Lesia Puhova
 *         Date: 17.02.14
 *         Time: 14:15
 */
public interface Validator {
    ValidationResult validate(CanBeValidated canBeValidated, Object info);
}
