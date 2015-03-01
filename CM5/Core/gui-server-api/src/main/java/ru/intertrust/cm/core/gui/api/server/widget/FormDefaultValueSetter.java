package ru.intertrust.cm.core.gui.api.server.widget;

import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.FormObjects;
import ru.intertrust.cm.core.gui.model.form.FormState;

/**
 * Created by andrey on 17.09.14.
 */
public interface FormDefaultValueSetter {
    @Deprecated //use  Value[] getDefaultValues(FormState formState, FieldPath fieldPath) instead
    Value[] getDefaultValues(FormObjects formObjects, FieldPath fieldPath);
    @Deprecated //use Value[] getDefaultValues(FormState formState, FieldPath fieldPath) instead
    Value getDefaultValue(FormObjects formObjects, FieldPath fieldPath);

    Value[] getDefaultValues(FormState formState, FieldPath fieldPath);

    Value getDefaultValue(FormState formState, FieldPath fieldPath);

}
