package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.config.gui.navigation.HeaderNotificationConfig;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.CollectionRowItem;
import ru.intertrust.cm.core.gui.model.plugin.HeaderNotificationItem;
import ru.intertrust.cm.core.gui.model.plugin.HeaderNotificationPluginData;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;

import java.util.ArrayList;

/**
 * Created by lvov on 26.03.14.
 */
@ComponentName("header.notifications.plugin")
public class HeaderNotificationPluginHandler extends PluginHandler {

    @Autowired
    CollectionsService collectionsService;

    private String collectionName = "NewNotifications";
    private String subjectColumnName = "subject";
    private String bodyColumnName = "body";

    @Override
    public PluginData initialize(Dto param) {

        HeaderNotificationPluginData pluginData = new HeaderNotificationPluginData();
        ArrayList<HeaderNotificationItem> collItems = new ArrayList<HeaderNotificationItem>();
        IdentifiableObjectCollection collection = collectionsService.findCollection(collectionName);

        for (int i =0; i < collection.size(); i++){
            IdentifiableObject identifiableObject = collection.get(i);
            collItems.add(new HeaderNotificationItem(identifiableObject.getId(),
                    identifiableObject.getString(subjectColumnName), identifiableObject.getString(bodyColumnName)));

        }


        pluginData.setCollection(collItems);
        pluginData.setSubjectColumnName(subjectColumnName);
        pluginData.setBodyColumnName(bodyColumnName);



        return pluginData;
    }


}
