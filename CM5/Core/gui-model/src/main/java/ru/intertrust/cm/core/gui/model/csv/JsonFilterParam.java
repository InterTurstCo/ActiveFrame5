package ru.intertrust.cm.core.gui.model.csv;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 28.05.14
 *         Time: 16:15
 */
public class JsonFilterParam {
    private Integer name;
    private String value;
    private String type;
    private boolean setCurrentMoment;
    private boolean setCurrentUser;
    private boolean setBaseObject;
    private String timeZoneId;

    public Integer getName() {
        return name;
    }

    public void setName(Integer name) {
        this.name = name;
    }

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

    public boolean isSetBaseObject() {
        return setBaseObject;
    }

    public void setSetBaseObject(boolean setBaseObject) {
        this.setBaseObject = setBaseObject;
    }

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JsonFilterParam that = (JsonFilterParam) o;

        if (setBaseObject != that.setBaseObject) {
            return false;
        }
        if (setCurrentMoment != that.setCurrentMoment) {
            return false;
        }
        if (setCurrentUser != that.setCurrentUser) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (type != null ? !type.equals(that.type) : that.type != null) {
            return false;
        }
        if (value != null ? !value.equals(that.value) : that.value != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (setCurrentMoment ? 1 : 0);
        result = 31 * result + (setCurrentUser ? 1 : 0);
        result = 31 * result + (setBaseObject ? 1 : 0);
        return result;
    }
}
