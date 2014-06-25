package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Created by tbilyi on 18.06.2014.
 */

@Element(name = "field")
public class FieldValueConfig implements Dto{

    @Attribute(name = "name", required = false)
    private String name;

    @Attribute(name = "value", required = false)
    private String value;

    @Attribute(name = "set-current-user", required = false)
    private String setCurrentUser;

    @Attribute(name = "set-base-object", required = false)
    private String setBaseObject;

    @Attribute(name = "set-null", required = false)
    private String setNull;

    @Attribute(name = "type", required = false)
    private String type;

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

    public String getSetCurrentUser() {
        return setCurrentUser;
    }

    public void setSetCurrentUser(String setCurrentUser) {
        this.setCurrentUser = setCurrentUser;
    }

    public String getSetBaseObject() {
        return setBaseObject;
    }

    public void setSetBaseObject(String setBaseObject) {
        this.setBaseObject = setBaseObject;
    }

    public String getSetNull() {
        return setNull;
    }

    public void setSetNull(String setNull) {
        this.setNull = setNull;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UniqueKeyValueConfig getUniqueKeyValueConfig() {
        return uniqueKeyValueConfig;
    }

    public void setUniqueKeyValueConfig(UniqueKeyValueConfig uniqueKeyValueConfig) {
        this.uniqueKeyValueConfig = uniqueKeyValueConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FieldValueConfig that = (FieldValueConfig) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (setBaseObject != null ? !setBaseObject.equals(that.setBaseObject) : that.setBaseObject != null)
            return false;
        if (setCurrentUser != null ? !setCurrentUser.equals(that.setCurrentUser) : that.setCurrentUser != null)
            return false;
        if (setNull != null ? !setNull.equals(that.setNull) : that.setNull != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (uniqueKeyValueConfig != null ? !uniqueKeyValueConfig.equals(that.uniqueKeyValueConfig) : that.uniqueKeyValueConfig != null)
            return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (setCurrentUser != null ? setCurrentUser.hashCode() : 0);
        result = 31 * result + (setBaseObject != null ? setBaseObject.hashCode() : 0);
        result = 31 * result + (setNull != null ? setNull.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (uniqueKeyValueConfig != null ? uniqueKeyValueConfig.hashCode() : 0);
        return result;
    }
}
