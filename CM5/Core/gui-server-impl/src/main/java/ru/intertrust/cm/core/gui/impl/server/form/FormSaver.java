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
import ru.intertrust.cm.core.config.FieldConfig;
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
    private FormObjects formObjects;
    private List<WidgetConfig> widgetConfigs;
    private HashSet<ObjectCreationOperation> newObjectsCreationOperations;
    private ArrayList<FormSaveOperation> linkChangeOperations;
    private HashSet<FieldPath> objectsFieldPathsToUpdate;
    private HashMap<Id, DomainObject> savedObjectsById;

    public FormSaver(FormState formState) {
        this.formState = formState;
        this.formObjects = formState.getObjects();
        newObjectsCreationOperations = new HashSet<>();
        linkChangeOperations = new ArrayList<>();
        objectsFieldPathsToUpdate = new HashSet<>();
        savedObjectsById = new HashMap<>();
    }

    @PostConstruct
    private void init() {
        FormConfig formConfig = configurationExplorer.getConfig(FormConfig.class, formState.getName());
        WidgetConfigurationConfig widgetConfigurationConfig = formConfig.getWidgetConfigurationConfig();
        widgetConfigs = widgetConfigurationConfig.getWidgetConfigList();
    }

    public DomainObject saveForm() {
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
                    FormSaver formSaver = (FormSaver) applicationContext.getBean("formSaver", nestedFormState);
                    formSaver.saveForm();
                }
            }

            FieldPath[] fieldPaths = FieldPath.createPaths(widgetConfig.getFieldPathConfig().getValue());
            FieldPath firstFieldPath = fieldPaths[0];
            WidgetHandler handler = getWidgetHandler(widgetConfig);
            if (firstFieldPath.isBackReference()) {
                boolean deleteEntriesOnLinkDrop = ((LinkEditingWidgetHandler) handler).deleteEntriesOnLinkDrop();
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
                ArrayList<ObjectCreationOperation> objectCreationOperations = addNewNodeChainIfNotEmpty(parentObjectPath);
                if (!objectCreationOperations.isEmpty()) {
                    for (ObjectCreationOperation operation : objectCreationOperations) {
                        if (operation.parentToUpdateReference != null) {
                            objectsFieldPathsToUpdate.add(operation.parentToUpdateReference);
                        }
                    }
                    newObjectsCreationOperations.addAll(objectCreationOperations);
                } else { // value is changed -> add field's object to be updated
                    objectsFieldPathsToUpdate.add(parentObjectPath);
                }
                formObjects.setFieldValue(firstFieldPath, newValue);
            }
        }

        createNewDirectLinkObjects();

        // root DO is saved separately as we should return it's identifier in case it's created from scratch
        boolean saveRoot = false;
        for (FieldPath fieldPath : objectsFieldPathsToUpdate) {
            if (fieldPath.isRoot()) {
                saveRoot = true;
                continue;
            }
            DomainObject domainObject = getSingleDomainObject(fieldPath);
            save(domainObject);
            ((SingleObjectNode) formObjects.getNode(fieldPath)).setDomainObject(domainObject);
        }
        DomainObject rootDomainObject = formObjects.getRootNode().getDomainObject();
        saveRoot = saveRoot || rootDomainObject.getId() == null;
        if (saveRoot) {
            rootDomainObject = save(rootDomainObject);

            // todo: do this for all saved objects to keep new objects IDs up to date
            formObjects.getRootNode().setDomainObject(rootDomainObject);
        }
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

    private void saveNewLinkedObjects() {
        for (WidgetConfig config : widgetConfigs) {
            WidgetState widgetState = formState.getWidgetState(config.getId());
            if (widgetState == null) { // ignore - such data shouldn't be saved
                continue;
            }
            WidgetHandler widgetHandler = getWidgetHandler(config);
            if (!(widgetHandler instanceof LinkEditingWidgetHandler)) {
                continue;
            }
            WidgetContext widgetContext = new WidgetContext(config, formObjects);
            final LinkEditingWidgetHandler linkEditingHandler = (LinkEditingWidgetHandler) widgetHandler;
            final List<DomainObject> newObjects = linkEditingHandler.saveNewObjects(widgetContext, widgetState);
            if (newObjects == null || linkEditingHandler.handlesNewObjectsReferences()) {
                continue;
            }
            DomainObject rootDomainObject = formObjects.getRootNode().getDomainObject();
            FieldPath fieldPath = new FieldPath(config.getFieldPathConfig().getValue());
            for (DomainObject newObject : newObjects) {
                if (fieldPath.isOneToManyReference()) {
                    newObject.setReference(fieldPath.getLinkToParentName(), rootDomainObject);
                    crudService.save(newObject);
                } else if (fieldPath.isManyToManyReference()) {
                    String referenceType = fieldPath.getReferenceType();
                    FieldConfig fieldConfig = configurationExplorer.getFieldConfig(referenceType, fieldPath.getReferenceName());
                    DomainObject referencedObject = crudService.createDomainObject(referenceType);
                    if (fieldConfig != null) {
                        referencedObject.setReference(fieldConfig.getName(), newObject);
                    }
                    fieldConfig = configurationExplorer.getFieldConfig(referenceType, rootDomainObject.getTypeName());
                    if (fieldConfig != null) {
                        referencedObject.setReference(fieldConfig.getName(), rootDomainObject);
                    }
                    crudService.save(referencedObject);
                } else { // one-to-one reference
                    formObjects.setFieldValue(fieldPath, new ReferenceValue(newObject.getId()));
                    crudService.save(((SingleObjectNode) formObjects.getNode(fieldPath.getParentPath())).getDomainObject());
                }
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

    private void createNewDirectLinkObjects() {
        ArrayList<ObjectCreationOperation> creationOperations = new ArrayList<>(newObjectsCreationOperations.size());
        creationOperations.addAll(newObjectsCreationOperations);
        Collections.sort(creationOperations);

        for (ObjectCreationOperation operation : creationOperations) {
            DomainObject newObject = save(getSingleDomainObject(operation.path));
            if (operation.parentToUpdateReference != null) {
                DomainObject parentObject = getSingleDomainObject(operation.parentToUpdateReference);
                parentObject.setValue(operation.parentField, new ReferenceValue(newObject.getId()));
            }
        }
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

    private ArrayList<ObjectCreationOperation> addNewNodeChainIfNotEmpty(FieldPath objectPath) {
        // let's say we are pointing to a.b.c.d (objects, not values)
        // it means 1:1 relationship
        // a->b exists, b->c empty, c->d empty
        // 1) create d and put it into a.b.c.d
        // 2) create c and put it into a.b.c
        // 3) as b exists, that's all
        ObjectsNode node = formObjects.getNode(objectPath);
        if (!node.isEmpty()) {
            return new ArrayList<>(0);
        }
        ArrayList<ObjectCreationOperation> result = new ArrayList<>();
        DomainObject domainObject = crudService.createDomainObject(node.getType());
        formObjects.setNode(objectPath, new SingleObjectNode(domainObject));
        FieldPath parentPath = objectPath.getParentPath();
        SingleObjectNode parentNode = (SingleObjectNode) formObjects.getNode(parentPath);
        String fieldNameInParent = objectPath.getFieldName();
        if (parentNode.isEmpty()) {
            // parent path doesn't contain objects, so ref to this will have to be updated
            result.add(new ObjectCreationOperation(objectPath, parentPath, fieldNameInParent));
        } else {
            Value value = parentNode.getDomainObject().getValue(fieldNameInParent);
            if (value == null || value.get() == null) {
                result.add(new ObjectCreationOperation(objectPath, parentPath, fieldNameInParent));
            } else {
                result.add(new ObjectCreationOperation(objectPath, null, null));
            }
        }
        result.addAll(addNewNodeChainIfNotEmpty(parentPath));

        return result;
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
            return mergeManyToMany(fieldPath, newIds);
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

    private ArrayList<FormSaveOperation> mergeManyToMany(FieldPath fieldPath, ArrayList<Id> newIds) {
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
