package ru.intertrust.cm.core.config.migration;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Java модель конфигурации удаляемых при миграции типов
 */
@Root(name = "delete-types")
public class DeleteTypesConfig extends MigrationScenarioConfig implements Dto {

    @ElementList(entry="type", inline=true)
    private List<DeleteTypesTypeConfig> types = new ArrayList<>();

    public List<DeleteTypesTypeConfig> getTypes() {
        return types;
    }

    public void setTypes(List<DeleteTypesTypeConfig> types) {
        if (types == null) {
            this.types.clear();
        } else {
            this.types = types;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeleteTypesConfig that = (DeleteTypesConfig) o;

        if (types != null ? !types.equals(that.types) : that.types != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return types != null ? types.hashCode() : 0;
    }
}
