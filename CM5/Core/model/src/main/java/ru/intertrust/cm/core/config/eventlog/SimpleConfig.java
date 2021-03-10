package ru.intertrust.cm.core.config.eventlog;


import org.simpleframework.xml.Attribute;
import ru.intertrust.cm.core.business.api.dto.Dto;

public class SimpleConfig implements Dto {

    @Attribute
    private boolean enable;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleConfig that = (SimpleConfig) o;

        if (enable != that.enable) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (enable ? 1 : 0);
    }

    @Override
    public String toString() {
        return "SimpleConfig{" +
                "enable=" + enable +
                '}';
    }
}
