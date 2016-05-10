package ru.intertrust.cm.core.gui.impl.server.form;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.FormSaveExtensionConfig;
import ru.intertrust.cm.core.config.gui.form.widget.EnumBoxConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfigurationConfig;
import ru.intertrust.cm.core.gui.api.server.form.FieldPathHelper;
import ru.intertrust.cm.core.gui.api.server.form.FormAfterSaveInterceptor;
import ru.intertrust.cm.core.gui.api.server.form.FormBeforeSaveInterceptor;
import ru.intertrust.cm.core.gui.api.server.widget.LinkEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.SelfManagingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.model.form.*;
import ru.intertrust.cm.core.gui.model.form.widget.LinkEditingWidgetState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.*;

/**
 * @author Denis Mitavskiy
 *         Date: 21.10.13
 *         Time: 13:03
 */
public class FormSaver extends FormProcessor {
    @Autowired
    private FieldPathHelper fieldPathHelper;

    private FormConfig formConfig;
    private FormState formState;
    private Map<FieldPath, Value> forcedRootDomainObjectValues;
    private FormObjects formObjects;
    private List<WidgetConfig> widgetConfigs;
    private CaseInsensitiveHashMap<WidgetConfig> widgetConfigsById;
    private HashMap<FieldPath, ObjectWithReferences> toCreate;
    private HashMap<FieldPath, ObjectWithReferences> toUpdateReferences;
    private HashSet<FieldPath> toUpdate;
    private HashMap<Id, DomainObject> savedObjectsById;
    private String beforeSaveComponent;
    private String afterSaveComponent;

    public void setContext(FormState formState, Map<FieldPath, Value> forcedRootDomainObjectValues) {
        this.formState = formState;
        this.formConfig = configurationExplorer.getPlainFormConfig(formState.getName());
        this.formObjects = formState.getObjects();
        this.forcedRootDomainObjectValues = forcedRootDomainObjectValues == null ? new HashMap<FieldPath, Value>(0) : forcedRootDomainObjectValues;
        toCreate = new HashMap<>();
        toUpdateReferences = new HashMap<>();
        toUpdate = new HashSet<>();
        savedObjectsById = new HashMap<>();
        final FormSaveExtensionConfig extensionConfig = formConfig.getFormSaveExtensionConfig();
        if (extensionConfig != null) {
            if (extensionConfig.getBeforeSaveComponent() != null) {
                this.beforeSaveComponent = extensionConfig.getBeforeSaveComponent();
            }
            if (extensionConfig.getAfterSaveComponent() != null) {
                this.afterSaveComponent = extensionConfig.getAfterSaveComponent();
            }
        }
        init();
    }

    private void init() {
        if (beforeSaveComponent != null) {
            ((FormBeforeSaveInterceptor) applicationContext.getBean(beforeSaveComponent)).beforeSave(formState);
        }
        final WidgetConfigurationConfig widgetConfigurationConfig = formConfig.getWidgetConfigurationConfig();
        widgetConfigs = new ArrayList<>(widgetConfigurationConfig.getWidgetConfigList().size() / 2);
        widgetConfigsById = new CaseInsensitiveHashMap<>(widgetConfigurationConfig.getWidgetConfigList().size());
        for (WidgetConfig config : widgetConfigurationConfig.getWidgetConfigList()) {
            widgetConfigsById.put(config.getId(), config);
            if (formState.getWidgetState(config.getId()) != null && !config.isReadOnly() && !(getWidgetHandler(config) instanceof SelfManagingWidgetHandler)) {
                widgetConfigs.add(config);
            }
        }

        // this sort is very important - in order to resolve a situation like this:
        // widget 1, field-path: capital (a combo-box, allowing to choose city)
        // widget 2, field-path: capital.name (a text-box, allowing to chose capital name)
        // so, when algorithm gets to capital.name (after capital), it will be able to find ID chosen in capital field and "attach" to it
        Collections.sort(widgetConfigs, new WidgetConfigComparator());
    }

    public DomainObject saveForm() {
        DomainObject rootDomainObject = formObjects.getRootNode().getDomainObject();
        if (rootDomainObject.isNew()) {
            toCreate.put(FieldPath.ROOT, new ObjectWithReferences(FieldPath.ROOT, new HashMap<String, FieldPath>(0)));
        }
        ArrayList<WidgetContext> multiBackReferenceContexts = new ArrayList<>();
        ArrayList<WidgetContext> directReferenceContexts = new ArrayList<>();
        for (WidgetConfig widgetConfig : widgetConfigs) {


                if(!widgetConfig.isPersist()){
                    continue;
                }


            WidgetState widgetState = formState.getWidgetState(widgetConfig.getId());
            if (widgetState.mayContainNestedFormStates()) {
                // process nested form states of already created objects which has been edited
                // new objects are processed in method saveNewLinkedObjects();
                LinkedHashMap<String, FormState> nestedFormStates = widgetState.getEditedNestedFormStates();
                Set<Map.Entry<String, FormState>> entries = nestedFormStates.entrySet();
                for (Map.Entry<String, FormState> entry : entries) {
                    FormState nestedFormState = entry.getValue();
                    FormSaver formSaver = (FormSaver) applicationContext.getBean("formSaver");
                    formSaver.setContext(nestedFormState, null);
                    formSaver.saveForm();
                }
            }

            WidgetContext context = new WidgetContext(widgetConfig, formObjects);
            final FieldPath firstFieldPath = context.getFirstFieldPath();
            if (firstFieldPath.isMultiBackReference()) {
                multiBackReferenceContexts.add(context);
                continue;
            }

            if (fieldPathHelper.isDirectReference(rootDomainObject.getTypeName(), firstFieldPath)) {
                directReferenceContexts.add(context);
            }

            Value newValue = getWidgetHandler(widgetConfig).getValue(widgetState);
            Value oldValue = context.getValue();
            FieldPath parentObjectPath = firstFieldPath.getParentPath();
            findCreateAndUpdateReferencesOperationsForChain(new ObjectWithReferences(parentObjectPath));
            if (!areValuesSemanticallyEqual(newValue, oldValue)) {
                toUpdate.add(parentObjectPath);
                formObjects.setFieldValue(firstFieldPath, newValue);
            }
        }

        for (FieldPath forcedValueFieldPath : forcedRootDomainObjectValues.keySet()) {
            formObjects.setFieldValue(forcedValueFieldPath, forcedRootDomainObjectValues.get(forcedValueFieldPath));
        }


        saveRootWithDirectlyLinkedObjects();
        saveNewOneToOneObjects(directReferenceContexts);
        changeMultiBackReferences(multiBackReferenceContexts, rootDomainObject.isNew());
        saveNewBackReferencedObjects(multiBackReferenceContexts);

        DomainObject savedRootObject = formObjects.getRootNode().getDomainObject(); // after save its ID may be changed
        if (afterSaveComponent != null) {
            savedRootObject = ((FormAfterSaveInterceptor) applicationContext.getBean(afterSaveComponent)).afterSave(formState, widgetConfigsById);
            ((SingleObjectNode) formObjects.getNode(FieldPath.ROOT)).setDomainObject(savedRootObject);
        }
        return savedRootObject;
    }

    private void pushWidgetsAfterSave() {
        for (WidgetConfig widgetConfig : widgetConfigsById.values()) {
            final WidgetHandler handler = getWidgetHandler(widgetConfig);
            handler.afterFormSave(formState, widgetConfig);
        }
    }

    private void changeMultiBackReferences(ArrayList<WidgetContext> multiBackReferenceContexts, boolean isNew) {
        for (WidgetContext context : multiBackReferenceContexts) {
            // todo get rid of deleteEntriesOnLinkDrop - substitute with field-path config on-delete
            // what about single choice in widgets???
            final WidgetConfig widgetConfig = context.getWidgetConfig();
            LinkEditingWidgetState widgetState = (LinkEditingWidgetState) formState.getWidgetState(widgetConfig.getId());
            final WidgetHandler handler = getWidgetHandler(widgetConfig);
            boolean deleteEntriesOnLinkDrop = ((LinkEditingWidgetHandler) handler).deleteEntriesOnLinkDrop(widgetConfig);
            HashMap<FieldPath, ArrayList<Id>> fieldPathsIds = getBackReferenceFieldPathsIds(context.getFieldPaths(), widgetState);
            for (FieldPath fieldPath : context.getFieldPaths()) {
                final String linkerBeanName = fieldPath.isOneToManyReference() ? "oneToManyLinker" : "manyToManyLinker";
                ObjectsLinker linker = (ObjectsLinker) applicationContext.getBean(linkerBeanName);
                ArrayList<Id> currentIds = fieldPathsIds.get(fieldPath);

                MultiObjectNode multiObjectsNode = (MultiObjectNode) formState.getObjects().getNode(fieldPath);
                if (isNew) {
                    removeIntermediateObjectsForDefaults(multiObjectsNode);
                }
                linker.setContext(formState, context, fieldPath, currentIds, deleteEntriesOnLinkDrop, savedObjectsById);
                linker.updateLinkedObjects();
            }
        }
    }

    private void removeIntermediateObjectsForDefaults(MultiObjectNode multiObjectNode) {
        Iterator<DomainObject> iterator = multiObjectNode.getDomainObjects().iterator();
        while (iterator.hasNext()) {
            DomainObject next = iterator.next();
            if (next.getId() == null) {
                iterator.remove();
            }
        }
    }

    private void saveNewOneToOneObjects(ArrayList<WidgetContext> directReferenceContexts) {
        for (final WidgetContext context : directReferenceContexts) {
            final WidgetConfig config = context.getWidgetConfig();
            WidgetHandler widgetHandler = getWidgetHandler(config);
            final List<DomainObject> newLinkedObjects = widgetHandler.saveNewObjects(context, formState.getWidgetState(config.getId()));
            if (newLinkedObjects == null) {
                continue;
            }
            final FieldPath firstFieldPath = context.getFirstFieldPath();
            for (DomainObject newObject : newLinkedObjects) { // actually, there should be maximum a single object from such widgets, it's for the future
                formObjects.setFieldValue(firstFieldPath, new ReferenceValue(newObject.getId()));
                toUpdate.add(firstFieldPath.getParentPath());
            }
        }
    }

    private void saveNewBackReferencedObjects(ArrayList<WidgetContext> multiBackReferenceContexts) { // todo: handle on link as well
        for (final WidgetContext context : multiBackReferenceContexts) {
            final WidgetConfig config = context.getWidgetConfig();
            WidgetState widgetState = formState.getWidgetState(config.getId());
            if (widgetState == null) { // ignore - such data shouldn't be saved
                continue;
            }
            WidgetHandler widgetHandler = getWidgetHandler(config);
            if (!(widgetHandler instanceof LinkEditingWidgetHandler)) {
                continue;
            }
            widgetHandler.saveNewObjects(context, widgetState);
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

    private void saveRootWithDirectlyLinkedObjects() {
        ArrayList<ObjectWithReferences> creationOperations = new ArrayList<>(toCreate.values());
        creationOperations = ObjectWithReferences.sortForSave(creationOperations);

        // 1) Save (update form object node (substitute) while saving )
        // 2) Merge reference updates of existing objects with regular updates
        // 3) update!
        for (ObjectWithReferences operation : creationOperations) {
            final DomainObject domainObject = getSingleDomainObject(operation.path);
            for (String refFieldName : operation.references.keySet()) {
                final DomainObject reference = getSingleDomainObject(operation.references.get(refFieldName));
                domainObject.setReference(refFieldName, reference);
            }
            saveDomainObject(operation.path);
        }

        for (FieldPath fieldPath : toUpdateReferences.keySet()) {
            final ObjectWithReferences object = toUpdateReferences.get(fieldPath);
            final DomainObject domainObject = getSingleDomainObject(object.path);
            for (String refFieldName : object.references.keySet()) {
                final DomainObject reference = getSingleDomainObject(object.references.get(refFieldName));
                domainObject.setReference(refFieldName, reference);
            }
            if (object.references.size() != 0) {
                toUpdate.add(fieldPath);
            }
        }
        toUpdate.removeAll(toCreate.keySet());
        // todo: sort update operation in some stable order (for example, by domain object type name) in order to avoid deadlocks
        for (FieldPath fieldPath : toUpdate) {
            saveDomainObject(fieldPath);
        }
    }

    private void saveDomainObject(FieldPath fieldPath) {
        final DomainObject domainObject = crudService.save(getSingleDomainObject(fieldPath));
        ((SingleObjectNode) formObjects.getNode(fieldPath)).setDomainObject(domainObject);
    }

    private void findCreateAndUpdateReferencesOperationsForChain(ObjectWithReferences object) {
        // let's say we are pointing to a.b.c.d (objects, not values)
        // it means 1:1 relationship
        // a->b exists, b->c empty, c->d empty
        // 1) create d and put it into a.b.c.d
        // 2) create c and put it into a.b.c
        // 3) as b exists, that's all
        SingleObjectNode node = (SingleObjectNode) formObjects.getNode(object.path);
        if (!node.isEmpty()) { // root node is never empty - thus operation of its creation is not created in this method
            return;
        }

        // in case this is direct 1:1 reference, check parent object for its value. if it's there - use that object instead
        DomainObject domainObject = findOrCreateNewObjectInChain(object, node.getType());
        node.setDomainObject(domainObject);

        object.fillParentReference();
        toCreate.put(object.path, object);
        // ref to this will have to be updated in parent objects

        SingleObjectNode parentNode = (SingleObjectNode) formObjects.getNode(object.path.getParentPath());
        if (parentNode.isEmpty()) {
            findCreateAndUpdateReferencesOperationsForChain(object.getParentReferencingThis());
            return;
        }

        if (object.path.isOneToOneBackReference()) {
            return; // nothing to do more for back links (nothing to update in existing parent)
        }

        // but for direct links parent object references to this should be updated
        FieldPath parentPath = object.path.getParentPath();
        String fieldNameInParent = object.path.getFieldName();
        final ObjectWithReferences parentToCreate = toCreate.get(parentPath);
        if (parentToCreate != null) { // parent node ref to this node should be updated
            parentToCreate.addReference(fieldNameInParent, object.path);
            return;
        }

        // parent node is not empty and no creation operation for it exists - it means this parent contains existing object which
        // references should also be updated
        ObjectWithReferences parentToUpdateReferencesIn = toUpdateReferences.get(parentPath);
        if (parentToUpdateReferencesIn == null) {
            parentToUpdateReferencesIn = new ObjectWithReferences(parentPath);
            toUpdateReferences.put(parentPath, parentToUpdateReferencesIn);
        }
        parentToUpdateReferencesIn.addReference(fieldNameInParent, object.path);
    }

    private DomainObject findOrCreateNewObjectInChain(ObjectWithReferences object, String objectToCreateType) {
        if (!object.path.isOneToOneDirectReference()) {
            return crudService.createDomainObject(objectToCreateType);
        }

        final DomainObject parentDomainObject = getSingleDomainObject(object.path.getParentPath());
        if (parentDomainObject == null) {
            return crudService.createDomainObject(objectToCreateType);
        }

        Id id = parentDomainObject.getReference(object.path.getFieldName());
        return id == null ? crudService.createDomainObject(objectToCreateType) : crudService.find(id);
    }

    private HashMap<FieldPath, ArrayList<Id>> getBackReferenceFieldPathsIds(FieldPath[] fieldPaths,
                                                                            LinkEditingWidgetState widgetState) {
        HashMap<FieldPath, ArrayList<Id>> result = new HashMap<>();
        for (FieldPath fieldPath : fieldPaths) {
            result.put(fieldPath, new ArrayList<Id>());
        }
        List<Id> ids = widgetState.getIds();
        if (ids == null) {
            return result;
        }
        for (Id id : ids) {
            final String domainObjectType = crudService.getDomainObjectType(id);
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

                if (configurationExplorer.isAssignable(domainObjectType, linkedType)) {
                    result.get(fieldPath).add(id);
                    break;
                }
            }
        }
        return result;
    }

    private DomainObject getSingleDomainObject(FieldPath fieldPath) {
        return ((SingleObjectNode) formObjects.getNode(fieldPath)).getDomainObject();
    }

    private static FieldPath[] getFieldPaths(WidgetConfig widgetConfig) {
        return FieldPath.createPaths(widgetConfig.getFieldPathConfig().getValue());
    }

    private static class WidgetConfigComparator implements Comparator<WidgetConfig> {

        @Override
        public int compare(WidgetConfig o1, WidgetConfig o2) {
            final FieldPath fieldPath1 = getFieldPaths(o1)[0];
            final FieldPath fieldPath2 = getFieldPaths(o2)[0];
            return fieldPath1.compareTo(fieldPath2);
        }
    }
}
