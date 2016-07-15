package ru.intertrust.cm.core.config.migration;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Java модель конфигурации удаляемых при миграции типов
 */
@Root(name = "unextend-types")
public class UnextendTypesConfig extends MigrationScenarioConfig implements Dto {

    @ElementList(entry="type", inline=true)
    private List<UnextendTypesTypeConfig> types = new ArrayList<>();

    public List<UnextendTypesTypeConfig> getTypes() {
        return types;
    }

    public void setTypes(List<UnextendTypesTypeConfig> types) {
        if (types == null) {
            this.types.clear();
        } else {
            this.types = types;
        }
    }
}
