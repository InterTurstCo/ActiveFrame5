package ru.intertrust.cm.core.gui.impl.server.form;

import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.gui.api.server.widget.FormDefaultValueSetter;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.FormObjects;
import ru.intertrust.cm.core.gui.model.form.FormState;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 08.04.2016
 * Time: 10:33
 * To change this template use File | Settings | File and Code Templates.
 */
@ComponentName("department.form.value.setter")
public class DepartmentFormValueSetter implements FormDefaultValueSetter {
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

    @Override
    public Value getDefaultValue(FormState formState, FieldPath fieldPath) {
        return null;
    }
}
