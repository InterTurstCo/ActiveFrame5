package ru.intertrust.cm.core.config.gui.navigation;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Created by IPetrov on 06.03.14.
 */
@Root(name = "group-name")
public class GroupConfig implements Dto {
    @Attribute(name = "value")
    private String value;

    @Attribute(name = "priority", required = false)
    private Integer priority;

    public String getName() {
        return value;
    }

    public void setName(String value) {
        this.value = value;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GroupConfig that = (GroupConfig) o;
        if (priority != null ? !priority.equals(that.priority) : that.priority != null) {
            return false;
        }
        if (value != null ? !value.equals(that.value) : that.value != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (priority != null ? priority.hashCode() : 0);
        return result;
    }
}
