package ru.intertrust.cm.core.config.migration;

import org.simpleframework.xml.Text;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Java модель конфигурации sql-скрипта миграции
 */
public class NativeCommandConfig implements Dto {

    @Text(data=true)
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
