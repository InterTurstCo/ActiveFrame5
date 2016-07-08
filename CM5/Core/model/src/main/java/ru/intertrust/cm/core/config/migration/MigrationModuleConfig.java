package ru.intertrust.cm.core.config.migration;

import org.simpleframework.xml.Attribute;
import ru.intertrust.cm.core.business.api.dto.Dto;

public class MigrationModuleConfig implements Dto {

    @Attribute(name = "name", required = true)
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

        MigrationModuleConfig that = (MigrationModuleConfig) o;

        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
