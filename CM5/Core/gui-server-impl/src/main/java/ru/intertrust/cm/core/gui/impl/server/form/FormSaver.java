package ru.intertrust.cm.core.gui.impl.server.form;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfigurationConfig;
import ru.intertrust.cm.core.gui.api.server.widget.LinkEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.FormObjects;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.SingleObjectNode;
import ru.intertrust.cm.core.gui.model.form.widget.LinkEditingWidgetState;
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
    private Map<FieldPath, Value> forcedRootDomainObjectValues;
    private FormObjects formObjects;
    private List<WidgetConfig> widgetConfigs;
    private HashMap<FieldPath, ObjectWithReferences> toCreate;
    private HashMap<FieldPath, ObjectWithReferences> toUpdateReferences;
    private HashSet<FieldPath> toUpdate;
    private HashMap<Id, DomainObject> savedObjectsById;

    public FormSaver(FormState formState, Map<FieldPath, Value> forcedRootDomainObjectValues) {
        this.formState = formState;
        this.formObjects = formState.getObjects();
        this.forcedRootDomainObjectValues = forcedRootDomainObjectValues == null ? new HashMap<FieldPath, Value>(0) : forcedRootDomainObjectValues;
        toCreate = new HashMap<>();
        toUpdateReferences = new HashMap<>();
        toUpdate = new HashSet<>();
        savedObjectsById = new HashMap<>();
    }

    @PostConstruct
    private void init() {
        FormConfig formConfig = configurationExplorer.getConfig(FormConfig.class, formState.getName());
        WidgetConfigurationConfig widgetConfigurationConfig = formConfig.getWidgetConfigurationConfig();
        widgetConfigs = new ArrayList<>(widgetConfigurationConfig.getWidgetConfigList().size() / 2);
        for (WidgetConfig config : widgetConfigurationConfig.getWidgetConfigList()) {
            if (formState.getWidgetState(config.getId()) != null && !config.isReadOnly()) { // ignore empty - such data shouldn't be saved
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
        for (WidgetConfig widgetConfig : widgetConfigs) {
            WidgetState widgetState = formState.getWidgetState(widgetConfig.getId());
            if (widgetState.mayContainNestedFormStates()) {
                // process nested form states of already created objects which has been edited
                // new objects are processed in method saveNewLinkedObjects();
                LinkedHashMap<String, FormState> nestedFormStates = widgetState.getEditedNestedFormStates();
                Set<Map.Entry<String, FormState>> entries = nestedFormStates.entrySet();
                for (Map.Entry<String, FormState> entry : entries) {
                    FormState nestedFormState = entry.getValue();
                    FormSaver formSaver = (FormSaver) applicationContext.getBean("formSaver", nestedFormState, null);
                    formSaver.saveForm();
                }
            }

            WidgetContext context = new WidgetContext(widgetConfig, formObjects);
            final FieldPath firstFieldPath = context.getFirstFieldPath();
            if (firstFieldPath.isMultiBackReference()) {
                multiBackReferenceContexts.add(context);
                continue;
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

        changeMultiBackReferences(multiBackReferenceContexts);

        saveNewLinkedObjects();
        return formObjects.getRootNode().getDomainObject(); // after save its ID may be changed
    }

    private void changeMultiBackReferences(ArrayList<WidgetContext> multiBackReferenceContexts) {
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
                ObjectsLinker linker = (ObjectsLinker) applicationContext.getBean(linkerBeanName, formState, context, fieldPath, fieldPathsIds.get(fieldPath), deleteEntriesOnLinkDrop, savedObjectsById);
                linker.updateLinkedObjects();
            }
        }
    }

    private void saveNewLinkedObjects() { // todo: handle on link as well
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
