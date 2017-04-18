package ru.intertrust.cm.core.config.migration;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Java модель конфигурации события миграции
 */
public abstract class AutoMigrationEventConfig implements Dto {
    @ElementListUnion({
            @ElementList(entry = "rename-field", type = RenameFieldConfig.class, inline = true, required = false),
            @ElementList(entry = "change-field-class", type = ChangeFieldClassConfig.class, inline = true, required = false),
            @ElementList(entry="execute", inline=true, type = ExecuteConfig.class, required = false),
            @ElementList(entry="native-command", inline=true, type = NativeCommandConfig.class, required = false),
            @ElementList(entry="create-unique-key", inline=true, type = CreateUniqueKeyConfig.class, required = false),
            @ElementList(entry="make-not-null", inline=true, type = MakeNotNullConfig.class, required = false),
            @ElementList(entry="delete-types", inline=true, type = DeleteTypesConfig.class, required = false),
            @ElementList(entry="delete-fields", inline=true, type = DeleteFieldsConfig.class, required = false),
            @ElementList(entry="unextend-types", inline=true, type = UnextendTypesConfig.class, required = false)
    })
    private List<MigrationScenarioConfig> migrationScenarioConfigs = new ArrayList<>();

    public List<MigrationScenarioConfig> getMigrationScenarioConfigs() {
        return migrationScenarioConfigs;
    }

    public void setMigrationScenarioConfigs(List<MigrationScenarioConfig> migrationScenarioConfigs) {
        this.migrationScenarioConfigs = migrationScenarioConfigs == null ? new ArrayList<MigrationScenarioConfig>(0) : migrationScenarioConfigs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AutoMigrationEventConfig)) return false;

        AutoMigrationEventConfig that = (AutoMigrationEventConfig) o;

        if (migrationScenarioConfigs != null ? !migrationScenarioConfigs.equals(that.migrationScenarioConfigs) : that.migrationScenarioConfigs != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return migrationScenarioConfigs != null ? migrationScenarioConfigs.hashCode() : 0;
    }
}
