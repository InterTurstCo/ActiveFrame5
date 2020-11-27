package ru.intertrust.cm.core.gui.impl.server.widget.report;

import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.gui.api.server.widget.FormDefaultValueSetter;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.FormObjects;
import ru.intertrust.cm.core.gui.model.form.FormState;

@ComponentName("report.template.form.setter")
public class ReportTemplateFormDefaultValueSetter implements FormDefaultValueSetter {

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
        if ("type".equals(fieldPath.getPath())) {
            return new StringValue("Печатная форма");
        } else if ("constructor".equals(fieldPath.getPath())) {
            return new StringValue("Файл docx");
        }
        return null;
    }
}
