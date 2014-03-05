package ru.intertrust.cm.core.gui.impl.server.validation.validators;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.model.validation.ValidationResult;

/**
 * @author Lesia Puhova
 *         Date: 05.03.14
 *         Time: 12:27
 */
public interface ServerValidator {
    ValidationResult validate (Dto dtoToValidate);
}
