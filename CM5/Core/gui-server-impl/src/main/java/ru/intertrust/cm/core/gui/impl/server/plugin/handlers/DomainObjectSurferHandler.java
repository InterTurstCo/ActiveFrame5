package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.gui.navigation.DomainObjectSurferConfig;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.gui.api.server.plugin.ActivePluginHandler;
import ru.intertrust.cm.core.gui.impl.server.util.PluginHandlerHelper;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.ActivePluginData;
import ru.intertrust.cm.core.gui.model.plugin.DomainObjectSurferPluginData;
import ru.intertrust.cm.core.gui.model.plugin.DomainObjectSurferPluginState;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginState;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionPluginData;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;
import ru.intertrust.cm.core.model.AccessException;
import ru.intertrust.cm.core.model.ObjectNotFoundException;

import java.util.ArrayList;
import java.util.List;

import static ru.intertrust.cm.core.gui.model.util.UserSettingsHelper.DO_SPLITTER_ORIENTATION_FIELD_KEY;
import static ru.intertrust.cm.core.gui.model.util.UserSettingsHelper.DO_SPLITTER_POSITION_FIELD_KEY;
import static ru.intertrust.cm.core.gui.model.util.UserSettingsHelper.SELECTED_IDS_KEY;

@ComponentName("domain.object.surfer.plugin")
public class DomainObjectSurferHandler extends ActivePluginHandler {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private CurrentUserAccessor currentUserAccessor;
    @Autowired
    private CollectionsService collectionsService;
    @Autowired
    private CrudService crudService;

    public ActivePluginData initialize(Dto params) {
        final DomainObjectSurferConfig config = (DomainObjectSurferConfig) params;
        config.getCollectionViewerConfig().addHistoryValues(config.getHistoryValues());

        final CollectionPluginHandler collectionPluginHandler =
                (CollectionPluginHandler) applicationContext.getBean("collection.plugin");
        final CollectionPluginData collectionPluginData = collectionPluginHandler.initialize(config.getCollectionViewerConfig());
        final ArrayList<CollectionRowItem> items = collectionPluginData.getItems();
        final FormPluginConfig formPluginConfig;
        if (items == null || items.isEmpty()) {
            formPluginConfig = new FormPluginConfig(config.getDomainObjectTypeToCreate());
        } else {
            final List<Id> selectedIds = new ArrayList<>();
            final List<Id> selectedFromHistory = getSelectedIdsFromHistory(config);
            if (selectedFromHistory != null && !selectedFromHistory.isEmpty()) {
                selectedIds.addAll(selectedFromHistory);
            } else {
                selectedIds.add(items.get(0).getId());
            }
            formPluginConfig = new FormPluginConfig(selectedIds.get(0));
            collectionPluginData.setChosenIds(selectedIds);
        }
        formPluginConfig.addHistoryValues(config.getHistoryValues());
        final FormPluginState fpState = new FormPluginState();
        formPluginConfig.setPluginState(fpState);
        formPluginConfig.setFormViewerConfig(config.getFormViewerConfig());
        fpState.setToggleEdit(config.isToggleEdit());
        fpState.setEditable(!config.isToggleEdit());
        final FormPluginHandler formPluginHandler = (FormPluginHandler) applicationContext.getBean("form.plugin");
        final FormPluginData formPluginData = formPluginHandler.initialize(formPluginConfig);
        final DomainObjectSurferPluginData result = new DomainObjectSurferPluginData();
        result.setCollectionPluginData(collectionPluginData);
        result.setFormPluginData(formPluginData);
        final DomainObjectSurferPluginState dosState = new DomainObjectSurferPluginState();
        dosState.setToggleEdit(config.isToggleEdit());

        result.setPluginState(dosState);
        final IdentifiableObject identifiableObject = PluginHandlerHelper.getUserSettingsIdentifiableObject(
                currentUserAccessor.getCurrentUser(), collectionsService);
        if (identifiableObject != null) {
            final Long splitterOrientation = identifiableObject.getLong(DO_SPLITTER_ORIENTATION_FIELD_KEY);
            final Long splitterPosition = identifiableObject.getLong(DO_SPLITTER_POSITION_FIELD_KEY);
            if (splitterOrientation != null && splitterPosition != null) {
                result.setSplitterOrientation(splitterOrientation.intValue());
                result.setSplitterPosition(splitterPosition.intValue());
            }
        }
        return result;
    }

    private List<Id> getSelectedIdsFromHistory(DomainObjectSurferConfig config) {
        final List<Id> idsFromHistory = config.getHistoryValue(SELECTED_IDS_KEY);
        if (idsFromHistory == null) {
            return null;
        }
        final List<Id> validIds = new ArrayList<>(idsFromHistory.size());
        for (Id id : idsFromHistory) {
            try {
                if (id instanceof RdbmsId) {
                    crudService.find(id);
                    validIds.add(id);
                }
            } catch (ObjectNotFoundException | AccessException e) {
                // ignoring ids of non-existent or non-accessible DOs
            }
        }
        return validIds;
    }

}
