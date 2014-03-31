package ru.intertrust.cm.core.config;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;

/**
 * Подключает алгоритм получения доменного объекта, извлекающий из отслеживаемого объекта группу, которая должна
 * войти в группу. Не нужен, если сам отслеживаемый объект представляет эту группу.
 * @author atsvetkov
 */
public class GetGroupConfig extends GetPersonConfig implements Serializable {

    @Attribute(name="name")
    private String groupName;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
