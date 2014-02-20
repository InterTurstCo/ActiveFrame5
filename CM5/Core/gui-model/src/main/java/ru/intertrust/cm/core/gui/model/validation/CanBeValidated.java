package ru.intertrust.cm.core.gui.model.validation;

import java.util.Collection;

/**
 * @author Lesia Puhova
 *         Date: 17.02.14
 *         Time: 14:14
 */
public interface CanBeValidated {
    Object getValue();
    Collection<Validator> getValidators();
}
