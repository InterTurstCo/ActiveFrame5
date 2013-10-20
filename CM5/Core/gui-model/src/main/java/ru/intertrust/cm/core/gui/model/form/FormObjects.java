package ru.intertrust.cm.core.gui.model.form;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
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
    private Map<FieldPath, ObjectsNode> fieldPathNodes;

    public FormObjects() {
        fieldPathNodes = new HashMap<FieldPath, ObjectsNode>();
    }

    public boolean isObjectsSet(FieldPath fieldPath) {
        return fieldPathNodes.containsKey(fieldPath);
    }

    public void setObjects(FieldPath fieldPath, ObjectsNode domainObjects) {
        this.fieldPathNodes.put(fieldPath, domainObjects);
    }

    public ObjectsNode getObjects(FieldPath fieldPath) {
        return fieldPathNodes.get(fieldPath);
    }

    public void setRootObjects(ObjectsNode object) {
        this.fieldPathNodes.put(FieldPath.ROOT, object);
    }

    public ObjectsNode getRootObjects() {
        return fieldPathNodes.get(FieldPath.ROOT);
    }

    public void setObjectValues(FieldPath fieldPath, ArrayList<Value> value) {
        ArrayList<DomainObject> fieldPathObjects = getFieldNode(fieldPath).getDomainObjects();
        for (int i = 0; i < fieldPathObjects.size(); ++i) {
            DomainObject fieldPathObject = fieldPathObjects.get(i);
            fieldPathObject.setValue(fieldPath.getLastElement(), value.get(i));
        }
    }

    public <T extends Value> ArrayList<T> getObjectValues(FieldPath fieldPath) {
        ArrayList<DomainObject> fieldPathObject = getFieldNode(fieldPath).getDomainObjects();
        if (fieldPathObject == null) {
            return null;
        }
        ArrayList<T> result = new ArrayList<T>(fieldPathObject.size());
        for (DomainObject domainObject : fieldPathObject) {
            result.add((T) domainObject.getValue(fieldPath.getLastElement()));
        }
        return result;
    }

    public ArrayList<Id> getObjectIds(FieldPath fieldPath) {
        ObjectsNode fieldPathNode = fieldPathNodes.get(fieldPath);
        if (fieldPathNode != null) { // node is a list of objects. it's values are Ids
            ArrayList<Id> result = new ArrayList<Id>(fieldPathNode.size());
            for (DomainObject domainObject : fieldPathNode) {
                result.add(domainObject.getId());
            }
            return result;
        }
        FieldPath parentPath = fieldPath.getParent();
        fieldPathNode = fieldPathNodes.get(parentPath);
        if (fieldPathNode == null) {
            return null;
        }
        ArrayList<Id> result = new ArrayList<Id>(fieldPathNode.size());
        for (DomainObject domainObject : fieldPathNode) {
            result.add(domainObject.getReference(fieldPath.getLastElement()));
        }
        return result;
    }

    private ObjectsNode getFieldNode(FieldPath fieldPath) {
        FieldPath objectPath = fieldPath.getParent();
        return fieldPathNodes.get(objectPath);
    }
}
