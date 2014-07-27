package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.gui.form.widget.FillParentOnAddConfig;
import ru.intertrust.cm.core.gui.api.server.DomainObjectUpdater;
import ru.intertrust.cm.core.gui.impl.server.util.FilterBuilderUtil;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserUpdaterContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 30.04.14
 *         Time: 13:15
 */
@ComponentName("hierarchy-browser-do-updater")
public class HierarchyBrowserDomainObjectUpdater implements DomainObjectUpdater {
    @Autowired
    private CollectionsService collectionsService;

    @Override
    public void updateDomainObject(DomainObject domainObject, Dto updaterContext) {
        HierarchyBrowserUpdaterContext hierarchyBrowserUpdaterContext = (HierarchyBrowserUpdaterContext) updaterContext;
        FillParentOnAddConfig fillParentOnAddConfig = hierarchyBrowserUpdaterContext.getFillParentOnAddConfig();
        String collection = fillParentOnAddConfig.getCollection();
        Id idToReferWith = hierarchyBrowserUpdaterContext.getIdToReferWith();
        Id realIdToFill = collection == null ? idToReferWith : getIdFromCollection(idToReferWith, fillParentOnAddConfig);
        String fieldToFill = fillParentOnAddConfig.getFieldToFill();
        domainObject.setReference(fieldToFill, realIdToFill);

    }

    private Id getIdFromCollection(Id idToReferWith, FillParentOnAddConfig fillParentOnAddConfig) {
        String collection = fillParentOnAddConfig.getCollection();
        String filterByParentId = fillParentOnAddConfig.getFilterByParentId();
        Filter referenceFilter = FilterBuilderUtil.prepareReferenceFilter(idToReferWith, filterByParentId);
        List<Filter> filters = new ArrayList<>();
        filters.add(referenceFilter);
        IdentifiableObjectCollection identifiableObjects = collectionsService.findCollection(collection, null, filters);
        if (identifiableObjects.size() == 0) {
            return null;
        }
        String collectionField = fillParentOnAddConfig.getCollectionField();
        IdentifiableObject objectToReferWith = identifiableObjects.get(0);
        Id realIdToFill = collectionField == null ? objectToReferWith.getId()
                : objectToReferWith.getReference(collectionField);
        return realIdToFill;
    }
}
