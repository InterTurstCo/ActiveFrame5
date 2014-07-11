package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.navigation.DomainObjectSurferConfig;
import ru.intertrust.cm.core.gui.api.server.plugin.ActivePluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.ActivePluginData;
import ru.intertrust.cm.core.gui.model.plugin.CollectionPluginData;
import ru.intertrust.cm.core.gui.model.plugin.CollectionRowItem;
import ru.intertrust.cm.core.gui.model.plugin.DomainObjectSurferPluginData;
import ru.intertrust.cm.core.gui.model.plugin.DomainObjectSurferPluginState;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginState;

@ComponentName("domain.object.surfer.plugin")
public class DomainObjectSurferHandler extends ActivePluginHandler {
    private static final String IDS_KEY = "ids";

    @Autowired
    private ApplicationContext applicationContext;

    public ActivePluginData initialize(Dto params) {
        final DomainObjectSurferConfig config = (DomainObjectSurferConfig) params;

        final CollectionPluginHandler collectionPluginHandler =
                (CollectionPluginHandler) applicationContext.getBean("collection.plugin");
        final CollectionPluginData collectionPluginData = collectionPluginHandler.initialize(config.getCollectionViewerConfig());
        final ArrayList<CollectionRowItem> items = collectionPluginData.getItems();

        final FormPluginConfig formPluginConfig;
        if (items == null || items.isEmpty()) {
            formPluginConfig = new FormPluginConfig(config.getDomainObjectTypeToCreate());
        } else {
            final List<Id> selectedIds = new ArrayList<>();
            final List<Id> selectedFromHistory = config.getHistoryValue(IDS_KEY);
            if (selectedFromHistory != null && !selectedFromHistory.isEmpty()) {
                selectedIds.addAll((List) config.getHistoryValue(IDS_KEY));
            } else {
                selectedIds.add(items.get(0).getId());
            }
            formPluginConfig = new FormPluginConfig(selectedIds.get(0));
            collectionPluginData.setChosenIds(selectedIds);
        }
        final FormPluginState fpState = new FormPluginState();
        formPluginConfig.setPluginState(fpState);
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
        return result;
    }
}
