package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.config.gui.navigation.HeaderNotificationConfig;

import java.util.ArrayList;

/**
 * Created by lvov on 26.03.14.
 */
public class HeaderNotificationPluginData extends PluginData {
    private String collectionName = "NewNotifications";
    ArrayList<HeaderNotificationItem> collection;
//    HeaderNotificationConfig headerNotificationConfig;
    private String subjectColumnName;
    private String bodyColumnName ;


    public HeaderNotificationPluginData() {

    }


    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public ArrayList<HeaderNotificationItem> getCollection() {
        return collection;
    }

    public void setCollection(ArrayList<HeaderNotificationItem> collection) {
        this.collection = collection;
    }

//    public HeaderNotificationConfig getHeaderNotificationConfig() {
//        return headerNotificationConfig;
//    }
//
//    public void setHeaderNotificationConfig(HeaderNotificationConfig headerNotificationConfig) {
//        this.headerNotificationConfig = headerNotificationConfig;
//    }

    public String getSubjectColumnName() {
        return subjectColumnName;
    }

    public void setSubjectColumnName(String subjectColumnName) {
        this.subjectColumnName = subjectColumnName;
    }

    public String getBodyColumnName() {
        return bodyColumnName;
    }

    public void setBodyColumnName(String bodyColumnName) {
        this.bodyColumnName = bodyColumnName;
    }
}
