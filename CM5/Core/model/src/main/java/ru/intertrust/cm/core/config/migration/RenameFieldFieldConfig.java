package ru.intertrust.cm.core.config.migration;

import org.simpleframework.xml.Attribute;

/**
 * Java модель конфигурации переименовываемого поля при миграции
 */
public class RenameFieldFieldConfig {

    @Attribute
    private String name;

    @Attribute(name = "new-name")
    private String newName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }
}
