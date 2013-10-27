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

    public boolean containsNode(FieldPath fieldPath) {
        return fieldPathNodes.containsKey(fieldPath);
    }

    public void setNode(FieldPath fieldPath, ObjectsNode node) {
        this.fieldPathNodes.put(fieldPath, node);
    }

    public ObjectsNode getNode(FieldPath fieldPath) {
        return fieldPathNodes.get(fieldPath);
    }

    public ObjectsNode getParentNode(FieldPath fieldPath) {
        FieldPath objectPath = fieldPath.getParentPath();
        return fieldPathNodes.get(objectPath);
    }

    public void setRootNode(ObjectsNode node) {
        this.fieldPathNodes.put(FieldPath.ROOT, node);
    }

    public ObjectsNode getRootNode() {
        return fieldPathNodes.get(FieldPath.ROOT);
    }

    public void setFieldValue(FieldPath fieldPath, Value value) {
        getParentNode(fieldPath).getObject().setValue(fieldPath.getFieldName(), value);
    }

    public Value getFieldValue(FieldPath fieldPath) {
        return getParentNode(fieldPath).getObject().getValue(fieldPath.getFieldName());
    }

    public ArrayList<Id> getObjectIds(FieldPath fieldPath) {
        if (fieldPath.isField()) {
            ObjectsNode fieldPathNode = getParentNode(fieldPath);
            String fieldName = fieldPath.getFieldName();
            ArrayList<Id> result = new ArrayList<Id>(fieldPathNode.size());
            for (DomainObject domainObject : fieldPathNode) {
                result.add(domainObject.getReference(fieldName));
            }
            return result;
        }

        ObjectsNode fieldPathNode = fieldPathNodes.get(fieldPath);
        if (fieldPath.isOneToManyReference()) {
            ArrayList<Id> result = new ArrayList<Id>(fieldPathNode.size());
            for (DomainObject domainObject : fieldPathNode) {
                result.add(domainObject.getId());
            }
            return result;
        }
        if (fieldPath.isManyToManyReference()) {
            String linkToChildrenName = fieldPath.getLinkToChildrenName();
            ArrayList<Id> result = new ArrayList<Id>(fieldPathNode.size());
            for (DomainObject domainObject : fieldPathNode) {
                result.add(domainObject.getReference(linkToChildrenName));
            }
            return result;
        }
        // it's a one to one reference
        String fieldName = fieldPath.getOneToOneReferenceName();
        ArrayList<Id> result = new ArrayList<Id>(fieldPathNode.size());
        for (DomainObject domainObject : fieldPathNode) {
            result.add(domainObject.getReference(fieldName));
        }
        return result;
    }
}
