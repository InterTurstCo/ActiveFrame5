package ru.intertrust.cm.core.gui.impl.server.form.defaults;

import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.FormObjects;

/**
 * Created by andrey on 17.09.14.
 */
public interface FormDefaultValueSetter {
    Value[] getDefaultValues(FormObjects formObjects, FieldPath fieldPath);

    Value getDefaultValue(FormObjects formObjects, FieldPath fieldPath);




}
