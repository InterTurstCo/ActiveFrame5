package ru.intertrust.cm.core.gui.impl.server.action.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.util.ObjectCloner;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionViewConfig;
import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.config.localization.MessageResourceProvider;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.exception.OptimisticLockException;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.impl.server.util.PluginHandlerHelper;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.system.CollectionColumnOrderActionContext;

import javax.ejb.EJB;

import static ru.intertrust.cm.core.gui.model.util.UserSettingsHelper.DO_COLLECTION_VIEW_FIELD_KEY;

/**
 * @author Sergey.Okolot
 *         Created on 31.07.2014 17:31.
 */
@ComponentName(CollectionColumnOrderActionContext.COMPONENT_NAME)
public class CollectionColumnOrderActionHandler extends ActionHandler<CollectionColumnOrderActionContext, ActionData> {

    private static final String SETTINGS_OP_LOCK = "Optimistic lock exception while saving settings";
    private static Logger log = LoggerFactory.getLogger(CollectionColumnOrderActionHandler.class);

    @Autowired private ConfigurationExplorer configurationService;
    @Autowired private CollectionsService collectionsService;
    @EJB
    SettingsUtil settingsUtil;
    @Override
    public ActionData executeAction(CollectionColumnOrderActionContext context) {
        if (context.getLink() == null) {
            throw new GuiException(MessageResourceProvider.getMessage(LocalizationKeys.GUI_EXCEPTION_UNKNOWN_URL,
                    "Неизвестный url", GuiContext.getUserLocale()));
        }
        final DomainObject object = PluginHandlerHelper.findAndLockCollectionSettingsDomainObject(context.getLink(),
                context.getCollectionViewName(), currentUserAccessor, crudService, collectionsService,settingsUtil);
        CollectionViewConfig collectionViewConfig = null;
        if (object.getString(DO_COLLECTION_VIEW_FIELD_KEY) != null) {
            collectionViewConfig = PluginHandlerHelper.deserializeFromXml(
                    CollectionViewConfig.class, object.getString(DO_COLLECTION_VIEW_FIELD_KEY));
        }
        if (collectionViewConfig == null) {
            collectionViewConfig =
                    configurationService.getConfig(CollectionViewConfig.class, context.getCollectionViewName());
            collectionViewConfig = ObjectCloner.getInstance().cloneObject(collectionViewConfig, CollectionViewConfig.class);
        }
        collectionViewConfig.getCollectionDisplayConfig().updateColumnOrder(context.getOrders());
        final String configAsStr = PluginHandlerHelper.serializeToXml(collectionViewConfig);
        object.setString(DO_COLLECTION_VIEW_FIELD_KEY, configAsStr);
        try {
            crudService.save(object);
        } catch (OptimisticLockException ole){
            log.error(SETTINGS_OP_LOCK,ole);
        }
        return null;
    }

    @Override
    public CollectionColumnOrderActionContext getActionContext(final ActionConfig actionConfig) {
        return new CollectionColumnOrderActionContext();
    }
}
