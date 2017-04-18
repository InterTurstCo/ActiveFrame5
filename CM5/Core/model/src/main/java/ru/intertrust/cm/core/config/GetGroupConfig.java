package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;

import java.io.Serializable;

/**
 * Подключает алгоритм получения доменного объекта, извлекающий из отслеживаемого объекта группу, которая должна
 * войти в группу. Не нужен, если сам отслеживаемый объект представляет эту группу.
 * @author atsvetkov
 */
public class GetGroupConfig extends GetPersonConfig implements Serializable {

    @Attribute(name="name", required=false)
    private String groupName;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        GetGroupConfig that = (GetGroupConfig) o;

        if (groupName != null ? !groupName.equals(that.groupName) : that.groupName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (groupName != null ? groupName.hashCode() : 0);
        return result;
    }
}
