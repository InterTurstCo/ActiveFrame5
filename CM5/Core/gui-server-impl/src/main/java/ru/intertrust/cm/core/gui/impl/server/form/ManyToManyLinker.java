package ru.intertrust.cm.core.gui.impl.server.form;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.server.form.DomainObjectLinkContext;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.MultiObjectNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @author Denis Mitavskiy
 *         Date: 23.06.2014
 *         Time: 22:41
 */
public class ManyToManyLinker extends ObjectsLinker {
    private String linkedObjectReferenceName;
    private MultiObjectNode intermediateObjectsNode;
    private String intermediateObjectType;

    public ManyToManyLinker(FormState formState, WidgetContext widgetContext, FieldPath fieldPath, ArrayList<Id> currentIds, boolean deleteEntriesOnLinkDrop, HashMap<Id, DomainObject> savedObjectsCache) {
        super(formState, widgetContext, fieldPath, currentIds, deleteEntriesOnLinkDrop, savedObjectsCache);
        linkedObjectReferenceName = fieldPath.getLinkToChildrenName();
        intermediateObjectsNode = (MultiObjectNode) formState.getObjects().getNode(fieldPath);
        intermediateObjectType = intermediateObjectsNode.getType();
    }

    public void updateLinkedObjects() {
        ArrayList<DomainObject> previousIntermediateDOs = intermediateObjectsNode.getDomainObjects();
        if (previousIntermediateDOs == null) {
            previousIntermediateDOs = new ArrayList<>(0);
        }

        HashSet<Id> previousIds = new HashSet<>(previousIntermediateDOs.size());
        HashMap<Id, DomainObject> prevIntermediateDOsByLinkedObjectId = new HashMap<>(previousIntermediateDOs.size());
        for (DomainObject prevInBetweenObject : previousIntermediateDOs) {
            Id linkedObjectId = prevInBetweenObject.getReference(linkedObjectReferenceName);
            if (linkedObjectId != null) {
                previousIds.add(linkedObjectId);
                prevIntermediateDOsByLinkedObjectId.put(linkedObjectId, prevInBetweenObject);
            }
        }

        // links to create
        linkObjects(previousIds);

        // links to drop
        previousIds.removeAll(currentIds); // leave only those which aren't in new values
        unlink(previousIds, prevIntermediateDOsByLinkedObjectId);
    }

    protected void link(ArrayList<Id> idsToLink) {
        for (Id id : idsToLink) {
            if (linkInterceptor != null) {
                final DomainObject objectToLink = crudService.find(id); // todo: optimize with batch operations
                final DomainObjectLinkContext context = new DomainObjectLinkContext(formState, parentObject, objectToLink, widgetContext, fieldPath);
                final boolean doLink = linkInterceptor.beforeLink(context);
                if (!doLink) {
                    continue;
                }
            }
            DomainObject newIntermediateObject = crudService.createDomainObject(intermediateObjectType);
            newIntermediateObject.setReference(linkToParentName, parentObject.getId());
            newIntermediateObject.setReference(linkedObjectReferenceName, id);
            save(newIntermediateObject);
        }
    }

    private void unlink(HashSet<Id> idsToUnlink, HashMap<Id, DomainObject> prevIntermediateDOsByLinkedObjectId) {
        for (Id id : idsToUnlink) {
            if (linkInterceptor != null) {
                DomainObject objectToUnlink = crudService.find(id); // todo optimize
                final DomainObjectLinkContext context = new DomainObjectLinkContext(formState, parentObject, objectToUnlink, widgetContext, fieldPath);
                final boolean doUnlink = linkInterceptor.beforeUnlink(context);
                if (!doUnlink) {
                    continue;
                }
            }

            // do unlink
            final DomainObject intermediateDOToDrop = prevIntermediateDOsByLinkedObjectId.get(id);
            delete(intermediateDOToDrop.getId()); // todo optimize?
            if (deleteEntriesOnLinkDrop) {
                delete(id);
            }
        }
    }
}
