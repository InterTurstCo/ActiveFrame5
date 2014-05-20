package ru.intertrust.cm.core.gui.impl.server.form;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.FieldPathConfig;
import ru.intertrust.cm.core.config.gui.form.widget.LabelConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.gui.model.form.*;

import java.util.*;

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
 * <field-path on-delete="cascade/unlink" - modifies default behavior/>
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
    private String userUid;

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
        this.formObjects = formRetriever.getForm(id).getFormState().getObjects();
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
            findDeleteOperationsForChain(new ObjectWithReferences(fieldPath.getParentPath()), fieldPathConfigs.get(fieldPath).getOnDelete());
        }

        deleteMultiBackReferences(multiBackReferencesToDelete);
        deleteRootWithOneToOneChain();
    }

    private void buildCaches() {
        fieldPathConfigs = new HashMap<>();
        List<WidgetConfig> widgetConfigs = formConfig.getWidgetConfigurationConfig().getWidgetConfigList();
        for (WidgetConfig config : widgetConfigs) {
            FieldPathConfig fieldPathConfig = config.getFieldPathConfig();
            if (fieldPathConfig == null || fieldPathConfig.getValue() == null || config instanceof LabelConfig) {
                continue;
            }

            // field path config can point to multiple paths
            FieldPath[] fieldPaths = FieldPath.createPaths(fieldPathConfig.getValue());
            for (FieldPath fieldPath : fieldPaths) {
                fieldPathConfigs.put(fieldPath, fieldPathConfig);
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
            if (fieldPath.isOneToManyReference()) {
                deleteOneToManyReferences(fieldPath);
            } else {
                deleteManyToManyReferences(fieldPath);
            }
        }
    }

    private void deleteOneToManyReferences(FieldPath fieldPath) {
        String refFieldName = fieldPath.getLinkToParentName();
        String refType = fieldPath.getReferenceType();
        if (configurationExplorer.isAttachmentType(refType)) {
            deleteAttachments();
            return;
        }

        List<DomainObject> linkedDomainObjects = crudService.findLinkedDomainObjects(formObjects.getRootDomainObject().getId(), refType, refFieldName);
        FieldPathConfig.OnDeleteAction action = fieldPathConfigs.get(fieldPath).getOnDelete();
        for (DomainObject object : linkedDomainObjects) {
            if (action == null || action == FieldPathConfig.OnDeleteAction.CASCADE) {
                crudService.delete(object.getId());
                continue;
            }

            if (action == FieldPathConfig.OnDeleteAction.UNLINK) {
                object.setReference(refFieldName, (Id) null);
                crudService.save(object);
            }
        }
    }

    private void deleteAttachments() {
        final List<DomainObject> attachments = attachmentService.findAttachmentDomainObjectsFor(formObjects.getRootDomainObject().getId());
        for (DomainObject attachment : attachments) {
            attachmentService.deleteAttachment(attachment.getId());
        }
    }

    private void deleteManyToManyReferences(FieldPath fieldPath) {
        String refFieldName = fieldPath.getLinkToParentName();
        String refType = fieldPath.getReferenceType();
        String linkToChildrenName = fieldPath.getLinkToChildrenName();
        final List<DomainObject> intermediateDomainObjects = crudService.findLinkedDomainObjects(formObjects.getRootDomainObject().getId(), refType, refFieldName);
        FieldPathConfig.OnDeleteAction action = fieldPathConfigs.get(fieldPath).getOnDelete();
        for (DomainObject intermediateObject : intermediateDomainObjects) {
            crudService.delete(intermediateObject.getId()); // unlinking
            if (action == FieldPathConfig.OnDeleteAction.CASCADE) { // default behavior is unlink only
                final DomainObject domainObject = crudService.find(intermediateObject.getReference(linkToChildrenName));
                crudService.delete(domainObject.getId());
            }
        }
    }

    private DomainObject getSingleDomainObject(FieldPath fieldPath) {
        return ((SingleObjectNode) formObjects.getNode(fieldPath)).getDomainObject();
    }
}
