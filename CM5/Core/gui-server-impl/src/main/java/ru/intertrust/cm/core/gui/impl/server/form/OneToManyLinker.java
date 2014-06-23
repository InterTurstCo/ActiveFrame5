package ru.intertrust.cm.core.gui.impl.server.form;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.gui.api.server.form.DomainObjectLinkContext;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.MultiObjectNode;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @author Denis Mitavskiy
 *         Date: 23.06.2014
 *         Time: 18:56
 */
public class OneToManyLinker extends ObjectsLinker {
    private boolean isNotNullReference;

    public OneToManyLinker(FormState formState, WidgetContext widgetContext, FieldPath fieldPath, ArrayList<Id> currentIds, boolean deleteEntriesOnLinkDrop, HashMap<Id, DomainObject> savedObjectsCache) {
        super(formState, widgetContext, fieldPath, currentIds, deleteEntriesOnLinkDrop, savedObjectsCache);
    }

    @PostConstruct
    private void init() {
        isNotNullReference = isOneToOneNotNullReference();
    }

    public void updateLinkedObjects() {
        ArrayList<DomainObject> previousState = ((MultiObjectNode) widgetContext.getFormObjects().getNode(fieldPath)).getDomainObjects();
        if (previousState == null) {
            previousState = new ArrayList<>(0);
        }

        HashSet<Id> previousIds = new HashSet<>(previousState.size());
        for (DomainObject previousStateObject : previousState) {
            final Id prevId = previousStateObject.getId();
            if (prevId != null) {
                previousIds.add(prevId);
            }
        }

        // links to create
        linkObjects(previousIds);

        // links to drop
        previousIds.removeAll(currentIds); // leave only those which aren't in new IDs
        unlink(previousIds);
    }

    protected void link(ArrayList<Id> idsToLink) {
        for (Id id : idsToLink) {
            DomainObject objectToLink = crudService.find(id); // todo: optimize with batch operations
            if (linkInterceptor != null) {
                final DomainObjectLinkContext context = new DomainObjectLinkContext(formState, parentObject, objectToLink, widgetContext, fieldPath);
                final boolean doLink = linkInterceptor.beforeLink(context);
                if (!doLink) {
                    continue;
                }
            }
            objectToLink.setReference(linkToParentName, parentObject.getId());
            save(objectToLink);
        }
    }

    private void unlink(HashSet<Id> idsToUnlink) {
        for (Id id : idsToUnlink) {
            DomainObject objectToUnlink = crudService.find(id); // todo optimize
            if (linkInterceptor != null) {
                final DomainObjectLinkContext context = new DomainObjectLinkContext(formState, parentObject, objectToUnlink, widgetContext, fieldPath);
                final boolean doUnlink = linkInterceptor.beforeUnlink(context);
                if (!doUnlink) {
                    continue;
                }
            }

            // do unlink
            if (isNotNullReference) {
                delete(id);
                continue;
            }
            if (deleteEntriesOnLinkDrop) {
                delete(id);
                continue;
            }
            objectToUnlink.setReference(linkToParentName, (Id) null);
            save(objectToUnlink); // todo: optimize? is it possible?
        }
    }

    private boolean isOneToOneNotNullReference() {
        if (!fieldPath.isOneToManyReference()) {
            return false;
        }
        final String type = fieldPath.getReferenceType();
        final String fieldName = fieldPath.getLinkToParentName();
        final ReferenceFieldConfig fieldConfig = (ReferenceFieldConfig) configurationExplorer.getFieldConfig(type, fieldName);
        return fieldConfig.isNotNull();
    }
}
