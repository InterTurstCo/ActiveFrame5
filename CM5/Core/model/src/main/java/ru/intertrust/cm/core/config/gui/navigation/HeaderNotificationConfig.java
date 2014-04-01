package ru.intertrust.cm.core.config.gui.navigation;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Created by lvov on 25.03.14.
 */
@Root(name = "header-notification")
public class HeaderNotificationConfig extends PluginConfig {

    private String collectionName = "NewNotifications";
    private String subjectColumnName = "subject";
    private String bodyColumnName = "body";



    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HeaderNotificationConfig that = (HeaderNotificationConfig) o;



        return true;
    }

//    @Override
//    public int hashCode() {
//        int result =  1;
//        result = 31 * result;
//        return result;
//    }





    @Override
    public String getComponentName() {
        return "header.notifications.plugin";
    }
}
