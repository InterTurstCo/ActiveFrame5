package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

/**
 * 
 * @author atsvetkov
 *
 */
@Root(name = "notification")
public class NotificationConfig implements TopLevelConfig {

    @Attribute(name = "name", required = true)
    private String name;

    @Attribute(name = "replace", required = false)
    private String replacementPolicy;
  
    @Element(name = "notification-type", required = true)
    private NotificationTypeConfig notificationTypeConfig = new NotificationTypeConfig();

    public String getName() {
        return name;
    }

    @Override
    public ExtensionPolicy getReplacementPolicy() {
        return ExtensionPolicy.fromString(replacementPolicy);
    }

    @Override
    public ExtensionPolicy getCreationPolicy() {
        return ExtensionPolicy.Runtime;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NotificationTypeConfig getNotificationTypeConfig() {
        return notificationTypeConfig;
    }

    public void setNotificationTypeConfig(NotificationTypeConfig notificationTypeConfig) {
        this.notificationTypeConfig = notificationTypeConfig;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((notificationTypeConfig == null) ? 0 : notificationTypeConfig.hashCode());
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
        NotificationConfig other = (NotificationConfig) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (replacementPolicy != null ? !replacementPolicy.equals(other.replacementPolicy) : other.replacementPolicy != null) {
            return false;
        }
        if (notificationTypeConfig == null) {
            if (other.notificationTypeConfig != null) {
                return false;
            }
        } else if (!notificationTypeConfig.equals(other.notificationTypeConfig)) {
            return false;
        }
        return true;
    }

}
