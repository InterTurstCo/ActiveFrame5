package ru.intertrust.cm.core.gui.impl.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.FieldConfig;
import ru.intertrust.cm.core.config.model.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.model.gui.RoleConfig;
import ru.intertrust.cm.core.config.model.gui.RolesConfig;
import ru.intertrust.cm.core.config.model.gui.UserConfig;
import ru.intertrust.cm.core.config.model.gui.UsersConfig;
import ru.intertrust.cm.core.config.model.gui.form.FormConfig;
import ru.intertrust.cm.core.config.model.gui.form.FormMappingConfig;
import ru.intertrust.cm.core.config.model.gui.form.FormMappingsConfig;
import ru.intertrust.cm.core.config.model.gui.form.widget.FieldPathConfig;
import ru.intertrust.cm.core.config.model.gui.form.widget.LabelConfig;
import ru.intertrust.cm.core.config.model.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.config.model.gui.form.widget.WidgetConfigurationConfig;
import ru.intertrust.cm.core.config.model.gui.navigation.NavigationConfig;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.*;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import java.util.*;

/**
 * Базовая реализация сервиса GUI
 * Author: Denis Mitavskiy
 * Date: 14.06.13
 * Time: 16:14
 */
@Stateless
@Local(GuiService.class)
@Remote(GuiService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class GuiServiceImpl extends AbstractGuiServiceImpl implements GuiService, GuiService.Remote {

    private static Logger log = LoggerFactory.getLogger(GuiServiceImpl.class);

    @Override
    public NavigationConfig getNavigationConfiguration() {
        String navigationPanelName = "panel";
        NavigationConfig navigationConfig = configurationExplorer.getConfig(NavigationConfig.class, navigationPanelName);
        return navigationConfig;
    }

    @Override
    public Dto executeCommand(Command command) {
        ComponentHandler componentHandler = obtainHandler(command.getComponentName());
        if (componentHandler == null) {
            log.warn("handler for component '{}' not found", command.getComponentName());
            return null;
        }
        try {
            return (Dto) componentHandler.getClass().getMethod(command.getName(), Dto.class)
                    .invoke(componentHandler, command.getParameter());
        } catch (NoSuchMethodException e) {
            log.error(e.getMessage(), e);
            throw new GuiException("No command + " + command.getName() + " implemented");
        } catch (Throwable e) {
            e.printStackTrace();
            throw new GuiException("Command can't be executed: " + command.getName());
        }
    }

    @Override
    public FormDisplayData getForm(String domainObjectType) {
        DomainObject root = crudService.createDomainObject(domainObjectType);
        // todo: separate empty form?
        return buildDomainObjectForm(root);
    }

    public FormDisplayData getForm(Id domainObjectId) {
        DomainObject root = crudService.find(domainObjectId);
        if (root == null) {
            throw new GuiException("Object with id: " + domainObjectId.toStringRepresentation() + " doesn't exist");
        }
        return buildDomainObjectForm(root);
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
            FieldPath parentObjectPath = fieldPath.getParent();
            String parentType = formObjects.getObjects(parentObjectPath).getType();
            FieldConfig fieldConfig = configurationExplorer.getFieldConfig(parentType, fieldPath.getLastElement());
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
        return rootDomainObject;
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
        FieldPath parentPath = objectPath.getParent();
        ObjectsNode parentNode = formObjects.getObjects(parentPath);
        String linkToThisPathFromParent = objectPath.getLastElement();
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
        String lastElement = fieldPath.getLastElement();
        String[] typeAndField = lastElement.split("\\^");
        boolean oneToMany = typeAndField.length == 2;
        if (oneToMany) {
            return mergeOneToMay(fieldPath, formObjects, newValues);
        } else {
            return mergeManyToMany(fieldPath, formObjects, newValues);
        }
    }

    private ArrayList<FormSaveOperation> mergeOneToMay(FieldPath fieldPath, FormObjects formObjects,
                                                               ArrayList<Value> newValues) {
        ArrayList<DomainObject> parentObjects = formObjects.getObjects(fieldPath.getParent()).getDomainObjects();
        if (parentObjects.size() > 1) {
            throw new GuiException("Back reference is referencing " + parentObjects.size() + " objects");
        }
        Id parentObjectId = parentObjects.get(0).getId();

        String lastElement = fieldPath.getLastElement();
        String[] typeAndField = lastElement.split("\\^");
        String type = typeAndField[0];
        String field = typeAndField[1];

        ArrayList<DomainObject> previousState = formObjects.getObjects(fieldPath).getDomainObjects();
        if (previousState == null) {
            previousState = new ArrayList<>(0);
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
        String linkField = fieldPath.getLastElement();
        FieldPath mergedNodePath = fieldPath.getParent();
        ObjectsNode mergedNode = formObjects.getObjects(mergedNodePath);
        String linkObjectType = mergedNode.getType();
        FieldPath parentNodePath = mergedNodePath.getParent();
        ArrayList<DomainObject> parentObjects = formObjects.getObjects(parentNodePath).getDomainObjects();
        if (parentObjects.size() > 1) {
            throw new GuiException("Back reference is referencing " + parentObjects.size() + " objects");
        }

        String[] typeAndField = mergedNodePath.getLastElement().split("\\^");
        String rootLinkField = typeAndField[1];

        ArrayList<DomainObject> previousState = mergedNode.getDomainObjects();
        if (previousState == null) {
            previousState = new ArrayList<>(0);
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

    private HashMap<FieldPath, String> getFieldPathWidgetIds(List<WidgetConfig> widgetConfigs) {
        HashMap<FieldPath, String> result = new HashMap<>(widgetConfigs.size());
        for (WidgetConfig config : widgetConfigs) {
            FieldPathConfig fieldPathConfig = config.getFieldPathConfig();
            if (fieldPathConfig == null || fieldPathConfig.getValue() == null) {
                continue;
            }
            FieldPath fieldPath = new FieldPath(fieldPathConfig.getValue());
            result.put(fieldPath, config.getId());
        }
        return result;
    }

    private ArrayList<FieldPath> getFieldPathsPlainTree(Collection<FieldPath> widgetPaths) {

        ArrayList<FieldPath> result = new ArrayList<>(widgetPaths.size());
        for (FieldPath fieldPath : widgetPaths) {
            for (Iterator<FieldPath> subPathIterator = fieldPath.subPathIterator(); subPathIterator.hasNext(); ) {
                FieldPath subPath = subPathIterator.next();
                result.add(subPath);
            }
        }
        return result;
    }

    private FormDisplayData buildDomainObjectForm(DomainObject root) {
        // todo validate that constructions like A^B.C.D aren't allowed or A.B^C
        // allowed are such definitions only:
        // a.b.c.d - direct links
        // a^b - link defining 1:N relationship (widgets changing attributes can't have such field path)
        // a^b.c - link defining N:M relationship (widgets changing attributes can't have such field path)
        FormConfig formConfig = findFormConfig(root);
        List<WidgetConfig> widgetConfigs = formConfig.getWidgetConfigurationConfig().getWidgetConfigList();
        HashMap<String, WidgetState> widgetStateMap = new HashMap<>(widgetConfigs.size());
        HashMap<String, String> widgetComponents = new HashMap<>(widgetConfigs.size());
        FormObjects formObjects = new FormObjects();

        ObjectsNode rootNode = new ObjectsNode(root);
        formObjects.setRootObjects(rootNode);
        for (WidgetConfig config : widgetConfigs) {
            String widgetId = config.getId();
            FieldPathConfig fieldPathConfig = config.getFieldPathConfig();
            if (fieldPathConfig == null || fieldPathConfig.getValue() == null) {
                if (!(config instanceof LabelConfig)) {
                    throw new GuiException("Widget, id: " + widgetId + " is not configured with Field Path");
                }

                //todo refactor
                WidgetContext widgetContext = new WidgetContext(config, formObjects);
                WidgetHandler componentHandler = obtainHandler(config.getComponentName());
                WidgetState initialState = componentHandler.getInitialState(widgetContext);
                widgetStateMap.put(widgetId, initialState);
                widgetComponents.put(widgetId, config.getComponentName());
                continue;
            }
            FieldPath fieldPath = new FieldPath(fieldPathConfig.getValue());

            rootNode = formObjects.getRootObjects();
            for (Iterator<FieldPath> subPathIterator = fieldPath.subPathIterator(); subPathIterator.hasNext(); ) {
                FieldPath subPath = subPathIterator.next();
                String linkPath = subPath.getLastElement();
                if (!subPathIterator.hasNext() && !linkPath.contains("^")) { // it's a field
                    break;
                }

                // it's a reference then
                String childNodeType;
                if (!linkPath.contains("^")) { // direct reference
                    ReferenceFieldConfig fieldConfig = (ReferenceFieldConfig)
                        configurationExplorer.getFieldConfig(rootNode.getType(), linkPath);
                    childNodeType = fieldConfig.getType();
                } else { // back reference
                    childNodeType = linkPath.split("\\^")[0];
                }

                if (formObjects.isObjectsSet(subPath)) {
                    rootNode = formObjects.getObjects(subPath);
                    continue;
                }
                ObjectsNode linkedNode = findLinkedNode(rootNode, childNodeType, linkPath);

                formObjects.setObjects(subPath, linkedNode);
                rootNode.setChild(linkPath, linkedNode);
                rootNode = linkedNode;
            }

            WidgetContext widgetContext = new WidgetContext(config, formObjects);
            WidgetHandler componentHandler = obtainHandler(config.getComponentName());
            WidgetState initialState = componentHandler.getInitialState(widgetContext);
            initialState.setEditable(true);
            widgetStateMap.put(widgetId, initialState);
            widgetComponents.put(widgetId, config.getComponentName());
        }
        FormState formState = new FormState(formConfig.getName(), widgetStateMap, formObjects);
        return new FormDisplayData(formState, formConfig.getMarkup(), widgetComponents, formConfig.getDebug(), true);
    }

    private ObjectsNode findLinkedNode(ObjectsNode parentNode, String linkedType, String linkPath) {
        if (parentNode.isEmpty()) {
            return new ObjectsNode(linkedType, 0);
        }

        if (!linkPath.contains("^")) { // direct link
            ObjectsNode result = new ObjectsNode(linkedType, parentNode.size());
            for (DomainObject domainObject : parentNode) {
                Id linkedObjectId = domainObject.getReference(linkPath);
                if (linkedObjectId != null) {
                    result.add(crudService.find(linkedObjectId)); // it can't be null as reference is set
                }
            }
            return result;
        }

        // it's a "back-link" (like country_best_friend^country)
        String[] domainObjectTypeAndReference = linkPath.split("\\^");
        if (domainObjectTypeAndReference.length != 2) {
            throw new GuiException("Invalid reference: " + linkPath);
        }

        String referenceField = domainObjectTypeAndReference[1];

        // todo after CMFIVE-122 is done - get the first two linked objects, not everything!
        // todo after cardinality functionality is developed, check cardinality (static-check, not runtime)

        ObjectsNode result = new ObjectsNode(linkedType, parentNode.size());
        for (DomainObject domainObject : parentNode) {
            List<DomainObject> linkedDomainObjects = domainObject.getId() == null
                    ? new ArrayList<DomainObject>()
                    : crudService.findLinkedDomainObjects(domainObject.getId(), linkedType, referenceField);
            if (linkedDomainObjects.size() > 1 && parentNode.size() > 1) {
                // join 2 multi-references - not supported and usually doesn't make sense
                throw new GuiException(linkPath + " is resulting into many-on-many join which is not supported");
            }
            for (DomainObject linkedDomainObject : linkedDomainObjects) {
                result.add(linkedDomainObject);
            }
        }

        return result;
    }

    private String getBackReferenceDomainObjectType(String linkPath) {
        // it's a "back-link" (like country_best_friend^country)
        String[] domainObjectTypeAndReference = linkPath.split("\\^");
        if (domainObjectTypeAndReference.length != 2) {
            throw new GuiException("Invalid reference: " + linkPath);
        }

        String linkedDomainObjectType = domainObjectTypeAndReference[0];
        String referenceField = domainObjectTypeAndReference[1];
        FieldConfig fieldConfig = configurationExplorer.getFieldConfig(linkedDomainObjectType, referenceField);
        if (fieldConfig == null) {
            throw new GuiException(linkPath + " is referencing a non-existing type/field");
        }
        if (!(fieldConfig instanceof ReferenceFieldConfig)) {
            throw new GuiException(linkPath + " is not pointing to a reference");
        }
        ReferenceFieldConfig referenceFieldConfig = (ReferenceFieldConfig) fieldConfig;
        return referenceFieldConfig.getType();
    }

    private FormConfig findFormConfig(DomainObject root) {
        // по Id ищется тип доменного объекта
        // далее находится форма для данного контекста, учитывая факт того, переопределена ли форма для пользователя/роли,
        // если флаг "использовать по умолчанию" не установлен
        // в конечном итоге получаем FormConfig

        if (formMappingsCache == null) {
            formMappingsCache = new FormMappingsCache(configurationExplorer);
        }
        String typeName = root.getTypeName();
        String userUid = sessionContext.getCallerPrincipal().getName();
        List<FormConfig> userFormConfigs = formMappingsCache.getUserFormConfigs(userUid, typeName);
        if (userFormConfigs != null && userFormConfigs.size() != 0) {
            if (userFormConfigs.size() > 1) {
                log.warn("There's " + userFormConfigs.size()
                        + " forms defined for Domain Object Type: " + typeName + " and User: " + userUid);
            }
            return userFormConfigs.get(0);
        }

        // todo define strategy of finding a form by role. which role? context role? or may be a static group?
        List<FormConfig> allFormConfigs = formMappingsCache.getAllFormConfigs(typeName);
        if (allFormConfigs == null || allFormConfigs.size() == 0) {
            throw new GuiException("There's no form defined for Domain Object Type: " + typeName);
        }

        FormConfig firstMetForm = allFormConfigs.get(0);
        if (allFormConfigs.size() == 1) {
            return firstMetForm;
        }

        FormConfig defaultFormConfig = formMappingsCache.getDefaultFormConfig(typeName);
        if (defaultFormConfig != null) {
            return defaultFormConfig;
        }

        log.warn("There's no default form defined for Domain Object Type: " + typeName);
        return firstMetForm;
    }

    private static class FormSaveOperation {
        public static enum Type {
            Create,
            Update,
            Delete
        }

        public final Type type;
        public final DomainObject domainObject;
        public final String fieldToSetWithRootReference;

        private FormSaveOperation(Type type, DomainObject domainObject, String fieldToSetWithRootReference) {
            this.type = type;
            this.domainObject = domainObject;
            this.fieldToSetWithRootReference = fieldToSetWithRootReference;
        }
    }

    private static class ObjectCreationOperation implements Comparable<ObjectCreationOperation> {
        public final FieldPath path;
        public final FieldPath parentToUpdateReference;
        public final String parentField;

        private ObjectCreationOperation(FieldPath path, FieldPath parentPathToUpdateReferenceIn, String parentField) {
            this.path = path;
            this.parentToUpdateReference = parentPathToUpdateReferenceIn;
            this.parentField = parentField;
        }

        @Override
        public int compareTo(ObjectCreationOperation o) {
            return -path.compareTo(o.path);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ObjectCreationOperation that = (ObjectCreationOperation) o;

            if (!path.equals(that.path)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            return path.hashCode();
        }
    }

    private static FormMappingsCache formMappingsCache; // todo drop this ugly thing
    private static class FormMappingsCache {
        private HashMap<String, FormConfig> defaultFormByDomainObjectType = new HashMap<>();
        private HashMap<String, List<FormConfig>> allFormsByDomainObjectType = new HashMap<>();
        private HashMap<Pair<String, String>, List<FormConfig>> formsByRoleAndDomainObjectType = new HashMap<>();
        private HashMap<Pair<String, String>, List<FormConfig>> formsByUserAndDomainObjectType = new HashMap<>();

        public FormMappingsCache(ConfigurationExplorer configExplorer) {
            Collection<FormConfig> formConfigs = configExplorer.getConfigs(FormConfig.class);
            if (formConfigs == null) {
                formConfigs = Collections.EMPTY_LIST;
            }
            for (FormConfig formConfig : formConfigs) {
                String domainObjectType = formConfig.getDomainObjectType();
                if (formConfig.isDefault()) {
                    if (defaultFormByDomainObjectType.containsKey(domainObjectType)) {
                        throw new GuiException("There's more than 1 default form for type: " + domainObjectType);
                    }
                    defaultFormByDomainObjectType.put(domainObjectType, formConfig);
                }

                List<FormConfig> domainObjectTypeForms = allFormsByDomainObjectType.get(domainObjectType);
                if (domainObjectTypeForms == null) {
                    domainObjectTypeForms = new ArrayList<>();
                    allFormsByDomainObjectType.put(domainObjectType, domainObjectTypeForms);
                }
                domainObjectTypeForms.add(formConfig);
            }

            Collection<FormMappingConfig> formMappingConfigs = getFormMappingConfigs(configExplorer);
            for (FormMappingConfig formMapping : formMappingConfigs) {
                String domainObjectType = formMapping.getDomainObjectType();
                FormConfig formConfig = configExplorer.getConfig(FormConfig.class, formMapping.getForm());
                fillRoleAndDomainObjectTypeFormMappings(formMapping, domainObjectType, formConfig);
                fillUserAndDomainObjectTypeFormMappings(formMapping, domainObjectType, formConfig);
            }
        }

        public FormConfig getDefaultFormConfig(String domainObjectType) {
            return defaultFormByDomainObjectType.get(domainObjectType);
        }

        public List<FormConfig> getAllFormConfigs(String domainObjectType) {
            return allFormsByDomainObjectType.get(domainObjectType);
        }

        public List<FormConfig> getRoleFormConfigs(String roleName, String domainObjectType) {
            return formsByRoleAndDomainObjectType.get(new Pair<>(roleName, domainObjectType));
        }

        public List<FormConfig> getUserFormConfigs(String userUid, String domainObjectType) {
            return formsByUserAndDomainObjectType.get(new Pair<>(userUid, domainObjectType));
        }

        private Collection<FormMappingConfig> getFormMappingConfigs(ConfigurationExplorer explorer) {
            Collection<FormMappingsConfig> configs = explorer.getConfigs(FormMappingsConfig.class);
            if (configs == null || configs.isEmpty()) {
                return Collections.EMPTY_LIST;
            }
            ArrayList<FormMappingConfig> result = new ArrayList<>();
            for (FormMappingsConfig config : configs) {
                List<FormMappingConfig> formMappings = config.getFormMappingConfigList();
                if (formMappings != null) {
                    result.addAll(formMappings);
                }
            }
            return result;
        }

        private void fillRoleAndDomainObjectTypeFormMappings(FormMappingConfig formMapping, String domainObjectType, FormConfig formConfig) {
            RolesConfig rolesConfig = formMapping.getRolesConfig();
            if (rolesConfig == null) {
                return;
            }
            List<RoleConfig> roleConfigs = rolesConfig.getRoleConfigList();
            if (roleConfigs == null || roleConfigs.size() == 0) {
                return;
            }
            for (RoleConfig roleConfig : roleConfigs) {
                String roleName = roleConfig.getName();
                Pair<String, String> roleAndDomainObjectType = new Pair<>(roleName, domainObjectType);
                List<FormConfig> roleFormConfigs = formsByRoleAndDomainObjectType.get(roleAndDomainObjectType);
                if (roleFormConfigs == null) {
                    roleFormConfigs = new ArrayList<>();
                    formsByRoleAndDomainObjectType.put(roleAndDomainObjectType, roleFormConfigs);
                }
                roleFormConfigs.add(formConfig);
            }
        }

        private void fillUserAndDomainObjectTypeFormMappings(FormMappingConfig formMapping, String domainObjectType, FormConfig formConfig) {
            UsersConfig usersConfig = formMapping.getUsersConfig();
            if (usersConfig == null) {
                return;
            }
            List<UserConfig> userConfigs = usersConfig.getUserConfigList();
            if (userConfigs == null || userConfigs.size() == 0) {
                return;
            }
            for (UserConfig userConfig : userConfigs) {
                String userUid = userConfig.getUid();
                Pair<String, String> userAndDomainObjectType = new Pair<>(userUid, domainObjectType);
                List<FormConfig> userFormConfigs = formsByUserAndDomainObjectType.get(userAndDomainObjectType);
                if (userFormConfigs == null) {
                    userFormConfigs = new ArrayList<>();
                    formsByUserAndDomainObjectType.put(userAndDomainObjectType, userFormConfigs);
                }
                userFormConfigs.add(formConfig);
            }
        }

    }

}
