package ru.intertrust.cm.core.gui.impl.server.form;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.gui.form.FormConfig;
import ru.intertrust.cm.core.config.model.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.config.model.gui.form.widget.WidgetConfigurationConfig;
import ru.intertrust.cm.core.gui.api.server.widget.LinkEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.model.form.*;
import ru.intertrust.cm.core.gui.model.form.widget.LinkEditingWidgetState;
import ru.intertrust.cm.core.gui.model.form.widget.ValueEditingWidgetState;
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
            FieldPath fieldPath = new FieldPath(widgetConfig.getFieldPathConfig().getValue());
            FieldPath parentObjectPath = fieldPath.getParentPath();
            if (fieldPath.isBackReference()) {
                ArrayList<Id> newIds = ((LinkEditingWidgetState) widgetState).getIds();
                linkChangeOperations.addAll(mergeObjectReferences(fieldPath, newIds));
                continue;
            }

            Value newValue = ((ValueEditingWidgetState) widgetState).getValue();
            Value oldValue = formObjects.getFieldValue(fieldPath);
            if (!areValuesSemanticallyEqual(newValue, oldValue)) {
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
                formObjects.setFieldValue(fieldPath, newValue);
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
                crudService.delete(operation.domainObject.getId());
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
        DomainObject empl = crudService.find(new RdbmsId(14, 1));
        empl.setBoolean("HasChildren", true);
        crudService.save(empl);


        // this is required to avoid optimistic lock exceptions when same object is being edited by several widgets,
        // for example, one widget is editing object's properties while the other edits links
        DomainObject earlierSavedObject = savedObjectsById.get(object.getId());
        if (earlierSavedObject != null) {
            // todo merge objects here
            return earlierSavedObject;
        }
        DomainObject savedObject = crudService.save(object);
        savedObjectsById.put(savedObject.getId(), savedObject);
        return savedObject;
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

    private ArrayList<FormSaveOperation> mergeObjectReferences(FieldPath fieldPath, ArrayList<Id> newIds) {
        if (fieldPath.isOneToManyReference()) {
            return mergeOneToMany(fieldPath, newIds);
        } else {
            return mergeManyToMany(fieldPath, newIds);
        }
    }

    private ArrayList<FormSaveOperation> mergeOneToMany(FieldPath fieldPath, ArrayList<Id> newIds) {

        // there will be an exception if multi-object node is a parent for one-to-many relationship
        DomainObject parentObject = getSingleDomainObject(fieldPath.getParentPath());
        Id parentObjectId = parentObject.getId();

        String linkToParentName = fieldPath.getLinkToParentName();

        ArrayList<DomainObject> previousState = ((MultiObjectNode)formObjects.getNode(fieldPath)).getDomainObjects();
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

    private ArrayList<FormSaveOperation> mergeManyToMany(FieldPath fieldPath, ArrayList<Id> newIds) {
        MultiObjectNode mergedNode = (MultiObjectNode) formObjects.getNode(fieldPath);
        String linkObjectType = mergedNode.getType();

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

        ArrayList<FormSaveOperation> operations = new ArrayList<>(previousIds.size() + newIds.size());

        // there will be an exception if multi-object node is a parent for one-to-many relationship
        DomainObject parentObject = getSingleDomainObject(fieldPath.getParentPath());
        Id parentObjectId = parentObject.getId();
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

    private DomainObject getSingleDomainObject(FieldPath fieldPath) {
        return ((SingleObjectNode) formObjects.getNode(fieldPath)).getDomainObject();
    }

    private ArrayList<DomainObject> getDomainObjects(FieldPath fieldPath) {
        return ((MultiObjectNode) formObjects.getNode(fieldPath)).getDomainObjects();
    }
}
