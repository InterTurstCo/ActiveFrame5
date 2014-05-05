package ru.intertrust.cm.core.gui.impl.server.form;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfigurationConfig;
import ru.intertrust.cm.core.gui.api.server.widget.LinkEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.model.form.*;
import ru.intertrust.cm.core.gui.model.form.widget.LinkEditingWidgetState;
import ru.intertrust.cm.core.gui.model.form.widget.LinkedDomainObjectsTableState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * @author Denis Mitavskiy
 *         Date: 21.10.13
 *         Time: 13:03
 */
public class FormSaver {
    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    private ConfigurationExplorer configurationExplorer;
    @Autowired
    private CrudService crudService;
    @Autowired
    private AttachmentService attachmentService;
    private FormState formState;
    private Map<FieldPath, Value> forcedRootDomainObjectValues;
    private FormObjects formObjects;
    private List<WidgetConfig> widgetConfigs;
    private HashMap<FieldPath, ObjectCreationOperation> toCreate;
    private HashMap<FieldPath, ObjectReferencesUpdateOperation> toUpdateExistingObjectsReferences;
    private ArrayList<FormSaveOperation> linkChangeOperations;
    private HashSet<FieldPath> toUpdate;
    private HashMap<Id, DomainObject> savedObjectsById;

    public FormSaver(FormState formState, Map<FieldPath, Value> forcedRootDomainObjectValues) {
        this.formState = formState;
        this.formObjects = formState.getObjects();
        this.forcedRootDomainObjectValues = forcedRootDomainObjectValues == null ? new HashMap<FieldPath, Value>(0) : forcedRootDomainObjectValues;
        toCreate = new HashMap<>();
        toUpdateExistingObjectsReferences = new HashMap<>();
        linkChangeOperations = new ArrayList<>();
        toUpdate = new HashSet<>();
        savedObjectsById = new HashMap<>();
    }

    @PostConstruct
    private void init() {
        FormConfig formConfig = configurationExplorer.getConfig(FormConfig.class, formState.getName());
        WidgetConfigurationConfig widgetConfigurationConfig = formConfig.getWidgetConfigurationConfig();
        widgetConfigs = widgetConfigurationConfig.getWidgetConfigList();
    }

    public DomainObject saveForm() {
        DomainObject rootDomainObject = formObjects.getRootNode().getDomainObject();
        if (rootDomainObject.isNew()) {
            toCreate.put(FieldPath.ROOT, new ObjectCreationOperation(FieldPath.ROOT, new HashMap<String, FieldPath>(0)));
        }
        for (WidgetConfig widgetConfig : widgetConfigs) {
            WidgetState widgetState = formState.getWidgetState(widgetConfig.getId());
            if (widgetState == null) { // ignore - such data shouldn't be saved
                continue;
            }
            if (widgetState instanceof LinkedDomainObjectsTableState) {
                //process nested form states
                LinkedDomainObjectsTableState linkedDomainObjectsTableState = (LinkedDomainObjectsTableState) widgetState;
                LinkedHashMap<String, FormState> nestedFormStates = linkedDomainObjectsTableState.getEditedFormStates();
                Set<Map.Entry<String, FormState>> entries = nestedFormStates.entrySet();
                for (Map.Entry<String, FormState> entry : entries) {
                    FormState nestedFormState = entry.getValue();
                    FormSaver formSaver = (FormSaver) applicationContext.getBean("formSaver", nestedFormState, null);
                    formSaver.saveForm();
                }
            }

            FieldPath[] fieldPaths = FieldPath.createPaths(widgetConfig.getFieldPathConfig().getValue());
            FieldPath firstFieldPath = fieldPaths[0];
            WidgetHandler handler = getWidgetHandler(widgetConfig);
            if (firstFieldPath.isBackReference()) {
                boolean deleteEntriesOnLinkDrop = ((LinkEditingWidgetHandler) handler).deleteEntriesOnLinkDrop(widgetConfig) || isParentReferencedByNotNullField(fieldPaths);
                HashMap<FieldPath, ArrayList<Id>> fieldPathsIds
                        = getBackReferenceFieldPathsIds(fieldPaths, (LinkEditingWidgetState) widgetState);
                for (FieldPath fieldPath : fieldPaths) {
                    linkChangeOperations.addAll(mergeObjectReferences(fieldPath, fieldPathsIds.get(fieldPath), deleteEntriesOnLinkDrop));
                }
                continue;
            }

            Value newValue = handler.getValue(widgetState);
            Value oldValue = formObjects.getFieldValue(firstFieldPath);
            if (!areValuesSemanticallyEqual(newValue, oldValue)) {
                FieldPath parentObjectPath = firstFieldPath.getParentPath();
                addNewNodeChainAndFillCreateAndReferencesUpdateOperations(parentObjectPath, null);
                toUpdate.add(parentObjectPath);
                formObjects.setFieldValue(firstFieldPath, newValue);
            }
        }

        for (FieldPath forcedValueFieldPath : forcedRootDomainObjectValues.keySet()) {
            formObjects.setFieldValue(forcedValueFieldPath, forcedRootDomainObjectValues.get(forcedValueFieldPath));
        }

        saveRootWithDirectlyLinkedObjects();

        rootDomainObject = formObjects.getRootNode().getDomainObject(); // after save its ID changed
        ReferenceValue rootObjectReference = new ReferenceValue(rootDomainObject.getId());

        for (FormSaveOperation operation : linkChangeOperations) {
            if (operation.type == FormSaveOperation.Type.Delete) {
                delete(operation);
            } else {
                operation.domainObject.setValue(operation.fieldToSetWithRootReference, rootObjectReference);
                save(operation.domainObject);
            }
        }
        saveNewLinkedObjects();
        return rootDomainObject;
    }

    private boolean isParentReferencedByNotNullField(FieldPath[] fieldPaths) {
        for (final FieldPath fieldPath : fieldPaths) {
            if (!fieldPath.isOneToManyReference()) {
                continue;
            }
            final String type = fieldPath.getReferenceType();
            final String fieldName = fieldPath.getLinkToParentName();
            final ReferenceFieldConfig fieldConfig = (ReferenceFieldConfig) configurationExplorer.getFieldConfig(type, fieldName);
            if (fieldConfig.isNotNull()) {
                return true;
            }
        }
        return false;
    }

    private void saveNewLinkedObjects() {
        for (WidgetConfig config : widgetConfigs) {
            WidgetState widgetState = formState.getWidgetState(config.getId());
            if (widgetState == null) { // ignore - such data shouldn't be saved
                continue;
            }
            WidgetHandler widgetHandler = getWidgetHandler(config);
            if (widgetHandler instanceof LinkEditingWidgetHandler) {
                WidgetContext widgetContext = new WidgetContext(config, formObjects);
                ((LinkEditingWidgetHandler) widgetHandler).saveNewObjects(widgetContext, widgetState);
            }
        }
    }

    private WidgetHandler getWidgetHandler(WidgetConfig config) {
        return (WidgetHandler) applicationContext.getBean(config.getComponentName());
    }

    private boolean areValuesSemanticallyEqual(Value newValue, Value oldValue) {
        boolean newValueEmpty = newValue == null || newValue.get() == null;
        boolean oldValueEmpty = oldValue == null || oldValue.get() == null;
        if (newValueEmpty && oldValueEmpty) {
            return true;
        }
        if (newValueEmpty || oldValueEmpty) { // something is NOT empty then (prev. condition)
            return false;
        }
        return newValue.equals(oldValue);
    }

    private void saveRootWithDirectlyLinkedObjects() {
        ArrayList<ObjectCreationOperation> creationOperations = new ArrayList<>(toCreate.values());
        creationOperations = ObjectCreationOperation.sortForSave(creationOperations);

        // 1) Save (update form object node (substitute) while saving )
        // 2) Merge reference updates of existing objects with regular updates
        // 3) update!
        for (ObjectCreationOperation operation : creationOperations) {
            final DomainObject domainObject = getSingleDomainObject(operation.path);
            for (String refFieldName : operation.refFieldObjectFieldPath.keySet()) {
                final DomainObject reference = getSingleDomainObject(operation.refFieldObjectFieldPath.get(refFieldName));
                domainObject.setReference(refFieldName, reference);
            }
            saveDomainObject(operation.path);
        }

        for (FieldPath fieldPath : toUpdateExistingObjectsReferences.keySet()) {
            final ObjectReferencesUpdateOperation operation = toUpdateExistingObjectsReferences.get(fieldPath);
            final DomainObject domainObject = getSingleDomainObject(operation.path);
            for (String refFieldName : operation.refFieldObjectFieldPath.keySet()) {
                final DomainObject reference = getSingleDomainObject(operation.refFieldObjectFieldPath.get(refFieldName));
                domainObject.setReference(refFieldName, reference);
            }
            toUpdate.remove(fieldPath);
        }
        toUpdate.removeAll(toCreate.keySet());

        for (FieldPath fieldPath : toUpdate) {
            saveDomainObject(fieldPath);
        }
    }

    private void saveDomainObject(FieldPath fieldPath) {
        final DomainObject domainObject = crudService.save(getSingleDomainObject(fieldPath));
        ((SingleObjectNode) formObjects.getNode(fieldPath)).setDomainObject(domainObject);
    }

    private DomainObject save(DomainObject object) {
        // this is required to avoid optimistic lock exceptions when same object is being edited by several widgets,
        // for example, one widget is editing object's properties while the other edits links
        final Id id = object.getId();
        DomainObject earlierSavedObject = savedObjectsById.get(id);
        if (earlierSavedObject != null) {
            // todo merge objects here
            return earlierSavedObject;
        }
        if (id != null && isAttachment(id)) { // attachments should never be saved again - they're "final"
            return object;
        }
        DomainObject savedObject = crudService.save(object);
        savedObjectsById.put(savedObject.getId(), savedObject);
        return savedObject;
    }

    private void delete(FormSaveOperation operation) {
        final Id id = operation.id;
        if (isAttachment(id)) {
            attachmentService.deleteAttachment(id);
        } else {
            crudService.delete(id);
        }
    }

    private boolean isAttachment(Id id) {
        return configurationExplorer.isAttachmentType(crudService.getDomainObjectType(id));
    }

    private void addNewNodeChainAndFillCreateAndReferencesUpdateOperations(FieldPath objectPath, HashMap<String, FieldPath> referencesToFill) {
        // let's say we are pointing to a.b.c.d (objects, not values)
        // it means 1:1 relationship
        // a->b exists, b->c empty, c->d empty
        // 1) create d and put it into a.b.c.d
        // 2) create c and put it into a.b.c
        // 3) as b exists, that's all
        SingleObjectNode node = (SingleObjectNode) formObjects.getNode(objectPath);
        if (!node.isEmpty()) { // root node is never empty - thus operation of its creation is not created in this method
            return;
        }
        FieldPath parentPath = objectPath.getParentPath();
        final boolean oneToOneBackReference = objectPath.isOneToOneBackReference();
        DomainObject domainObject = crudService.createDomainObject(node.getType());
        node.setDomainObject(domainObject);

        referencesToFill = referencesToFill == null ? new HashMap<String, FieldPath>(2) : referencesToFill;
        if (oneToOneBackReference) { // reference to parent object should be updated in this object
            referencesToFill.put(objectPath.getLinkToParentName(), parentPath);
        }
        toCreate.put(objectPath, new ObjectCreationOperation(objectPath, referencesToFill));
        // ref to this will have to be updated in parent objects

        SingleObjectNode parentNode = (SingleObjectNode) formObjects.getNode(parentPath);
        // todo add 2nd link for 1:1 back reference
        if (parentNode.isEmpty()) {
            HashMap<String, FieldPath> referencesToFillInParent = new HashMap<>(2);
            if (!oneToOneBackReference) { // direct reference
                String fieldNameInParent = objectPath.getFieldName();
                referencesToFillInParent.put(fieldNameInParent, objectPath);
            }
            addNewNodeChainAndFillCreateAndReferencesUpdateOperations(parentPath, referencesToFillInParent);
            return;
        }

        if (oneToOneBackReference) { // nothing to do more for back links
            return;
        }

        // but for direct links parent object references to this should be updated
        String fieldNameInParent = objectPath.getFieldName();
        final ObjectCreationOperation creationOperationOfParentNode = toCreate.get(parentPath);
        if (creationOperationOfParentNode != null) { // parent node ref to this node should be updated
            creationOperationOfParentNode.refFieldObjectFieldPath.put(fieldNameInParent, objectPath);
            return;
        }

        // node is not empty and no creation operation for it exists - it means this node contains existing object which
        // references should also be updated
        ObjectReferencesUpdateOperation referencesUpdateOperation = toUpdateExistingObjectsReferences.get(parentPath);
        if (referencesUpdateOperation == null) {
            referencesUpdateOperation = new ObjectReferencesUpdateOperation(parentPath, new HashMap<String, FieldPath>(2));
            toUpdateExistingObjectsReferences.put(parentPath, referencesUpdateOperation);
        }
        referencesUpdateOperation.refFieldObjectFieldPath.put(fieldNameInParent, objectPath);
    }

    private HashMap<FieldPath, ArrayList<Id>> getBackReferenceFieldPathsIds(FieldPath[] fieldPaths,
                                                                            LinkEditingWidgetState widgetState) {
        HashMap<FieldPath, ArrayList<Id>> result = new HashMap<>();
        for (FieldPath fieldPath : fieldPaths) {
            result.put(fieldPath, new ArrayList<Id>());
        }
        ArrayList<Id> ids = widgetState.getIds();
        if (ids == null) {
            return result;
        }
        for (Id id : ids) {
            for (FieldPath fieldPath : fieldPaths) {
                String linkedType;
                String referenceType = fieldPath.getReferenceType();
                if (fieldPath.isOneToManyReference()) {
                    linkedType = referenceType;
                } else {
                    String referenceName = fieldPath.getReferenceName();
                    ReferenceFieldConfig referenceFieldConfig = (ReferenceFieldConfig)
                            configurationExplorer.getFieldConfig(referenceType, referenceName);
                    linkedType = referenceFieldConfig.getType();
                }

                if (crudService.getDomainObjectType(id).equalsIgnoreCase(linkedType)) {
                    result.get(fieldPath).add(id);
                    break;
                }
            }
        }
        return result;
    }

    private ArrayList<FormSaveOperation> mergeObjectReferences(FieldPath fieldPath, ArrayList<Id> newIds, boolean deleteEntriesOnLinkDrop) {
        if (fieldPath.isOneToManyReference()) {
            return mergeOneToMany(fieldPath, newIds, deleteEntriesOnLinkDrop);
        } else {
            return mergeManyToMany(fieldPath, newIds, deleteEntriesOnLinkDrop);
        }
    }

    private ArrayList<FormSaveOperation> mergeOneToMany(FieldPath fieldPath, ArrayList<Id> newIds, boolean deleteEntriesOnLinkDrop) {

        // there will be an exception if multi-object node is a parent for one-to-many relationship
        DomainObject parentObject = getSingleDomainObject(fieldPath.getParentPath());
        Id parentObjectId = parentObject.getId();

        String linkToParentName = fieldPath.getLinkToParentName();

        ArrayList<DomainObject> previousState = ((MultiObjectNode) formObjects.getNode(fieldPath)).getDomainObjects();
        if (previousState == null) {
            previousState = new ArrayList<>(0);
        }
        if (newIds == null) {
            newIds = new ArrayList<>(0);
        }

        HashSet<Id> previousIds = new HashSet<>(previousState.size());
        for (DomainObject previousStateObject : previousState) {
            previousIds.add(previousStateObject.getId());
        }

        ArrayList<FormSaveOperation> operations = new ArrayList<>(previousIds.size() + newIds.size());

        // links to create
        for (Id id : newIds) {
            if (previousIds.contains(id)) {
                continue; // nothing to update
            }
            DomainObject objectToSetLinkIn = crudService.find(id);
            objectToSetLinkIn.setReference(linkToParentName, parentObjectId);
            operations.add(new FormSaveOperation(FormSaveOperation.Type.Create, objectToSetLinkIn, linkToParentName));
        }

        // links to drop
        previousIds.removeAll(newIds); // leave only those which aren't in new IDs
        for (Id id : previousIds) {
            if (id == null) {
                continue;
            }
            if (deleteEntriesOnLinkDrop) {
                operations.add(new FormSaveOperation(FormSaveOperation.Type.Delete, id));
                continue;
            }
            DomainObject objectToDropLinkIn = crudService.find(id);
            objectToDropLinkIn.setReference(linkToParentName, (Id) null);
            operations.add(new FormSaveOperation(FormSaveOperation.Type.Update, objectToDropLinkIn, null));
        }
        return operations;
    }

    private ArrayList<FormSaveOperation> mergeManyToMany(FieldPath fieldPath, ArrayList<Id> newIds, boolean deleteEntriesOnLinkDrop) {
        MultiObjectNode mergedNode = (MultiObjectNode) formObjects.getNode(fieldPath);
        String linkObjectType = mergedNode.getType();

        String rootLinkField = fieldPath.getLinkToParentName();

        ArrayList<DomainObject> previousInBetweenDOs = mergedNode.getDomainObjects();
        if (previousInBetweenDOs == null) {
            previousInBetweenDOs = new ArrayList<>(0);
        }
        if (newIds == null) {
            newIds = new ArrayList<>(0);
        }

        HashSet<Id> prevLinkedIds = new HashSet<>(previousInBetweenDOs.size());
        HashMap<Id, DomainObject> prevInBetweenDOsByLinkedObjectId = new HashMap<>(previousInBetweenDOs.size());
        String linkToChildrenName = fieldPath.getLinkToChildrenName();
        for (DomainObject prevInBetweenObject : previousInBetweenDOs) {
            Id linkedObjectId = prevInBetweenObject.getReference(linkToChildrenName);
            prevLinkedIds.add(linkedObjectId);
            prevInBetweenDOsByLinkedObjectId.put(linkedObjectId, prevInBetweenObject);
        }

        ArrayList<FormSaveOperation> operations = new ArrayList<>(prevLinkedIds.size() + newIds.size());

        // there will be an exception if multi-object node is a parent for one-to-many relationship
        DomainObject parentObject = getSingleDomainObject(fieldPath.getParentPath());
        Id parentObjectId = parentObject.getId();
        // links to create
        for (Id id : newIds) {
            if (prevLinkedIds.contains(id)) {
                continue; // nothing to update
            }
            DomainObject newInBetweenObject = crudService.createDomainObject(linkObjectType);
            newInBetweenObject.setReference(rootLinkField, parentObjectId);
            newInBetweenObject.setReference(linkToChildrenName, id);
            operations.add(new FormSaveOperation(FormSaveOperation.Type.Create, newInBetweenObject, rootLinkField));
        }

        // links to drop
        prevLinkedIds.removeAll(newIds); // leave only those which aren't in new values
        for (Id id : prevLinkedIds) {
            if (id == null) {
                continue;
            }
            final DomainObject inBetweenDOToDrop = prevInBetweenDOsByLinkedObjectId.get(id);
            operations.add(new FormSaveOperation(FormSaveOperation.Type.Delete, inBetweenDOToDrop.getId()));
            if (deleteEntriesOnLinkDrop) {
                operations.add(new FormSaveOperation(FormSaveOperation.Type.Delete, id));
            }
        }
        return operations;
    }

    private DomainObject getSingleDomainObject(FieldPath fieldPath) {
        return ((SingleObjectNode) formObjects.getNode(fieldPath)).getDomainObject();
    }

    private ArrayList<DomainObject> getDomainObjects(FieldPath fieldPath) {
        return ((MultiObjectNode) formObjects.getNode(fieldPath)).getDomainObjects();
    }
}
