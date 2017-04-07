package ru.intertrust.cm.core.config.gui.action;

import org.simpleframework.xml.Attribute;

/**
 * @author Sergey.Okolot
 *         Created on 02.09.2014 15:48.
 */
public class OnSuccessMessageConfig extends MessageConfig {

    public static final String DEFAULT_NOTIFICATION_TYPE = "fading";

    @Attribute(name = "success-notification-type", required = false)
    private String successNotificationType;

    public String getSuccessNotificationType() {
        return successNotificationType == null ? DEFAULT_NOTIFICATION_TYPE : successNotificationType;
    }

    public void setSuccessNotificationType(String successNotificationType) {
        this.successNotificationType = successNotificationType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        OnSuccessMessageConfig that = (OnSuccessMessageConfig) o;

        if (successNotificationType != null ? !successNotificationType.equals(that.successNotificationType) : that.successNotificationType != null)
            return false;

        return true;
    }
}
