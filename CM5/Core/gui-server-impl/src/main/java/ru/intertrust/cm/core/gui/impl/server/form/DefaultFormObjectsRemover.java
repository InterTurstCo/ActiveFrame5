package ru.intertrust.cm.core.gui.impl.server.form;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.DomainObjectFieldsConfig;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.FormObjectsRemoverConfig;
import ru.intertrust.cm.core.config.gui.form.OnDeleteConfig;
import ru.intertrust.cm.core.config.gui.form.widget.*;
import ru.intertrust.cm.core.gui.api.server.form.FormObjectsRemover;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.FormObjects;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.SingleObjectNode;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Primary scenarios:
 * a.b.c.d.e - primary scenario - delete all linked objects
 * b|a - delete linked object b
 * b|a.d|b - delete 2 linked objects (b, d)
 * a.b|a - unlink "root->a" (do not delete a)
 * b|a.d - delete b, direct links further - do not delete
 *
 * b^a - delete b (default)
 * b^a.c - unlink (default)
 *
 * Modifiers for removal:
 * "cascade" - all linked objects in field path will be deleted
 * "unlink" -> first child will be unlinked
 *
 * "cascade" -> a.b.c.d.e - objects a,b,c,d will be deleted (by default)
 *              b|a - delete b
 *              b|a.d|b - delete b, delete d
 *              a.b|a.c.d - a, b, c will be deleted (d - field)
 *              b|a.d|b - default behavior -> b, d will be deleted
 * "unlink" -> a.b.c.d.e (nothing will be deleted)
 *             b|a - b will be unlinked
 *             b|a.d|b - b (only) will be unlinked
 *             a.b|a (default -> nothing will be deleted)
 *             b|a.d - b (only) will be unlinked
 *
 * <field-path on-root-delete="cascade/unlink" - modifies default behavior/>
 *
 * @author Denis Mitavskiy
 *         Date: 05.05.14
 *         Time: 13:06
 */
public class DefaultFormObjectsRemover extends FormProcessor implements FormObjectsRemover {
    @Autowired
    protected FormResolver formResolver;
    @Autowired
    protected FormRetriever formRetriever;

    protected FormConfig formConfig;
    protected FormState initialFormState;
    protected FormState currentFormState;
    protected FormObjects formObjects;
    protected HashMap<FieldPath, ObjectWithReferences> rootAndOneToOneToDelete;
    protected HashSet<FieldPath> oneToOneToUnlink;

    protected HashMap<FieldPath, FieldPathConfig> fieldPathConfigs;
    protected HashMap<FieldPath, WidgetState> fieldPathStates;
    protected boolean attachmentsDeleted;

    private HashSet<Id> deletedAttachments;

    public DefaultFormObjectsRemover() {
        this.rootAndOneToOneToDelete = new HashMap<>();
        this.oneToOneToUnlink = new HashSet<>();
    }

    public void deleteForm(FormState currentFormState) {
        this.currentFormState = currentFormState;
        this.formConfig = configurationExplorer.getPlainFormConfig(currentFormState.getName());
        this.initialFormState = formRetriever.getForm(currentFormState.getObjects().getRootDomainObject().getId(), currentFormState.getFormViewerConfig()).getFormState(); //TODO: is it ok to pass null as formViewerConfig param?
        this.formObjects = initialFormState.getObjects();
        deleteForm();
    }

    protected void deleteForm() {
        final boolean doDelete = performBeforeDeleteOperations();
        if (!doDelete) {
            return;
        }

        buildCaches();

        ArrayList<FieldPath> multiBackReferencesToDelete = new ArrayList<>();
        for (FieldPath fieldPath : fieldPathConfigs.keySet()) {
            if (fieldPath.isMultiBackReference()) {
                multiBackReferencesToDelete.add(fieldPath);
                continue;
            }

            findDeleteOperationsForChain(new ObjectWithReferences(fieldPath.getParentPath()), fieldPathConfigs.get(fieldPath).getOnRootDelete());
        }
        final ArrayList<Id> attachmentsReferencedFromRoot = cleanUpRootReferences();
        deleteAttachmentsReferencedFromRoot(attachmentsReferencedFromRoot);
        deleteMultiBackReferences(multiBackReferencesToDelete);
        deleteRootWithOneToOneChain();
    }

    protected boolean performBeforeDeleteOperations() {
        final FormObjectsRemoverConfig formObjectsRemoverConfig = formConfig.getFormObjectsRemoverConfig();
        if (formObjectsRemoverConfig == null) {
            return true;
        }

        final OnDeleteConfig onDelete = formObjectsRemoverConfig.getOnDelete();
        if (onDelete == null) {
            return true;
        }

        final List<OperationConfig> operationConfigs = onDelete.getOperationConfigs();
        boolean saveRoot = false;
        for (OperationConfig operationConfig : operationConfigs) {
            if (operationConfig instanceof UpdateConfig) {
                updateRootObject((UpdateConfig) operationConfig);
                saveRoot = true;
            } else if (operationConfig instanceof CreateConfig) {
                createObject((CreateConfig) operationConfig);
            }
        }

        if (saveRoot) {
            final DomainObject savedRoot = crudService.save(formObjects.getRootDomainObject());
            formObjects.getRootNode().setDomainObject(savedRoot);
        }
        return onDelete.doDelete();
    }

    private void buildCaches() {
        List<WidgetConfig> widgetConfigs = formConfig.getWidgetConfigurationConfig().getWidgetConfigList();
        fieldPathConfigs = new HashMap<>(widgetConfigs.size());
        fieldPathStates = new HashMap<>(widgetConfigs.size());
        for (WidgetConfig config : widgetConfigs) {
            FieldPathConfig fieldPathConfig = config.getFieldPathConfig();
            if (fieldPathConfig == null || fieldPathConfig.getValue() == null || config.isReadOnly()) {
                continue;
            }

            WidgetState widgetState = initialFormState.getWidgetState(config.getId());
            // field path config can point to multiple paths - and for simplicity they all will share the same state - it doesn't matter
            FieldPath[] fieldPaths = FieldPath.createPaths(fieldPathConfig.getValue());
            for (FieldPath fieldPath : fieldPaths) {
                fieldPathConfigs.put(fieldPath, fieldPathConfig);
                fieldPathStates.put(fieldPath, widgetState);
            }
        }
    }

    private void findDeleteOperationsForChain(ObjectWithReferences toDelete, FieldPathConfig.OnDeleteAction onDeleteAction) {
        ObjectWithReferences checkedObject = rootAndOneToOneToDelete.get(toDelete.path);
        if (checkedObject != null) {
            checkedObject.addReferencesFrom(toDelete);
            return;
        }

        toDelete.fillParentReference();

        if (onDeleteAction == null || onDeleteAction == FieldPathConfig.OnDeleteAction.CASCADE) { // default behavior is cascading
            rootAndOneToOneToDelete.put(toDelete.path, toDelete);
        } else {
            oneToOneToUnlink.add(toDelete.path);
            return;
        }

        if (!toDelete.path.isRoot()) { // recursive call for parent objects; passing this object reference in case parent references it directly
            findDeleteOperationsForChain(toDelete.getParentReferencingThis(), FieldPathConfig.OnDeleteAction.CASCADE);
        }
    }

    private ArrayList<Id> cleanUpRootReferences() {
        final DomainObject rootDomainObject = formObjects.getRootDomainObject();
        final DomainObjectTypeConfig typeConfig = configurationExplorer.getDomainObjectTypeConfig(rootDomainObject.getTypeName());
        final DomainObjectFieldsConfig fieldsConfig = typeConfig.getDomainObjectFieldsConfig();
        if (fieldsConfig == null || fieldsConfig.getFieldConfigs() == null) {
            return null;
        }
        ArrayList<Id> attachmentsReferencedFromRoot = new ArrayList<>();
        boolean toSave = false;
        for (FieldConfig fieldConfig : fieldsConfig.getFieldConfigs()) {
            if (fieldConfig.getFieldType() != FieldType.REFERENCE || fieldConfig.isNotNull()) {
                continue;
            }
            final Value value = rootDomainObject.getValue(fieldConfig.getName());
            if (value == null || value.isEmpty()) {
                continue;
            }
            final String type = ((ReferenceFieldConfig) fieldConfig).getType();
            if (configurationExplorer.isAttachmentType(type)) {
                attachmentsReferencedFromRoot.add(((ReferenceValue) value).get());
            }
            rootDomainObject.setValue(fieldConfig.getName(), null);
            toSave = true;
        }
        if (toSave) {
            crudService.save(rootDomainObject);
        }
        return attachmentsReferencedFromRoot;
    }

    private void deleteRootWithOneToOneChain() {
        ObjectWithReferences rootReference = rootAndOneToOneToDelete.get(FieldPath.ROOT);
        if (rootReference == null) {
            rootAndOneToOneToDelete.put(FieldPath.ROOT, new ObjectWithReferences(FieldPath.ROOT));
        }

        // unlink operation at first
        for (FieldPath fieldPath : oneToOneToUnlink) {
            final FieldPath firstChildPath = fieldPath.childrenIterator().next();
            if (!firstChildPath.isOneToOneBackReference()) {
                continue;
            }

            String refFieldName = firstChildPath.getLinkToParentName();
            String refType = firstChildPath.getReferenceType();
            List<DomainObject> linkedDomainObjects = crudService.findLinkedDomainObjects(formObjects.getRootDomainObject().getId(), refType, refFieldName);
            if (!linkedDomainObjects.isEmpty()) { // one-to-one, so the object should be the only
                final DomainObject domainObject = linkedDomainObjects.get(0);
                domainObject.setReference(refFieldName, (Id) null);
                crudService.save(domainObject);
            }
        }

        ArrayList<ObjectWithReferences> toDelete = new ArrayList<>(rootAndOneToOneToDelete.values());
        toDelete = ObjectWithReferences.sortForSave(toDelete);
        for (int i = toDelete.size() - 1; i >= 0; --i) {
            delete(toDelete.get(i));
        }

    }

    private void delete(ObjectWithReferences object) {
        final DomainObject domainObject = getSingleDomainObject(object.path);
        if (domainObject != null) {
            crudService.delete(domainObject.getId()); // todo: change to delete by DO - to support optimistic locking
        }
    }

    private void deleteAttachmentsReferencedFromRoot(ArrayList<Id> ids) {
        deletedAttachments = new HashSet<>(ids);
        for (Id id : ids) {
            attachmentService.deleteAttachment(id);
            deletedAttachments.add(id);
        }
    }

    private void deleteMultiBackReferences(List<FieldPath> fieldPathsWithBackReferences) {
        for (FieldPath fieldPath : fieldPathsWithBackReferences) {
            final FieldPathConfig.OnDeleteAction action = this.fieldPathConfigs.get(fieldPath).getOnRootDelete();
            if (fieldPath.isOneToManyReference()) {
                if (configurationExplorer.isAttachmentType(fieldPath.getReferenceType())) {
                    deleteAttachments();
                    continue;
                }

                final boolean cascadeDelete = isOneToManyCascadeDelete(fieldPath, action);
                if (cascadeDelete) {
                    cascadeDeleteOneToManyReferences(fieldPath);
                    continue;
                } else {
                    unlinkOneToManyReferences(fieldPath);
                    continue;
                }
            }

            // it's many-to-many reference:
            final boolean cascadeDelete = action == FieldPathConfig.OnDeleteAction.CASCADE;
            if (cascadeDelete) {
                cascadeDeleteManyToManyReferences(fieldPath);
            } else {
                unlinkManyToManyReferences(fieldPath);
            }
        }
    }

    private boolean isOneToManyCascadeDelete(FieldPath fieldPath, FieldPathConfig.OnDeleteAction action) {
        final String type = fieldPath.getReferenceType();
        final String fieldName = fieldPath.getLinkToParentName();
        final ReferenceFieldConfig fieldConfig = (ReferenceFieldConfig) configurationExplorer.getFieldConfig(type, fieldName);
        return fieldConfig.isNotNull() || action == null || action == FieldPathConfig.OnDeleteAction.CASCADE;
    }

    private void cascadeDeleteOneToManyReferences(FieldPath fieldPath) {
        if (cascadeDeleteNestedFormStates(fieldPath)) {
            return;
        }

        String refFieldName = fieldPath.getLinkToParentName();
        String refType = fieldPath.getReferenceType();
        List<DomainObject> linkedDomainObjects = crudService.findLinkedDomainObjects(formObjects.getRootDomainObject().getId(), refType, refFieldName);
        for (DomainObject object : linkedDomainObjects) {
            crudService.delete(object.getId());
        }
    }

    private void unlinkOneToManyReferences(FieldPath fieldPath) {
        String refFieldName = fieldPath.getLinkToParentName();
        String refType = fieldPath.getReferenceType();

        List<DomainObject> linkedDomainObjects = crudService.findLinkedDomainObjects(formObjects.getRootDomainObject().getId(), refType, refFieldName);
        for (DomainObject object : linkedDomainObjects) {
            object.setReference(refFieldName, (Id) null);
            crudService.save(object);
        }
    }

    private void cascadeDeleteManyToManyReferences(FieldPath fieldPath) {
        final List<DomainObject> intermediateDomainObjects = unlinkManyToManyReferences(fieldPath);

        if (cascadeDeleteNestedFormStates(fieldPath)) {
            return;
        }

        String linkToChildrenName = fieldPath.getLinkToChildrenName();
        for (DomainObject intermediateObject : intermediateDomainObjects) {
            final DomainObject domainObject = crudService.find(intermediateObject.getReference(linkToChildrenName));
            crudService.delete(domainObject.getId());
        }
    }

    private boolean cascadeDeleteNestedFormStates(FieldPath fieldPath) {
        /*
         todo: this doesn't work - we need a new method WidgetState.getNestedFormStatesExceptNew() returning all form states
         todo: so, sub-cascading is not supported yet

        final WidgetState widgetState = fieldPathStates.get(fieldPath);
        if (widgetState.mayContainNestedFormStates()) { // if not cascade - just unlink (see below)
            for (FormState nestedState : widgetState.getEditedNestedFormStates().values()) {
                ((FormObjectsRemover) applicationContext.getBean("formObjectsRemover", userUid)).deleteForm(nestedState);
            }
            return true;
        }*/
        return false;
    }

    private List<DomainObject> unlinkManyToManyReferences(FieldPath fieldPath) {
        String refFieldName = fieldPath.getLinkToParentName();
        String refType = fieldPath.getReferenceType();
        final List<DomainObject> intermediateDomainObjects = crudService.findLinkedDomainObjects(formObjects.getRootDomainObject().getId(), refType, refFieldName);
        for (DomainObject intermediateObject : intermediateDomainObjects) {
            crudService.delete(intermediateObject.getId()); // unlinking
        }
        return intermediateDomainObjects;
    }

    private void deleteAttachments() { // delete all types of attachments at once
        if (attachmentsDeleted) {
            return;
        }
        final List<DomainObject> attachments = attachmentService.findAttachmentDomainObjectsFor(formObjects.getRootDomainObject().getId());
        for (DomainObject attachment : attachments) {
            if (deletedAttachments.contains(attachment.getId())) {
                continue;
            }
            attachmentService.deleteAttachment(attachment.getId());
        }
        attachmentsDeleted = true;
    }

    private DomainObject getSingleDomainObject(FieldPath fieldPath) {
        return ((SingleObjectNode) formObjects.getNode(fieldPath)).getDomainObject();
    }

    private void updateRootObject(UpdateConfig updateConfig) {
        final List<FieldValueConfig> fieldValueConfigs = updateConfig.getFieldValueConfigs();
        if (fieldValueConfigs == null || fieldValueConfigs.isEmpty()) {
            return;
        }
        setFields(formObjects.getRootDomainObject(), fieldValueConfigs);
    }

    private void createObject(CreateConfig createConfig) {
        DomainObject createdObject = crudService.createDomainObject(createConfig.getType());
        final List<FieldValueConfig> fieldValueConfigs = createConfig.getFieldValueConfigs();
        if (fieldValueConfigs == null || fieldValueConfigs.isEmpty()) {
            return;
        }
        setFields(createdObject, fieldValueConfigs);

        crudService.save(createdObject);
    }

    private void setFields(DomainObject domainObject, List<FieldValueConfig> fieldValueConfigs) {
        ((DomainObjectFieldsSetter) applicationContext.getBean("domainObjectFieldsSetter", domainObject, fieldValueConfigs, formObjects.getRootDomainObject())).setFields();
    }
}
