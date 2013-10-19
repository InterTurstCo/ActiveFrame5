package ru.intertrust.cm.core.gui.model.form;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Denis Mitavskiy
 *         Date: 14.09.13
 *         Time: 18:09
 */
public class FormObjects implements Dto {
    private Map<FieldPath, ObjectsNode> fieldPathObjectLists;

    public FormObjects() {
        fieldPathObjectLists = new HashMap<FieldPath, ObjectsNode>();
    }

    public boolean isObjectsSet(FieldPath fieldPath) {
        return fieldPathObjectLists.containsKey(fieldPath);
    }

    public void setObjects(FieldPath fieldPath, ObjectsNode domainObjects) {
        this.fieldPathObjectLists.put(fieldPath, domainObjects);
    }

    public ObjectsNode getObjects(FieldPath fieldPath) {
        return fieldPathObjectLists.get(fieldPath);
    }

    public void setRootObjects(ObjectsNode object) {
        this.fieldPathObjectLists.put(FieldPath.ROOT, object);
    }

    public ObjectsNode getRootObjects() {
        return fieldPathObjectLists.get(FieldPath.ROOT);
    }

    public void setObjectValues(FieldPath fieldPath, ArrayList<Value> value) {
        ArrayList<DomainObject> fieldPathObjects = getObjectsContainingFieldPathValues(fieldPath).getDomainObjects();
        for (int i = 0; i < fieldPathObjects.size(); ++i) {
            DomainObject fieldPathObject = fieldPathObjects.get(i);
            fieldPathObject.setValue(fieldPath.getLastElement(), value.get(i));
        }
    }

    public <T extends Value> ArrayList<T> getObjectValues(FieldPath fieldPath) {
        ArrayList<DomainObject> fieldPathObject = getObjectsContainingFieldPathValues(fieldPath).getDomainObjects();
        if (fieldPathObject == null) {
            return null;
        }
        ArrayList<T> result = new ArrayList<T>(fieldPathObject.size());
        for (DomainObject domainObject : fieldPathObject) {
            result.add((T) domainObject.getValue(fieldPath.getLastElement()));
        }
        return result;
    }

    private ObjectsNode getObjectsContainingFieldPathValues(FieldPath fieldPath) {
        FieldPath objectPath = fieldPath.getParent();
        return fieldPathObjectLists.get(objectPath);
    }
}
