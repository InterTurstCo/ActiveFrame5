package ru.intertrust.cm.core.gui.impl.server.action.system;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionViewConfig;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.impl.server.util.PluginHelper;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.system.CollectionColumnHiddenActionContext;
import ru.intertrust.cm.core.util.ObjectCloner;

import static ru.intertrust.cm.core.gui.model.util.UserSettingsHelper.DO_COLLECTION_VIEW_FIELD_KEY;

/**
 * @author Sergey.Okolot
 *         Created on 06.08.2014 11:54.
 */
@ComponentName(CollectionColumnHiddenActionContext.COMPONENT_NAME)
public class CollectionColumnHiddenActionHandler extends ActionHandler<CollectionColumnHiddenActionContext, ActionData> {

    @Autowired private ConfigurationService configurationService;
    @Autowired private CrudService crudService;
    @Autowired private CollectionsService collectionsService;
    @Autowired private CurrentUserAccessor currentUserAccessor;

    @Override
    public ActionData executeAction(CollectionColumnHiddenActionContext context) {
        if (context.getLink() == null) {
            throw new GuiException("Неизвестный url");
        }
        final DomainObject object = PluginHelper.getCollectionSettingsDomainObject(context.getLink(),
                context.getCollectionViewName(), currentUserAccessor, crudService, collectionsService);
        CollectionViewConfig collectionViewConfig = null;
        if (object.getString(DO_COLLECTION_VIEW_FIELD_KEY) != null) {
            collectionViewConfig = PluginHelper.deserializeFromXml(
                    CollectionViewConfig.class, object.getString(DO_COLLECTION_VIEW_FIELD_KEY));
        }
        if (collectionViewConfig == null) {
            collectionViewConfig =
                    configurationService.getConfig(CollectionViewConfig.class, context.getCollectionViewName());
            final ObjectCloner cloner = new ObjectCloner();
            collectionViewConfig = cloner.cloneObject(collectionViewConfig, CollectionViewConfig.class);
        }
        collectionViewConfig.getCollectionDisplayConfig().updateColumnHidden(context.getHiddenFields());
        final String configAsStr = PluginHelper.serializeToXml(collectionViewConfig);
        object.setString(DO_COLLECTION_VIEW_FIELD_KEY, configAsStr);
        crudService.save(object);
        return null;
    }

    @Override
    public CollectionColumnHiddenActionContext getActionContext() {
        return new CollectionColumnHiddenActionContext();
    }
}
