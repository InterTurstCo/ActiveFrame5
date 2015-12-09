package ru.intertrust.cm.core.gui.impl.server.form;

import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.api.server.widget.FormDefaultValueSetter;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.FormObjects;
import ru.intertrust.cm.core.gui.model.form.FormState;

/**
 * @author Denis Mitavskiy
 */
@ComponentName("temp.org.value.setter")
public class TempOrganizationValueSetter implements FormDefaultValueSetter, ComponentHandler {
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
        if (fieldPath.getPath().equals("status")) {
            return new ReferenceValue(new RdbmsId(31, 9));
        }
        return null;
    }
}
