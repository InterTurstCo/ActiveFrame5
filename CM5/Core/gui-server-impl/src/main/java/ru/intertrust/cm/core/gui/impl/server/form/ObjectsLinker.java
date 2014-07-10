package ru.intertrust.cm.core.gui.impl.server.form;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.gui.form.widget.FieldPathConfig;
import ru.intertrust.cm.core.config.gui.form.widget.OnLinkConfig;
import ru.intertrust.cm.core.config.gui.form.widget.OnUnlinkConfig;
import ru.intertrust.cm.core.gui.api.server.form.DomainObjectLinkInterceptor;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.SingleObjectNode;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @author Denis Mitavskiy
 *         Date: 23.06.2014
 *         Time: 22:11
 */
public abstract class ObjectsLinker {
    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    protected ConfigurationExplorer configurationExplorer;
    @Autowired
    protected CrudService crudService;
    @Autowired
    protected AttachmentService attachmentService;

    protected FormState formState;
    protected DomainObject parentObject;
    protected WidgetContext widgetContext;
    protected FieldPath fieldPath;
    protected ArrayList<Id> currentIds;
    protected boolean deleteEntriesOnLinkDrop;
    protected HashMap<Id, DomainObject> savedObjectsCache;
    protected FieldPathConfig fieldPathConfig;
    protected String linkToParentName;
    protected DomainObjectLinkInterceptor linkInterceptor;

    public ObjectsLinker(FormState formState, WidgetContext widgetContext, FieldPath fieldPath, ArrayList<Id> currentIds, boolean deleteEntriesOnLinkDrop, HashMap<Id, DomainObject> savedObjectsCache) {
        this.formState = formState;
        this.parentObject = ((SingleObjectNode) formState.getObjects().getNode(fieldPath.getParentPath())).getDomainObject();
        this.widgetContext = widgetContext;
        this.fieldPath = fieldPath;
        this.currentIds = currentIds == null ? new ArrayList<Id>(0) : currentIds;
        this.deleteEntriesOnLinkDrop = deleteEntriesOnLinkDrop;
        this.savedObjectsCache = savedObjectsCache;
        this.fieldPathConfig = widgetContext.getFieldPathConfig();
        this.linkToParentName = fieldPath.getLinkToParentName();
    }

    @PostConstruct
    private void init() {
        this.linkInterceptor =  getLinkInterceptor();
    }

    public abstract void updateLinkedObjects();

    protected void linkObjects(HashSet<Id> previousIds) {
        ArrayList<Id> idsToLink = new ArrayList<>();
        for (Id id : currentIds) {
            if (!previousIds.contains(id)) {
                idsToLink.add(id);
            }
        }
        link(idsToLink);
    }

    protected abstract void link(ArrayList<Id> idsToLink);

    protected DomainObject save(DomainObject object) {
        // this is required to avoid optimistic lock exceptions when same object is being edited by several widgets,
        // for example, one widget is editing object's properties while the other edits links
        final Id id = object.getId();
        DomainObject earlierSavedObject = savedObjectsCache.get(id);
        if (earlierSavedObject != null) {
            // todo merge objects here
            return earlierSavedObject;
        }
        if (id != null && isAttachment(id)) { // attachments should never be saved again - they're "final"
            return object;
        }
        DomainObject savedObject = crudService.save(object);
        savedObjectsCache.put(savedObject.getId(), savedObject);
        return savedObject;
    }

    protected void delete(Id id) {
        if (isAttachment(id)) {
            attachmentService.deleteAttachment(id);
        } else {
            crudService.delete(id);
        }
    }

    protected boolean isAttachment(Id id) {
        return configurationExplorer.isAttachmentType(crudService.getDomainObjectType(id));
    }

    protected DomainObjectLinkInterceptor getLinkInterceptor() {
        final String linker = fieldPathConfig.getDomainObjectLinker();
        if (linker != null) {
            return (DomainObjectLinkInterceptor) applicationContext.getBean(linker);
        }
        final OnLinkConfig onLinkConfig = fieldPathConfig.getOnLinkConfig();
        final OnUnlinkConfig onUnlinkConfig = fieldPathConfig.getOnUnlinkConfig();
        if (onLinkConfig == null && onUnlinkConfig == null) {
            return null; // nothing to do before
        }
        return (DomainObjectLinkInterceptor) applicationContext.getBean("defaultConfigurableDomainObjectLinkInterceptor");
    }

}
