package ru.intertrust.cm.core.gui.impl.server.form;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.FieldPathConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;
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
public class FormObjectsRemover {
    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    private ConfigurationExplorer configurationExplorer;
    @Autowired
    private CrudService crudService;
    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private FormResolver formResolver;
    @Autowired
    private FormRetriever formRetriever;

    private FormConfig formConfig;
    private FormState initialFormState;
    private FormObjects formObjects;
    private HashMap<FieldPath, ObjectWithReferences> rootAndOneToOneToDelete;
    private HashSet<FieldPath> oneToOneToUnlink;

    private HashMap<FieldPath, FieldPathConfig> fieldPathConfigs;
    private HashMap<FieldPath, WidgetState> fieldPathStates;
    private String userUid;
    private boolean attachmentsDeleted;

    public FormObjectsRemover(String userUid) {
        this.userUid = userUid;
        this.rootAndOneToOneToDelete = new HashMap<>();
        this.oneToOneToUnlink = new HashSet<>();
    }

    public void deleteForm(FormState initialFormState) {
        this.formConfig = configurationExplorer.getConfig(FormConfig.class, initialFormState.getName());
        this.initialFormState = initialFormState;
        this.formObjects = initialFormState.getObjects();
        deleteForm();
    }

    public void deleteForm(Id id) {
        this.formConfig = formResolver.findEditingFormConfig(id, userUid);
        this.initialFormState = formRetriever.getForm(id).getFormState();
        this.formObjects = initialFormState.getObjects();
        deleteForm();
    }

    private void deleteForm() {
        buildCaches();

        ArrayList<FieldPath> multiBackReferencesToDelete = new ArrayList<>();
        for (FieldPath fieldPath : fieldPathConfigs.keySet()) {
            if (fieldPath.isMultiBackReference()) {
                multiBackReferencesToDelete.add(fieldPath);
                continue;
            }

            // delete chain
            findDeleteOperationsForChain(new ObjectWithReferences(fieldPath.getParentPath()), fieldPathConfigs.get(fieldPath).getOnRootDelete());
        }

        deleteMultiBackReferences(multiBackReferencesToDelete);
        deleteRootWithOneToOneChain();
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
            attachmentService.deleteAttachment(attachment.getId());
        }
        attachmentsDeleted = true;
    }

    private DomainObject getSingleDomainObject(FieldPath fieldPath) {
        return ((SingleObjectNode) formObjects.getNode(fieldPath)).getDomainObject();
    }
}
