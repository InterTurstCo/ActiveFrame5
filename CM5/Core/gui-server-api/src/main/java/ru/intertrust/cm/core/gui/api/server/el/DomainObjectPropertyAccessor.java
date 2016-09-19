package ru.intertrust.cm.core.gui.api.server.el;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.StringValue;

/**
 * @author Sergey.Okolot
 *         Created on 16.09.2014 16:21.
 */
public class DomainObjectPropertyAccessor implements PropertyAccessor {
    private static final String CURRENT_USER = "current_user";
    private static final String ID = "id";
    private static final String STATUS = "status";

    private final Id currentUserId;

    private final CrudService crudService;

    public DomainObjectPropertyAccessor(final Id currentUserId, CrudService aCrudService) {
        this.currentUserId = currentUserId;
        crudService = aCrudService;
    }

    @Override
    public Class[] getSpecificTargetClasses() {
        return new Class[] {DomainObject.class};
    }

    @Override
    public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
        if (CURRENT_USER.equals(name) || ID.equals(name)) {
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
            if (ID.equals(name)) {
                value =  new StringValue(dObj.getId().toStringRepresentation());
            } else
            if(STATUS.equals(name)){
                value = crudService.find(dObj.getReference(STATUS)).getString("name");
            }
            else {
                value = dObj.getValue(name);
            }
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
