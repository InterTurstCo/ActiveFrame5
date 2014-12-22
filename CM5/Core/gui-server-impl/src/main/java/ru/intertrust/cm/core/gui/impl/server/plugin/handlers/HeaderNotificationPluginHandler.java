package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DecimalValue;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.CancelHeaderNotificationItem;
import ru.intertrust.cm.core.gui.model.plugin.HeaderNotificationItem;
import ru.intertrust.cm.core.gui.model.plugin.HeaderNotificationPluginData;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by lvov on 26.03.14.
 */
@ComponentName("header.notifications.plugin")
public class HeaderNotificationPluginHandler extends PluginHandler {

    @Autowired
    CollectionsService collectionsService;

    @Autowired
    protected CrudService crudService;

    @Autowired
    private CurrentUserAccessor currentUserAccessor;

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
        if (cancelHeaderNotificationItem.getId() != null){
        DomainObject domainObject = crudService.find(cancelHeaderNotificationItem.getId());
        Value value = new DecimalValue(0);
        domainObject.setValue("new", value);
        crudService.save(domainObject);
        }
        cancelHeaderNotificationItem.setItems(getNotificationList());


        return cancelHeaderNotificationItem ;
    }

    private ArrayList<HeaderNotificationItem> getNotificationList(){
        ArrayList<HeaderNotificationItem> collItems = new ArrayList<HeaderNotificationItem>();

        Filter filter = new Filter();
        filter.setFilter("byRecipient");

        Id id = currentUserAccessor.getCurrentUserId();
        filter.addReferenceCriterion(0, id);
        IdentifiableObjectCollection collection = collectionsService.findCollection(collectionName, new SortOrder(),
                Collections.singletonList(filter));
        for (int i =0; i < collection.size(); i++){
            IdentifiableObject identifiableObject = collection.get(i);
            collItems.add(new HeaderNotificationItem(identifiableObject.getId(),
                    identifiableObject.getString(subjectColumnName), identifiableObject.getString(bodyColumnName)));

        }

        return collItems;
    }

}
