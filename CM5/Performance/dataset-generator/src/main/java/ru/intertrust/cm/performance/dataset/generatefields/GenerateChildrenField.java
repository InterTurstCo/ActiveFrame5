package ru.intertrust.cm.performance.dataset.generatefields;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.performance.dataset.xmltypes.ChildrenType;
import ru.intertrust.cm.performance.dataset.xmltypes.TemplateType;

public class GenerateChildrenField {
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

    public void generateField(ChildrenType childrenType, List<TemplateType> tamplateList) {

        field = "Children";

        ignore = true;
        // value = new Date();
    }
}
