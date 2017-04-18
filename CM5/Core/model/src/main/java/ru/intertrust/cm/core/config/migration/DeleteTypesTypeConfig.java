package ru.intertrust.cm.core.config.migration;

import org.simpleframework.xml.Attribute;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Java модель конфигурации удаляемого при миграции типа
 */
public class DeleteTypesTypeConfig implements Dto {

    @Attribute
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeleteTypesTypeConfig that = (DeleteTypesTypeConfig) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
