package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.gui.navigation.HeaderNotificationConfig;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.*;

import java.util.ArrayList;

/**
 * Created by lvov on 26.03.14.
 */
@ComponentName("header.notifications.plugin")
public class HeaderNotificationPluginHandler extends PluginHandler {

    @Autowired
    CollectionsService collectionsService;

    @Autowired
    protected CrudService crudService;

    private final String collectionName = "NewNotifications";
    private final String subjectColumnName = "subject";
    private final String bodyColumnName = "body";

    @Override
    public PluginData initialize(Dto param) {

        HeaderNotificationPluginData pluginData = new HeaderNotificationPluginData();
        ArrayList<HeaderNotificationItem> collItems  = getNotificationList();
        pluginData.setCollection(collItems);
        pluginData.setSubjectColumnName(subjectColumnName);
        pluginData.setBodyColumnName(bodyColumnName);

        return pluginData;
    }


    public Dto deleteNotification(Dto dto){

        CancelHeaderNotificationItem cancelHeaderNotificationItem = (CancelHeaderNotificationItem) dto;
        DomainObject domainObject = crudService.find(cancelHeaderNotificationItem.getId());
        Value value = new DecimalValue(0);
        domainObject.setValue("new", value);
        crudService.save(domainObject);

        cancelHeaderNotificationItem.setItems(getNotificationList());


        return cancelHeaderNotificationItem ;
    }

    private ArrayList<HeaderNotificationItem> getNotificationList(){

        ArrayList<HeaderNotificationItem> collItems = new ArrayList<HeaderNotificationItem>();
        IdentifiableObjectCollection collection = collectionsService.findCollection(collectionName);
        for (int i =0; i < collection.size(); i++){
            IdentifiableObject identifiableObject = collection.get(i);
            collItems.add(new HeaderNotificationItem(identifiableObject.getId(),
                    identifiableObject.getString(subjectColumnName), identifiableObject.getString(bodyColumnName)));

        }

        return collItems;
    }

}
