package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.BusinessUniverseConfig;
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

    @Autowired
    private ConfigurationService configurationService;

    private static final String FIELD_ID = "id";
    private static final String ASC_CRITERION = "asc";

    private final static String COLLECTION_NAME = "NewNotifications";
    private final static String SUBJECT_COLUMN_NAME = "subject";
    private final static String BODY_COLUMN_NAME = "body";
    private final static String CONTEXT_OBJECT_COLUMN_NAME = "context_object";

    @Override
    public PluginData initialize(Dto param) {

        HeaderNotificationPluginData pluginData = new HeaderNotificationPluginData();
        ArrayList<HeaderNotificationItem> collItems  = getNotificationList();
        pluginData.setCollection(collItems);
        pluginData.setSubjectColumnName(SUBJECT_COLUMN_NAME);
        pluginData.setBodyColumnName(BODY_COLUMN_NAME);

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
        BusinessUniverseConfig businessUniverseConfig = configurationService.getConfig(BusinessUniverseConfig.class,
                BusinessUniverseConfig.NAME);
        SortOrder sortOrder = new SortOrder();
        if (businessUniverseConfig.getNotificationSortOrderConfig() != null && businessUniverseConfig.getNotificationSortOrderConfig().getValue() != null) {
            if(businessUniverseConfig.getNotificationSortOrderConfig().getValue().toLowerCase().equals(ASC_CRITERION)){
                sortOrder.add(new SortCriterion(FIELD_ID, SortCriterion.Order.ASCENDING));
            } else {
                sortOrder.add(new SortCriterion(FIELD_ID, SortCriterion.Order.DESCENDING));
            }
        }
            ArrayList<HeaderNotificationItem> collItems = new ArrayList<HeaderNotificationItem>();

        Filter filter = new Filter();
        filter.setFilter("byRecipient");

        Id userId = currentUserAccessor.getCurrentUserId();
        filter.addReferenceCriterion(0, userId);
        IdentifiableObjectCollection collection = collectionsService.findCollection(COLLECTION_NAME, sortOrder,
                Collections.singletonList(filter));
        for (int i =0; i < collection.size(); i++){
            final IdentifiableObject identifiableObject = collection.get(i);
            collItems.add(new HeaderNotificationItem(identifiableObject.getId(), identifiableObject.getReference(CONTEXT_OBJECT_COLUMN_NAME),
                    identifiableObject.getString(SUBJECT_COLUMN_NAME), identifiableObject.getString(BODY_COLUMN_NAME)));

        }

        return collItems;
    }

}
