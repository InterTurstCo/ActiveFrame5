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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RenameFieldFieldConfig that = (RenameFieldFieldConfig) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (newName != null ? !newName.equals(that.newName) : that.newName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
