package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * 
 * @author atsvetkov
 *
 */
public class NotificationTypeConfig implements Dto {

    @Attribute(name = "name", required = true)
    private String name;
    
    @Attribute(name = "priority", required = true)
    private String priority;
 
    @Element(name = "addressee", required = false)
    private NotificationAddresseConfig notificationAddresseConfig;
    
    @Element(name = "triggers", required = true)
    private NotificationTriggersConfig notificationTriggersConfig = new NotificationTriggersConfig();

    @Element(name = "context-config", required = false)
    private NotificationContextConfig notificationContextConfig = new NotificationContextConfig();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NotificationAddresseConfig getNotificationAddresseConfig() {
        return notificationAddresseConfig;
    }

    public void setNotificationAddresseConfig(NotificationAddresseConfig notificationAddresseConfig) {
        this.notificationAddresseConfig = notificationAddresseConfig;
    }

    public NotificationTriggersConfig getNotificationTriggersConfig() {
        return notificationTriggersConfig;
    }

    public void setNotificationTriggersConfig(NotificationTriggersConfig notificationTriggersConfig) {
        this.notificationTriggersConfig = notificationTriggersConfig;
    }
    
    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public NotificationContextConfig getNotificationContextConfig() {
        return notificationContextConfig;
    }

    public void setNotificationContextConfig(NotificationContextConfig notificationContextConfig) {
        this.notificationContextConfig = notificationContextConfig;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((notificationAddresseConfig == null) ? 0 : notificationAddresseConfig.hashCode());
        result = prime * result + ((notificationTriggersConfig == null) ? 0 : notificationTriggersConfig.hashCode());
        result = prime * result + ((notificationContextConfig == null) ? 0 : notificationContextConfig.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NotificationTypeConfig other = (NotificationTypeConfig) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (notificationAddresseConfig == null) {
            if (other.notificationAddresseConfig != null) {
                return false;
            }
        } else if (!notificationAddresseConfig.equals(other.notificationAddresseConfig)) {
            return false;
        }
        if (notificationTriggersConfig == null) {
            if (other.notificationTriggersConfig != null) {
                return false;
            }
        } else if (!notificationTriggersConfig.equals(other.notificationTriggersConfig)) {
            return false;
        }
        if (notificationContextConfig == null) {
            if (other.notificationContextConfig != null) {
                return false;
            }
        } else if (!notificationContextConfig.equals(other.notificationContextConfig)) {
            return false;
        }
        return true;
    }
    
}
