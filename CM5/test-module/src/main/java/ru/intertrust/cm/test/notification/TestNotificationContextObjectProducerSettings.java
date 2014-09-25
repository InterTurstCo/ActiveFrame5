package ru.intertrust.cm.test.notification;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import ru.intertrust.cm.core.config.FindObjectSettings;

@Root(name="notification-context-object-producer-settings")
public class TestNotificationContextObjectProducerSettings implements FindObjectSettings{
    private static final long serialVersionUID = -6981634017698032790L;

    @Attribute(name="field-name")
    private String fieldName;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
}
