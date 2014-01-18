package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.navigation.DomainObjectSurferConfig;
import ru.intertrust.cm.core.gui.api.server.plugin.ActivePluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.plugin.*;

import java.util.ArrayList;
import java.util.List;

@ComponentName("domain.object.surfer.plugin")
public class DomainObjectSurferHandler extends ActivePluginHandler {

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
            formPluginConfig = new FormPluginConfig(items.get(0).getId());
            final ArrayList<Integer> selectedIndexes = new ArrayList<>(1);
            selectedIndexes.add(Integer.valueOf(0));
            collectionPluginData.setIndexesOfSelectedItems(selectedIndexes);
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
        result.setActionContexts(getActions(params, formPluginData));
        final DomainObjectSurferPluginState dosState = new DomainObjectSurferPluginState();
        dosState.setToggleEdit(config.isToggleEdit());
        result.setPluginState(dosState);
        return result;
    }

    public List<ActionContext> getActions(Dto initialData, FormPluginData formPluginData)  {
        ArrayList<ActionContext> result = new ArrayList<>();
        result.addAll(formPluginData.getActionContexts());
        return result;
    }
}
