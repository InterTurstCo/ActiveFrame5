package ru.intertrust.cm.core.gui.impl.server.form;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.gui.form.FormConfig;
import ru.intertrust.cm.core.config.model.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.config.model.gui.form.widget.WidgetConfigurationConfig;
import ru.intertrust.cm.core.gui.api.server.widget.LinkEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.FormObjects;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.ObjectsNode;
import ru.intertrust.cm.core.gui.model.form.widget.LinkEditingWidgetState;
import ru.intertrust.cm.core.gui.model.form.widget.ValueEditingWidgetState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

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

    public DomainObject saveForm(FormState formState) {
        FormConfig formConfig = configurationExplorer.getConfig(FormConfig.class, formState.getName());
        WidgetConfigurationConfig widgetConfigurationConfig = formConfig.getWidgetConfigurationConfig();
        List<WidgetConfig> widgetConfigs = widgetConfigurationConfig.getWidgetConfigList();
        FormObjects formObjects = formState.getObjects();

        HashSet<ObjectCreationOperation> newObjectsCreationOperations = new HashSet<>();
        ArrayList<FormSaveOperation> linkChangeOperations = new ArrayList<>();

        HashSet<FieldPath> objectsFieldPathsToUpdate = new HashSet<>();
        for (WidgetConfig widgetConfig : widgetConfigs) {
            WidgetState widgetState = formState.getWidgetState(widgetConfig.getId());
            if (widgetState == null) { // ignore - such data shouldn't be saved
                continue;
            }
            FieldPath fieldPath = new FieldPath(widgetConfig.getFieldPathConfig().getValue());
            FieldPath parentObjectPath = fieldPath.getParentPath();
            if (fieldPath.isBackReference()) {
                ArrayList<Id> newIds = ((LinkEditingWidgetState) widgetState).getIds();
                linkChangeOperations.addAll(mergeObjectReferences(fieldPath, formObjects, newIds));
                continue;
            }

            Value newValue = ((ValueEditingWidgetState) widgetState).getValue();
            Value oldValue = formObjects.getFieldValue(fieldPath);
            if (!areValuesSemanticallyEqual(newValue, oldValue)) {
                ArrayList<ObjectCreationOperation> objectCreationOperations = addNewNodeChainIfNotEmpty(parentObjectPath, formObjects);
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
                formObjects.setFieldValue(fieldPath, newValue);
            }
        }

        HashMap<Id, DomainObject> savedObjectsMap = new HashMap<>();
        createNewDirectLinkObjects(newObjectsCreationOperations, formObjects, savedObjectsMap);

        // root DO is save separately as we should return it's identifier in case it's created from scratch
        boolean saveRoot = false;
        for (FieldPath fieldPath : objectsFieldPathsToUpdate) {
            if (fieldPath.isRoot()) {
                saveRoot = true;
                continue;
            }
            // todo: why domain objects? it's the only one most likely
            ArrayList<DomainObject> domainObjects = formObjects.getNode(fieldPath).getDomainObjects();
            save(domainObjects, savedObjectsMap);
            formObjects.getNode(fieldPath).setDomainObjects(domainObjects);
        }
        DomainObject rootDomainObject = formObjects.getRootNode().getObject();
        if (saveRoot) {
            rootDomainObject = save(rootDomainObject, savedObjectsMap);

            // todo: do this for all saved objects to keep new objects IDs up to date
            formObjects.getRootNode().setObject(rootDomainObject);
        }
        ReferenceValue rootObjectReference = new ReferenceValue(rootDomainObject.getId());

        for (FormSaveOperation operation : linkChangeOperations) {
            if (operation.type == FormSaveOperation.Type.Delete) {
                crudService.delete(operation.domainObject.getId());
            } else {
                operation.domainObject.setValue(operation.fieldToSetWithRootReference, rootObjectReference);
                save(operation.domainObject, savedObjectsMap);
            }
        }
        saveNewLinkedObjects(formState, widgetConfigs);
        return rootDomainObject;
    }

    private void saveNewLinkedObjects(FormState formState, List<WidgetConfig> widgetConfigs) {
        FormObjects formObjects = formState.getObjects();
        for (WidgetConfig config : widgetConfigs) {
            WidgetState widgetState = formState.getWidgetState(config.getId());
            if (widgetState == null) { // ignore - such data shouldn't be saved
                continue;
            }
            WidgetHandler componentHandler = (WidgetHandler) applicationContext.getBean(config.getComponentName());
            if (componentHandler instanceof LinkEditingWidgetHandler) {
                WidgetContext widgetContext = new WidgetContext(config, formObjects);
                ((LinkEditingWidgetHandler) componentHandler).saveNewObjects(widgetContext, widgetState);
            }
        }
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

    private void createNewDirectLinkObjects(HashSet<ObjectCreationOperation> newObjectsCreationOperations,
                                            FormObjects formObjects, HashMap<Id, DomainObject> savedObjectsMap) {
        ArrayList<ObjectCreationOperation> creationOperations = new ArrayList<>(newObjectsCreationOperations.size());
        creationOperations.addAll(newObjectsCreationOperations);
        Collections.sort(creationOperations);

        for (ObjectCreationOperation operation : creationOperations) {
            DomainObject newObject = save(formObjects.getNode(operation.path).getObject(), savedObjectsMap);
            if (operation.parentToUpdateReference != null) {
                DomainObject parentObject = formObjects.getNode(operation.parentToUpdateReference).getObject();
                parentObject.setValue(operation.parentField, new ReferenceValue(newObject.getId()));
            }
        }
    }

    private ArrayList<DomainObject> save(ArrayList<DomainObject> domainObjects, HashMap<Id, DomainObject> savedObjects) {
        for (int i = 0; i < domainObjects.size(); ++i) {
            DomainObject domainObject = domainObjects.get(i);
            DomainObject savedObject = save(domainObject, savedObjects);
            domainObjects.set(i, savedObject);
        }
        return domainObjects;
    }

    private DomainObject save(DomainObject object, HashMap<Id, DomainObject> savedObjects) {
        // this is required to avoid optimistic lock exceptions when same object is being edited by several widgets,
        // for example, one widget is editing object's properties while the other edits links
        DomainObject earlierSavedObject = savedObjects.get(object.getId());
        if (earlierSavedObject != null) {
            // todo merge objects here
            return earlierSavedObject;
        }
        DomainObject savedObject = crudService.save(object);
        savedObjects.put(savedObject.getId(), savedObject);
        return savedObject;
    }

    private ArrayList<ObjectCreationOperation> addNewNodeChainIfNotEmpty(FieldPath objectPath, FormObjects formObjects) {
        // let's say we are pointing to a.b.c.d (objects, not values)
        // it means 1:1 relationship
        // a->b exists, b->c empty, c->d empty
        // 1) create d and put it into a.b.c.d
        // 2) create c and put it into a.b.c
        // 3) as b exists, that's all
        ObjectsNode node = formObjects.getNode(objectPath);
        if (node.size() > 0) {
            return new ArrayList<>(0);
        }
        ArrayList<ObjectCreationOperation> result = new ArrayList<>();
        DomainObject domainObject = crudService.createDomainObject(node.getType());
        formObjects.setNode(objectPath, new ObjectsNode(domainObject));
        FieldPath parentPath = objectPath.getParentPath();
        ObjectsNode parentNode = formObjects.getNode(parentPath);
        String fieldNameInParent = objectPath.getFieldName();
        if (parentNode.isEmpty()) {
            // parent path doesn't contain objects, so ref to this will have to be updated
            result.add(new ObjectCreationOperation(objectPath, parentPath, fieldNameInParent));
        } else {
            Value value = parentNode.getObject().getValue(fieldNameInParent);
            if (value == null || value.get() == null) {
                result.add(new ObjectCreationOperation(objectPath, parentPath, fieldNameInParent));
            } else {
                result.add(new ObjectCreationOperation(objectPath, null, null));
            }
        }
        result.addAll(addNewNodeChainIfNotEmpty(parentPath, formObjects));

        return result;
    }

    private ArrayList<FormSaveOperation> mergeObjectReferences(FieldPath fieldPath, FormObjects formObjects,
                                                               ArrayList<Id> newIds) {
        if (fieldPath.isOneToManyReference()) {
            return mergeOneToMany(fieldPath, formObjects, newIds);
        } else {
            return mergeManyToMany(fieldPath, formObjects, newIds);
        }
    }

    private ArrayList<FormSaveOperation> mergeOneToMany(FieldPath fieldPath, FormObjects formObjects,
                                                        ArrayList<Id> newIds) {
        ArrayList<DomainObject> parentObjects = formObjects.getNode(fieldPath.getParentPath()).getDomainObjects();
        if (parentObjects.size() > 1) {
            throw new GuiException("Back reference is referencing " + parentObjects.size() + " objects");
        }
        Id parentObjectId = parentObjects.get(0).getId();

        String linkToParentName = fieldPath.getLinkToParentName();

        ArrayList<DomainObject> previousState = formObjects.getNode(fieldPath).getDomainObjects();
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
            DomainObject objectToDropLinkIn = crudService.find(id);
            objectToDropLinkIn.setReference(linkToParentName, (Id) null);
            operations.add(new FormSaveOperation(FormSaveOperation.Type.Update, objectToDropLinkIn, null));
        }
        return operations;
    }

    private ArrayList<FormSaveOperation> mergeManyToMany(FieldPath fieldPath, FormObjects formObjects,
                                                         ArrayList<Id> newIds) {
        ObjectsNode mergedNode = formObjects.getNode(fieldPath);
        String linkObjectType = mergedNode.getType();
        FieldPath parentNodePath = fieldPath.getParentPath();
        ArrayList<DomainObject> parentObjects = formObjects.getNode(parentNodePath).getDomainObjects();
        if (parentObjects.size() > 1) {
            throw new GuiException("Back reference is referencing " + parentObjects.size() + " objects");
        }

        String rootLinkField = fieldPath.getLinkToParentName();

        ArrayList<DomainObject> previousState = mergedNode.getDomainObjects();
        if (previousState == null) {
            previousState = new ArrayList<>(0);
        }
        if (newIds == null) {
            newIds = new ArrayList<>(0);
        }

        HashSet<Id> previousIds = new HashSet<>(previousState.size());
        HashMap<Id, DomainObject> previousDomainObjectsById = new HashMap<>(previousState.size());
        String linkToChildrenName = fieldPath.getLinkToChildrenName();
        for (DomainObject previousStateObject : previousState) {
            Id id = previousStateObject.getReference(linkToChildrenName);
            previousIds.add(id);
            previousDomainObjectsById.put(id, previousStateObject);
        }

        Id parentObjectId = parentObjects.get(0).getId();
        ArrayList<FormSaveOperation> operations = new ArrayList<>(previousIds.size() + newIds.size());

        // links to create
        for (Id id : newIds) {
            if (previousIds.contains(id)) {
                continue; // nothing to update
            }
            DomainObject newLinkObject = crudService.createDomainObject(linkObjectType);
            newLinkObject.setReference(rootLinkField, parentObjectId);
            newLinkObject.setReference(linkToChildrenName, id);
            operations.add(new FormSaveOperation(FormSaveOperation.Type.Create, newLinkObject, rootLinkField));
        }

        // links to drop
        previousIds.removeAll(newIds); // leave only those which aren't in new values
        for (Id id : previousIds) {
            if (id == null) {
                continue;
            }
            DomainObject objectToDrop = previousDomainObjectsById.get(id);
            operations.add(new FormSaveOperation(FormSaveOperation.Type.Delete, objectToDrop, rootLinkField));
        }
        return operations;
    }

}
