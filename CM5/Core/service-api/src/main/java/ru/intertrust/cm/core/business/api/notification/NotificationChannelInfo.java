package ru.intertrust.cm.core.business.api.notification;

/**
 * Описание канала доставки уведомления
 * @author larin
 * 
 */
public class NotificationChannelInfo {
    private String name;
    private String descriptor;
    private NotificationChannelHandle channel;

    public NotificationChannelInfo(String name, String descriptor, NotificationChannelHandle channel) {
        this.name = name;
        this.descriptor = descriptor;
        this.channel = channel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }

    public NotificationChannelHandle getChannel() {
        return channel;
    }

    public void setChannel(NotificationChannelHandle channel) {
        this.channel = channel;
    }
}
