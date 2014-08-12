package ru.intertrust.cm.core.gui.impl.server.action.system;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.gui.navigation.CollectionViewerConfig;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.impl.server.util.PluginHelper;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.system.CollectionSortOrderActionContext;

import static ru.intertrust.cm.core.gui.model.util.UserSettingsHelper.DO_COLLECTION_VIEWER_FIELD_KEY;

/**
 * @author Sergey.Okolot
 *         Created on 31.07.2014 18:39.
 */
@ComponentName(CollectionSortOrderActionContext.COMPONENT_NAME)
public class CollectionSortOrderActionHandler extends ActionHandler<CollectionSortOrderActionContext, ActionData> {

    @Autowired private CrudService crudService;
    @Autowired private CollectionsService collectionsService;
    @Autowired private CurrentUserAccessor currentUserAccessor;

    @Override
    public ActionData executeAction(CollectionSortOrderActionContext context) {
        if (context.getLink() == null) {
            throw new GuiException("Неизвестный url");
        }
        final DomainObject domainObject = PluginHelper.getCollectionSettingsDomainObject(context.getLink(),
                context.getCollectionViewName(), currentUserAccessor, crudService, collectionsService);
        CollectionViewerConfig collectionViewerConfig = null;
        if (domainObject.getString(DO_COLLECTION_VIEWER_FIELD_KEY) != null) {
            collectionViewerConfig = PluginHelper.deserializeFromXml(CollectionViewerConfig.class,
                    domainObject.getString(DO_COLLECTION_VIEWER_FIELD_KEY));
        }
        if (collectionViewerConfig == null) {
            collectionViewerConfig = context.getCollectionViewerConfig();
        }
        collectionViewerConfig.setDefaultSortCriteriaConfig(
                context.getCollectionViewerConfig().getDefaultSortCriteriaConfig());
        domainObject.setString(DO_COLLECTION_VIEWER_FIELD_KEY,
                PluginHelper.serializeToXml(collectionViewerConfig));
        crudService.save(domainObject);
        return null;
    }

    @Override
    public CollectionSortOrderActionContext getActionContext() {
        return new CollectionSortOrderActionContext();
    }
}
