package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.config.model.gui.navigation.DomainObjectSurferConfig;
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
        DomainObjectSurferConfig config = (DomainObjectSurferConfig) params;

        CollectionPluginHandler collectionPluginHandler =
                (CollectionPluginHandler) applicationContext.getBean("collection.plugin");
        CollectionPluginData collectionPluginData = collectionPluginHandler.initialize(config.getCollectionViewerConfig());
        IdentifiableObjectCollection collection = collectionPluginData.getCollection();

        FormPluginConfig formPluginConfig;
        if (collection == null || collection.size() == 0) {
            formPluginConfig = new FormPluginConfig(config.getDomainObjectTypeToCreate());
        } else {
            formPluginConfig = new FormPluginConfig(collection.get(0).getId());
        }
        FormPluginHandler formPluginHandler = (FormPluginHandler) applicationContext.getBean("form.plugin");
        FormPluginData formPluginData = formPluginHandler.initialize(formPluginConfig);

        DomainObjectSurferPluginData result = new DomainObjectSurferPluginData();
        result.setCollectionPluginData(collectionPluginData);
        result.setFormPluginData(formPluginData);
        result.setActionContexts(getActions(params, formPluginData));
        return result;
    }

    public List<ActionContext> getActions(Dto initialData, FormPluginData formPluginData)  {
        ArrayList<ActionContext> result = new ArrayList<>();
        result.addAll(formPluginData.getActionContexts());
        return result;
    }

}
