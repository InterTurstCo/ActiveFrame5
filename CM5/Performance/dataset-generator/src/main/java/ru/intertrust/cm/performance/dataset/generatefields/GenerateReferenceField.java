package ru.intertrust.cm.performance.dataset.generatefields;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.performance.dataset.xmltypes.ReferenceType;
import ru.intertrust.cm.performance.dataset.xmltypes.TemplateType;

public class GenerateReferenceField {
    protected String field;
    protected Id value;
    protected boolean ignore;

    public String getField() {
        return field;
    }

    public Id getValue() {
        return value;
    }

    public boolean ignoreField() {
        return ignore;
    }

    public void generateField(ReferenceType referenceType, List<TemplateType> tamplateList) {
        if (referenceType.getName() != null) {
            field = referenceType.getName();
        } else {
            field = "null";
        }
        ignore = true;
        // value = new Date();
    }
}
