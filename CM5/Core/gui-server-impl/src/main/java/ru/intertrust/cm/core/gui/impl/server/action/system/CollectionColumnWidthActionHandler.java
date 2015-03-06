package ru.intertrust.cm.core.gui.impl.server.action.system;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.ProfileService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionViewConfig;
import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.config.localization.MessageResourceProvider;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.impl.server.util.PluginHandlerHelper;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.system.CollectionColumnWidthActionContext;
import ru.intertrust.cm.core.util.ObjectCloner;

import java.util.Map;

import static ru.intertrust.cm.core.gui.model.util.UserSettingsHelper.DO_COLLECTION_VIEW_FIELD_KEY;

/**
 * @author Sergey.Okolot
 *         Created on 28.07.2014 16:07.
 */
@ComponentName(CollectionColumnWidthActionContext.COMPONENT_NAME)
public class CollectionColumnWidthActionHandler extends ActionHandler<CollectionColumnWidthActionContext, ActionData> {

    @Autowired private ConfigurationService configurationService;
    @Autowired private CrudService crudService;
    @Autowired private CollectionsService collectionsService;
    @Autowired private CurrentUserAccessor currentUserAccessor;
    @Autowired private ProfileService profileService;

    @Override
    public ActionData executeAction(CollectionColumnWidthActionContext context) {
        if (context.getLink() == null) {
            throw new GuiException(MessageResourceProvider.getMessage(LocalizationKeys.GUI_EXCEPTION_UNKNOWN_URL,
                    "Неизвестный url", profileService.getPersonLocale()));
        }
        final DomainObject object = PluginHandlerHelper.getCollectionSettingsDomainObject(context.getLink(),
                context.getCollectionViewName(), currentUserAccessor, crudService, collectionsService);
        CollectionViewConfig collectionViewConfig = null;
        if (object.getString(DO_COLLECTION_VIEW_FIELD_KEY) != null) {
            collectionViewConfig = PluginHandlerHelper.deserializeFromXml(
                    CollectionViewConfig.class, object.getString(DO_COLLECTION_VIEW_FIELD_KEY));
        }
        if (collectionViewConfig == null) {
            collectionViewConfig =
                    configurationService.getConfig(CollectionViewConfig.class, context.getCollectionViewName());
            final ObjectCloner cloner = new ObjectCloner();
            collectionViewConfig = cloner.cloneObject(collectionViewConfig, CollectionViewConfig.class);
        }
        Map<String, String> fieldWidthMap = context.getFieldWidthMap();
        for (Map.Entry<String, String> entry : fieldWidthMap.entrySet()) {
            collectionViewConfig.getCollectionDisplayConfig().updateColumnWidth(entry.getKey(), entry.getValue());
        }

        final String configAsStr = PluginHandlerHelper.serializeToXml(collectionViewConfig);
        object.setString(DO_COLLECTION_VIEW_FIELD_KEY, configAsStr);
        crudService.save(object);
        return null;
    }

    @Override
    public CollectionColumnWidthActionContext getActionContext(final ActionConfig actionConfig) {
        return new CollectionColumnWidthActionContext();
    }
}
