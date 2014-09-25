package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Text;

public abstract class FindNotificationContextObjectsConfigBase extends FindNotificationContextObjectsConfig{
    @Text
    private String data;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FindNotificationContextObjectsConfigBase that = (FindNotificationContextObjectsConfigBase) o;

        if (data != null ? !data.equals(that.data) : that.data != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return data != null ? data.hashCode() : 0;
    }
}
