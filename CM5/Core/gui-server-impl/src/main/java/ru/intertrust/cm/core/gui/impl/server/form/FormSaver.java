package ru.intertrust.cm.core.gui.impl.server.form;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.FieldConfig;
import ru.intertrust.cm.core.config.model.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.model.gui.form.FormConfig;
import ru.intertrust.cm.core.config.model.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.config.model.gui.form.widget.WidgetConfigurationConfig;
import ru.intertrust.cm.core.gui.api.server.widget.MultiObjectWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.FormObjects;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.ObjectsNode;
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
            String parentType = formObjects.getObjects(parentObjectPath).getType();
            FieldConfig fieldConfig = configurationExplorer.getFieldConfig(parentType, fieldPath.getLastElement().getName());
            boolean lastElementIsReference = fieldConfig == null || fieldConfig instanceof ReferenceFieldConfig;
            boolean widgetIsChangingRelationships = fieldPath.isBackReference() && lastElementIsReference;
            if (widgetIsChangingRelationships) {
                ArrayList<Value> newValue = widgetState.toValues();
                linkChangeOperations.addAll(mergeObjectReferences(fieldPath, formObjects, newValue));
                continue;
            }

            ArrayList<Value> newValue = widgetState.toValues();
            ArrayList<Value> oldValue = formObjects.getObjectValues(fieldPath);
            if (!areValuesSemanticallyEqual(newValue, oldValue)) {
                ArrayList<ObjectCreationOperation> objectCreationOperations = addNewNodeChainIfNotEmpty(parentObjectPath, formObjects);
                if (!objectCreationOperations.isEmpty()) {
                    for (ObjectCreationOperation operation : objectCreationOperations) {
                        if (operation.parentToUpdateReference != null) {
                            objectsFieldPathsToUpdate.add(operation.parentToUpdateReference);
                        }
                    }
                    newObjectsCreationOperations.addAll(objectCreationOperations);
                } else { // if object is created separately, no need to update it later
                    objectsFieldPathsToUpdate.add(parentObjectPath);
                }
                formObjects.setObjectValues(fieldPath, newValue);
            }
        }

        HashMap<Id, DomainObject> savedObjectsMap = new HashMap<>();
        createNewDirectLinkObjects(newObjectsCreationOperations, formObjects, savedObjectsMap);

        ArrayList<DomainObject> toSave = new ArrayList<>(objectsFieldPathsToUpdate.size());
        // todo sort field paths in such a way that linked objects are saved first?
        // root DO is save separately as we should return it's identifier in case it's created from scratch
        boolean saveRoot = false;
        for (FieldPath fieldPath : objectsFieldPathsToUpdate) {
            if (fieldPath.isRoot()) {
                saveRoot = true;
                continue;
            }
            toSave.addAll(formObjects.getObjects(fieldPath).getDomainObjects());
        }
        for (DomainObject object : toSave) {
            save(object, savedObjectsMap);
        }
        DomainObject rootDomainObject = formObjects.getRootObjects().getObject();
        if (saveRoot) {
            rootDomainObject = save(rootDomainObject, savedObjectsMap);

            // todo: do this for all saved objects to keep new objects IDs up to date
            formObjects.getRootObjects().setObject(rootDomainObject);
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
            if (componentHandler instanceof MultiObjectWidgetHandler) {
                WidgetContext widgetContext = new WidgetContext(config, formObjects);
                ((MultiObjectWidgetHandler) componentHandler).saveNewObjects(widgetContext, widgetState);
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

    private boolean areValuesSemanticallyEqual(ArrayList<Value> newValue, ArrayList<Value> oldValue) {
        return false;
    }

    private void createNewDirectLinkObjects(HashSet<ObjectCreationOperation> newObjectsCreationOperations,
                                            FormObjects formObjects, HashMap<Id, DomainObject> savedObjectsMap) {
        ArrayList<ObjectCreationOperation> creationOperations = new ArrayList<>(newObjectsCreationOperations.size());
        creationOperations.addAll(newObjectsCreationOperations);
        Collections.sort(creationOperations);

        for (ObjectCreationOperation operation : creationOperations) {
            DomainObject newObject = save(formObjects.getObjects(operation.path).getObject(), savedObjectsMap);
            if (operation.parentToUpdateReference != null) {
                DomainObject parentObject = formObjects.getObjects(operation.parentToUpdateReference).getObject();
                parentObject.setValue(operation.parentField, new ReferenceValue(newObject.getId()));
            }
        }
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
        ObjectsNode node = formObjects.getObjects(objectPath);
        if (node.size() > 0) {
            return new ArrayList<>(0);
        }
        ArrayList<ObjectCreationOperation> result = new ArrayList<>();
        DomainObject domainObject = crudService.createDomainObject(node.getType());
        formObjects.setObjects(objectPath, new ObjectsNode(domainObject));
        FieldPath parentPath = objectPath.getParentPath();
        ObjectsNode parentNode = formObjects.getObjects(parentPath);
        String linkToThisPathFromParent = objectPath.getLastElement().getName();
        if (parentNode.isEmpty()) {
            // parent path doesn't contain objects, so ref to this will have to be updated
            result.add(new ObjectCreationOperation(objectPath, parentPath, linkToThisPathFromParent));
        } else {
            Value value = parentNode.getObject().getValue(linkToThisPathFromParent);
            if (value == null || value.get() == null) {
                result.add(new ObjectCreationOperation(objectPath, parentPath, linkToThisPathFromParent));
            } else {
                result.add(new ObjectCreationOperation(objectPath, null, null));
            }
        }
        result.addAll(addNewNodeChainIfNotEmpty(parentPath, formObjects));

        return result;
    }

    private ArrayList<FormSaveOperation> mergeObjectReferences(FieldPath fieldPath, FormObjects formObjects,
                                                               ArrayList<Value> newValues) {
        //todo after country_city^country.city is transformed to an Object, but not to a field lots of if-else will go
        String lastElement = fieldPath.getLastElement().getName();
        String[] typeAndField = lastElement.split("\\^");
        boolean oneToMany = typeAndField.length == 2;
        if (oneToMany) {
            return mergeOneToMany(fieldPath, formObjects, newValues);
        } else {
            return mergeManyToMany(fieldPath, formObjects, newValues);
        }
    }

    private ArrayList<FormSaveOperation> mergeOneToMany(FieldPath fieldPath, FormObjects formObjects,
                                                        ArrayList<Value> newValues) {
        ArrayList<DomainObject> parentObjects = formObjects.getObjects(fieldPath.getParentPath()).getDomainObjects();
        if (parentObjects.size() > 1) {
            throw new GuiException("Back reference is referencing " + parentObjects.size() + " objects");
        }
        Id parentObjectId = parentObjects.get(0).getId();

        String lastElement = fieldPath.getLastElement().getName();
        String[] typeAndField = lastElement.split("\\^");
        String type = typeAndField[0];
        String field = typeAndField[1];

        ArrayList<DomainObject> previousState = formObjects.getObjects(fieldPath).getDomainObjects();
        if (previousState == null) {
            previousState = new ArrayList<>(0);
        }
        if (newValues == null) {
            newValues = new ArrayList<>(0);
        }

        HashSet<Value> oldValuesSet = new HashSet<>(previousState.size());
        for (DomainObject previousStateObject : previousState) {
            oldValuesSet.add(new ReferenceValue(previousStateObject.getId()));
        }

        ArrayList<FormSaveOperation> operations = new ArrayList<>(oldValuesSet.size() + newValues.size());

        // links to create
        for (Value value : newValues) {
            if (oldValuesSet.contains(value)) {
                continue; // nothing to update
            }
            Id referenceId = ((ReferenceValue) value).get();
            DomainObject objectToSetLinkIn = crudService.find(referenceId);
            objectToSetLinkIn.setReference(field, parentObjectId);
            operations.add(new FormSaveOperation(FormSaveOperation.Type.Create, objectToSetLinkIn, field));
        }

        // links to drop
        oldValuesSet.removeAll(newValues); // leave only those which aren't in new values
        for (Value value : oldValuesSet) {
            if (value == null) {
                continue;
            }
            Id referenceId = ((ReferenceValue) value).get();
            DomainObject objectToDropLinkIn = crudService.find(referenceId);
            objectToDropLinkIn.setReference(field, (Id) null);
            operations.add(new FormSaveOperation(FormSaveOperation.Type.Update, objectToDropLinkIn, null));
        }
        return operations;
    }

    private ArrayList<FormSaveOperation> mergeManyToMany(FieldPath fieldPath, FormObjects formObjects,
                                                         ArrayList<Value> newValues) {
        String linkField = fieldPath.getLastElement().getName();
        FieldPath mergedNodePath = fieldPath.getParentPath();
        ObjectsNode mergedNode = formObjects.getObjects(mergedNodePath);
        String linkObjectType = mergedNode.getType();
        FieldPath parentNodePath = mergedNodePath.getParentPath();
        ArrayList<DomainObject> parentObjects = formObjects.getObjects(parentNodePath).getDomainObjects();
        if (parentObjects.size() > 1) {
            throw new GuiException("Back reference is referencing " + parentObjects.size() + " objects");
        }

        String[] typeAndField = mergedNodePath.getLastElement().getName().split("\\^");
        String rootLinkField = typeAndField[1];

        ArrayList<DomainObject> previousState = mergedNode.getDomainObjects();
        if (previousState == null) {
            previousState = new ArrayList<>(0);
        }
        if (newValues == null) {
            newValues = new ArrayList<>(0);
        }

        HashSet<Value> oldValuesSet = new HashSet<>(previousState.size());
        HashMap<Value, DomainObject> oldValuesDomainObjects = new HashMap<>(previousState.size());
        for (DomainObject previousStateObject : previousState) {
            Value value = previousStateObject.getValue(linkField);
            oldValuesSet.add(value);
            oldValuesDomainObjects.put(value, previousStateObject);
        }

        ReferenceValue parentObjectReference = new ReferenceValue(parentObjects.get(0).getId());
        ArrayList<FormSaveOperation> operations = new ArrayList<>(oldValuesSet.size() + newValues.size());

        // links to create
        for (Value value : newValues) {
            if (oldValuesSet.contains(value)) {
                continue; // nothing to update
            }
            DomainObject newLinkObject = crudService.createDomainObject(linkObjectType);
            newLinkObject.setValue(rootLinkField, parentObjectReference);
            newLinkObject.setValue(linkField, value);
            operations.add(new FormSaveOperation(FormSaveOperation.Type.Create, newLinkObject, rootLinkField));
        }

        // links to drop
        oldValuesSet.removeAll(newValues); // leave only those which aren't in new values
        for (Value value : oldValuesSet) {
            if (value == null) {
                continue;
            }
            DomainObject objectToDrop = oldValuesDomainObjects.get(value);
            operations.add(new FormSaveOperation(FormSaveOperation.Type.Delete, objectToDrop, rootLinkField));
        }
        return operations;
    }

}
