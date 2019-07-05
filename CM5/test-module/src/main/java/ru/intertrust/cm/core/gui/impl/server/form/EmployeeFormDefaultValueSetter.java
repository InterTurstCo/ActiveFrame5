package ru.intertrust.cm.core.gui.impl.server.form;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.gui.api.server.widget.FormDefaultValueSetter;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.FormObjects;
import ru.intertrust.cm.core.gui.model.form.FormState;

/**
 * Created by Myskin Sergey on 03.07.2019.
 */
@ComponentName("employee.form.default.value.setter")
public class EmployeeFormDefaultValueSetter implements FormDefaultValueSetter {

    @Autowired
    private CrudService crudService;

    @Override
    public Value getDefaultValue(FormState formState, FieldPath fieldPath) {
        final String path = fieldPath.getPath();
        if (path.equalsIgnoreCase("Department")) {

            final Id lastCollectionRowSelectedId = formState.getLastCollectionRowSelectedId();
            if (lastCollectionRowSelectedId != null) {

                final String domainObjectType = crudService.getDomainObjectType(lastCollectionRowSelectedId);
                if (domainObjectType.equalsIgnoreCase("Department")) {

                    final DomainObject departmentDo = crudService.find(lastCollectionRowSelectedId);
                    final Id classifierTypeDoId = departmentDo.getId();

                    final ReferenceValue departmentRefId = new ReferenceValue(classifierTypeDoId);
                    return departmentRefId;
                } else if (domainObjectType.equalsIgnoreCase("Employee")) {

                    final DomainObject employeeDo = crudService.find(lastCollectionRowSelectedId);
                    final Id departmentRefId = employeeDo.getReference("Department");

                    final ReferenceValue departmentRefValue = new ReferenceValue(departmentRefId);
                    return departmentRefValue;
                }
            }
        }
        return null;
    }

    @Override
    public Value[] getDefaultValues(FormObjects formObjects, FieldPath fieldPath) {
        return new Value[0];
    }

    @Override
    public Value getDefaultValue(FormObjects formObjects, FieldPath fieldPath) {
        return null;
    }

    @Override
    public Value[] getDefaultValues(FormState formState, FieldPath fieldPath) {
        return new Value[0];
    }


}
