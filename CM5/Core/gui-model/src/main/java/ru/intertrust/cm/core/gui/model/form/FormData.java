package ru.intertrust.cm.core.gui.model.form;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Value;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Denis Mitavskiy
 *         Date: 14.09.13
 *         Time: 18:09
 */
public class FormData implements Dto {
    private Map<FieldPath, DomainObject> fieldPathObjects;

    public FormData() {
        fieldPathObjects = new HashMap<FieldPath, DomainObject>();
    }

    public Map<FieldPath, DomainObject> getFieldPathObjects() {
        return fieldPathObjects;
    }

    public void setFieldPathObjects(Map<FieldPath, DomainObject> fieldPathObjects) {
        this.fieldPathObjects = fieldPathObjects;
    }

    public void setFieldPathObject(FieldPath fieldPath, DomainObject object) {
        fieldPathObjects.put(fieldPath, object);
    }

    public DomainObject getFieldPathObject(FieldPath fieldPath) {
        return fieldPathObjects.get(fieldPath);
    }

    public void setFieldPathValue(FieldPath fieldPath, Value value) {
        FieldPath objectPath = fieldPath.createFieldPathWithoutLastElement();
        if (objectPath.size() == 0) {
            objectPath = null;
        }
        fieldPathObjects.get(objectPath).setValue(fieldPath.getLastElement(), value);
    }

    public <T extends Value> T getFieldPathValue(FieldPath fieldPath) {
        FieldPath objectPath = fieldPath.createFieldPathWithoutLastElement();
        if (objectPath.size() == 0) {
            objectPath = null;
        }
        DomainObject fieldPathObject = fieldPathObjects.get(objectPath);
        return fieldPathObject == null ? null : (T) fieldPathObject.getValue(fieldPath.getLastElement());
    }
}
