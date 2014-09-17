package ru.intertrust.cm.core.gui.impl.server.action;

import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * @author Sergey.Okolot
 *         Created on 16.09.2014 16:21.
 */
public class DomainObjectPropertyAccessor implements PropertyAccessor {
    private static final String CURRENT_USER = "current_user";

    private final Id currentUserId;

    public DomainObjectPropertyAccessor(final Id currentUserId) {
        this.currentUserId = currentUserId;
    }

    @Override
    public Class[] getSpecificTargetClasses() {
        return new Class[] {DomainObject.class};
    }

    @Override
    public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
        if (CURRENT_USER.equals(name)) {
            return true;
        } else {
            final DomainObject dObj = (DomainObject) target;
            return dObj.getFields().contains(name);
        }
    }

    @Override
    public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
        final Object value;
        if (CURRENT_USER.equals(name)) {
            value = currentUserId;
        } else {
            final DomainObject dObj = (DomainObject) target;
            value = dObj.getValue(name);
        }
        return new TypedValue(value);
    }

    @Override
    public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
        return false;
    }

    @Override
    public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
    }
}
