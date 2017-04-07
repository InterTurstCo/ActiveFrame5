package ru.intertrust.cm.core.config.migration;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Java модель конфигурации sql-скрипта миграции
 */
@Root(name = "native-command")
public class NativeCommandConfig extends MigrationScenarioConfig implements Dto {

    @Text(data=true)
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NativeCommandConfig that = (NativeCommandConfig) o;

        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
