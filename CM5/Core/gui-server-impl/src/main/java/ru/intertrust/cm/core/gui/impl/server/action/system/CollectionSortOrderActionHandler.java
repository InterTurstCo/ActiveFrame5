package ru.intertrust.cm.core.gui.impl.server.action.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionViewerConfig;
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
import ru.intertrust.cm.core.gui.model.action.system.CollectionSortOrderActionContext;

import javax.ejb.EJB;

import static ru.intertrust.cm.core.gui.model.util.UserSettingsHelper.DO_COLLECTION_VIEWER_FIELD_KEY;

/**
 * @author Sergey.Okolot
 *         Created on 31.07.2014 18:39.
 */
@ComponentName(CollectionSortOrderActionContext.COMPONENT_NAME)
public class CollectionSortOrderActionHandler extends ActionHandler<CollectionSortOrderActionContext, ActionData> {

    private static final String SETTINGS_OP_LOCK = "Optimistic lock exception while saving settings";
    private static Logger log = LoggerFactory.getLogger(CollectionSortOrderActionHandler.class);

    @Autowired private CollectionsService collectionsService;
    @EJB
    SettingsUtil settingsUtil;
    @Override
    public ActionData executeAction(CollectionSortOrderActionContext context) {
        if (context.getLink() == null) {
            throw new GuiException(MessageResourceProvider.getMessage(LocalizationKeys.GUI_EXCEPTION_UNKNOWN_URL,
                    "Неизвестный url", GuiContext.getUserLocale()));
        }
        final DomainObject domainObject = PluginHandlerHelper.findAndLockCollectionSettingsDomainObject(context.getLink(),
                context.getCollectionViewName(), currentUserAccessor, crudService, collectionsService,settingsUtil);
        CollectionViewerConfig collectionViewerConfig = null;
        if (domainObject.getString(DO_COLLECTION_VIEWER_FIELD_KEY) != null) {
            collectionViewerConfig = PluginHandlerHelper.deserializeFromXml(CollectionViewerConfig.class,
                    domainObject.getString(DO_COLLECTION_VIEWER_FIELD_KEY));
        }
        if (collectionViewerConfig == null) {
            collectionViewerConfig = context.getCollectionViewerConfig();
        }
        collectionViewerConfig.setDefaultSortCriteriaConfig(
                context.getCollectionViewerConfig().getDefaultSortCriteriaConfig());
        domainObject.setString(DO_COLLECTION_VIEWER_FIELD_KEY,
                PluginHandlerHelper.serializeToXml(collectionViewerConfig));
        try {
            crudService.save(domainObject);
        } catch (OptimisticLockException ole){
            log.error(SETTINGS_OP_LOCK,ole);
        }
        return null;
    }

    @Override
    public CollectionSortOrderActionContext getActionContext(final ActionConfig actionConfig) {
        return new CollectionSortOrderActionContext();
    }
}
