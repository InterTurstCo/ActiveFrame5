package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Created by tbilyi on 18.06.2014.
 */

public class FieldValueConfig implements Dto{

    @Attribute(name = "name", required = false)
    private String name;

    @Attribute(name = "value", required = false)
    private String value;

    @Attribute(name = "set-current-user", required = false)
    private boolean setCurrentUser;

    @Attribute(name = "set-base-object", required = false)
    private boolean setBaseObject;

    @Attribute(name = "set-null", required = false)
    private boolean setNull;

    @Attribute(name = "set-current-moment", required = false)
    private boolean setCurrentMoment;

    @Attribute(name = "time-zone-id", required = false)
    private String timeZoneId;

    @Element(name = "unique-key-value", required = false)
    private UniqueKeyValueConfig uniqueKeyValueConfig;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isSetCurrentUser() {
        return setCurrentUser;
    }

    public void setSetCurrentUser(boolean setCurrentUser) {
        this.setCurrentUser = setCurrentUser;
    }

    public boolean isSetBaseObject() {
        return setBaseObject;
    }

    public void setSetBaseObject(boolean setBaseObject) {
        this.setBaseObject = setBaseObject;
    }

    public boolean isSetNull() {
        return setNull;
    }

    public void setSetNull(boolean setNull) {
        this.setNull = setNull;
    }

    public boolean isSetCurrentMoment() {
        return setCurrentMoment;
    }

    public void setSetCurrentMoment(boolean setCurrentMoment) {
        this.setCurrentMoment = setCurrentMoment;
    }

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    public UniqueKeyValueConfig getUniqueKeyValueConfig() {
        return uniqueKeyValueConfig;
    }

    public void setUniqueKeyValueConfig(UniqueKeyValueConfig uniqueKeyValueConfig) {
        this.uniqueKeyValueConfig = uniqueKeyValueConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FieldValueConfig that = (FieldValueConfig) o;

        if (setBaseObject != that.setBaseObject) {
            return false;
        }
        if (setCurrentUser != that.setCurrentUser) {
            return false;
        }
        if (setNull != that.setNull) {
            return false;
        }
        if (setCurrentMoment != that.setCurrentMoment) {
            return false;
        }
        if (!name.equals(that.name)) {
            return false;
        }
        if (uniqueKeyValueConfig != null ? !uniqueKeyValueConfig.equals(that.uniqueKeyValueConfig) : that.uniqueKeyValueConfig != null) {
            return false;
        }
        if (value != null ? !value.equals(that.value) : that.value != null) {
            return false;
        }
        if (timeZoneId != null ? !timeZoneId.equals(that.timeZoneId) : that.timeZoneId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
