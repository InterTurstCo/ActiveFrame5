package ru.intertrust.cm.core.config.migration;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Java модель конфигурации переименовываемого поля при миграции
 */
@Root(name="field")
public class RenameFieldFieldConfig implements Dto {

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
