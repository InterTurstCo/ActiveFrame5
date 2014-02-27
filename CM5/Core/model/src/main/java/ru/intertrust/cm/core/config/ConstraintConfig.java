package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.business.api.dto.Constraint;

import java.io.Serializable;

/**
 * @author Lesia Puhova
 *         Date: 25.02.14
 *         Time: 18:15
 */
public abstract class ConstraintConfig implements Serializable {

    abstract public Constraint getConstraint();
}
