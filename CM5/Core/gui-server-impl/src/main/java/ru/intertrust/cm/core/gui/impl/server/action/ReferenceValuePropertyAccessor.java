package ru.intertrust.cm.core.gui.impl.server.action;

import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;

/**
 * @author Sergey.Okolot
 *         Created on 19.09.2014 11:53.
 */
public class ReferenceValuePropertyAccessor implements PropertyAccessor {

    private final CrudService crudService;

    public ReferenceValuePropertyAccessor(CrudService crudService) {
        this.crudService = crudService;
    }

    @Override
    public Class[] getSpecificTargetClasses() {
        return new Class[] {ReferenceValue.class};
    }

    @Override
    public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
        final ReferenceValue referenceValue = (ReferenceValue) target;
        final DomainObject dobj = crudService.find(referenceValue.get());
        return dobj.getFields().contains(name);
    }

    @Override
    public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
        final ReferenceValue referenceValue = (ReferenceValue) target;
        final DomainObject dobj = crudService.find(referenceValue.get());
        return new TypedValue(dobj.getValue(name));
    }

    @Override
    public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
        return false;
    }

    @Override
    public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
    }
}
