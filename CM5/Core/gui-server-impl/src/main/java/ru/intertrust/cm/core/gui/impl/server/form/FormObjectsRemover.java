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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 05.05.14
 *         Time: 13:06
 */
public class FormObjectsRemover {
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
     * "cascade 1:1 back-links" - all 1:1 back-links will be deleted until the first direct link
     * "unlink" -> first child will be unlinked
     *
     * "cascade" -> a.b.c.d.e - objects a,b,c,d will be deleted (by default)
     *              b|a - delete b
     *              b|a.d|b - delete b, delete d
     *              a.b|a.c.d - a, b, c will be deleted (d - field)
     *              b|a.d|b - default behavior -> b, d will be deleted
     * "unlink" -> a.b.c.d.e (default -> nothing will be deleted)
     *             b|a - b will be unlinked
     *             b|a.d|b - b (only) will be unlinked
     *             a.b|a (default -> nothing will be deleted)
     *             b|a.d - b (only) will be unlinked
     *
     * <field-path on-delete="cascade/unlink" - modifies default behavior/>
     */
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

    private String userUid;

    public FormObjectsRemover(String userUid) {
        this.userUid = userUid;
        this.rootAndOneToOneToDelete = new HashMap<>();
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

    public void deleteForm() {
        List<WidgetConfig> widgetConfigs = formConfig.getWidgetConfigurationConfig().getWidgetConfigList();

        for (WidgetConfig config : widgetConfigs) {
            String widgetId = config.getId();
            FieldPathConfig fieldPathConfig = config.getFieldPathConfig();
            if (fieldPathConfig == null || fieldPathConfig.getValue() == null || config instanceof LabelConfig) {
                continue;
            }

            // field path config can point to multiple paths
            FieldPath[] fieldPaths = FieldPath.createPaths(fieldPathConfig.getValue());
            FieldPath firstFieldPath = fieldPaths[0];
            if (firstFieldPath.isMultiBackReference()) {
                continue; // todo delete
            }

            // delete chain
            for (FieldPath fieldPath : fieldPaths) {
                findDeleteOperationsForChain(new ObjectWithReferences(fieldPath.getParentPath()));
            }
        }

        deleteRootWithOneToOneChain();
    }

    private void findDeleteOperationsForChain(ObjectWithReferences toDelete) {//FieldPath objectPath, Pair<String, FieldPath> reference) {
        ObjectWithReferences checkedObject = rootAndOneToOneToDelete.get(toDelete.path);
        if (checkedObject != null) {
            checkedObject.addReferencesFrom(toDelete);
            return;
        }

        toDelete.fillParentReference();
        rootAndOneToOneToDelete.put(toDelete.path, toDelete);

        if (!toDelete.path.isRoot()) { // recursive call for parent objects; passing this object reference in case parent references it directly
            findDeleteOperationsForChain(toDelete.getParentReferencingThis());
        }
    }

    private void deleteRootWithOneToOneChain() {
        ArrayList<ObjectWithReferences> toDelete = new ArrayList<>(rootAndOneToOneToDelete.values());
        toDelete = ObjectWithReferences.sortForSave(toDelete);
        for (int i = toDelete.size() - 1; i >= 0; --i) {
            delete(toDelete.get(i));
        }
    }

    private void delete(ObjectWithReferences object) {
        final DomainObject domainObject = getSingleDomainObject(object.path);
        if (domainObject != null) {
            crudService.delete(domainObject.getId());
        }
    }

    private DomainObject getSingleDomainObject(FieldPath fieldPath) {
        return ((SingleObjectNode) formObjects.getNode(fieldPath)).getDomainObject();
    }
}
