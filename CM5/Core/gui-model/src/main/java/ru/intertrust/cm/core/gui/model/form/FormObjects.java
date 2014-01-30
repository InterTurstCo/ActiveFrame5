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

    public SingleObjectNode getRootNode() {
        return (SingleObjectNode) fieldPathNodes.get(FieldPath.ROOT);
    }

    public void setFieldValue(FieldPath fieldPath, Value value) {
        ((SingleObjectNode) getParentNode(fieldPath)).getDomainObject().setValue(fieldPath.getFieldName(), value);
    }

    public Value getFieldValue(FieldPath fieldPath) {
        DomainObject domainObject = ((SingleObjectNode) getParentNode(fieldPath)).getDomainObject();
        return domainObject == null ? null : domainObject.getValue(fieldPath.getFieldName());
    }

    public ArrayList<Id> getObjectIds(FieldPath fieldPath) {
        boolean isField = fieldPath.isField();
        boolean isOneToOneReference = fieldPath.isOneToOneReference();
        if (isField || isOneToOneReference) {
            String fieldName = isField ? fieldPath.getFieldName() : fieldPath.getOneToOneReferenceName();
            SingleObjectNode fieldPathNode = (SingleObjectNode) getParentNode(fieldPath);
            ArrayList<Id> result = new ArrayList<Id>(1);
            final DomainObject domainObject = fieldPathNode.getDomainObject();
            if (domainObject != null) {
                Id referenceId = domainObject.getReference(fieldName);
                if (referenceId != null) {
                    result.add(referenceId);
                }
            }
            return result;
        }

        MultiObjectNode fieldPathNode = (MultiObjectNode) fieldPathNodes.get(fieldPath);
        if (fieldPathNode == null) {
            return new ArrayList<Id>(0);
        }
        if (fieldPath.isOneToManyReference()) {
            ArrayList<Id> result = new ArrayList<Id>(fieldPathNode.size());
            for (DomainObject domainObject : fieldPathNode) {
                result.add(domainObject.getId());
            }
            return result;
        }

        // it's many-to-many
        String linkToChildrenName = fieldPath.getLinkToChildrenName();
        ArrayList<Id> result = new ArrayList<Id>(fieldPathNode.size());
        for (DomainObject domainObject : fieldPathNode) {
            result.add(domainObject.getReference(linkToChildrenName));
        }
        return result;
    }
}
