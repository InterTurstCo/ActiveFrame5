package ru.intertrust.cm.core.config.gui.form.widget.filter;

import org.simpleframework.xml.Attribute;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.05.14
 *         Time: 13:15
 */
public abstract class ParamConfig implements Dto{
    @Attribute(name = "name")
    private Integer name;

    @Attribute(name = "value", required = false)
    private String value;

    @Attribute(name = "type", required = false)
    private String type;

    @Attribute(name = "set-base-object", required = false)
    private boolean setBaseObject;

    @Attribute(name = "set-current-moment", required = false)
    private boolean setCurrentMoment;

    @Attribute(name = "set-current-user", required = false)
    private boolean setCurrentUser;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isSetCurrentMoment() {
        return setCurrentMoment;
    }

    public void setSetCurrentMoment(boolean setCurrentMoment) {
        this.setCurrentMoment = setCurrentMoment;
    }

    public boolean isSetCurrentUser() {
        return setCurrentUser;
    }

    public void setSetCurrentUser(boolean setCurrentUser) {
        this.setCurrentUser = setCurrentUser;
    }

    public Integer getName() {
        return name;
    }

    public void setName(Integer name) {
        this.name = name;
    }

    public boolean isSetBaseObject() {
        return setBaseObject;
    }

    public void setSetBaseObject(boolean setBaseObject) {
        this.setBaseObject = setBaseObject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ParamConfig that = (ParamConfig) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (type != null ? !type.equals(that.type) : that.type != null) {
            return false;
        }
        if (value != null ? !value.equals(that.value) : that.value != null) {
            return false;
        }
        if (setBaseObject != that.setBaseObject) {
            return false;
        }
        if (setCurrentMoment != that.setCurrentMoment) {
            return false;
        }
        if (setCurrentUser != that.setCurrentUser) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (setBaseObject ? 1 : 0);
        result = 31 * result + (setCurrentMoment ? 1 : 0);
        result = 31 * result + (setCurrentUser ? 1 : 0);
        return result;
    }
}
