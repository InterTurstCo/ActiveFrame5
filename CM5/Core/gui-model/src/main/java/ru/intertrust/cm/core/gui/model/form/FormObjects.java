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
public class FormObjects implements Dto {
    private Map<FieldPath, DomainObject> fieldPathObjects;

    public FormObjects() {
        fieldPathObjects = new HashMap<FieldPath, DomainObject>();
    }

    public Map<FieldPath, DomainObject> getFieldPathObjects() {
        return fieldPathObjects;
    }

    public boolean isObjectSet(FieldPath fieldPath) {
        return fieldPathObjects.containsKey(fieldPath);
    }

    public void setObject(FieldPath fieldPath, DomainObject object) {
        this.fieldPathObjects.put(fieldPath, object);
    }

    public DomainObject getObject(FieldPath fieldPath) {
        return fieldPathObjects.get(fieldPath);
    }

    public void setRootObject(DomainObject object) {
        this.fieldPathObjects.put(FieldPath.ROOT, object);
    }

    public DomainObject getRootObject() {
        return fieldPathObjects.get(FieldPath.ROOT);
    }

    public DomainObject setObjectValue(FieldPath fieldPath, Value value) {
        DomainObject fieldPathObject = getObjectContainingFieldPathValue(fieldPath);
        fieldPathObject.setValue(fieldPath.getLastElement(), value);
        return fieldPathObject;
    }

    public <T extends Value> T getObjectValue(FieldPath fieldPath) {
        DomainObject fieldPathObject = getObjectContainingFieldPathValue(fieldPath);
        return fieldPathObject == null ? null : (T) fieldPathObject.getValue(fieldPath.getLastElement());
    }

    private DomainObject getObjectContainingFieldPathValue(FieldPath fieldPath) {
        FieldPath objectPath = fieldPath.createFieldPathWithoutLastElement();
        return fieldPathObjects.get(objectPath);
    }
}
