package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.convert.Convert;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.converter.CollectorSettingsConverter;

/**
 * Подключение алгоритма с интерфейсом ContextRoleCollector.
 * @author atsvetkov
 */
@Root(name = "collector")
public class CollectorConfig implements Dto {

    /**
     * имя класса коллектора
     */
    @Attribute(required = true, name = "class-name")
    private String className;

    /**
     * Конфигурация коллектора
     */
    @Element(name = "collector-settings", required = false)
    @Convert(CollectorSettingsConverter.class)
    private CollectorSettings settings;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public CollectorSettings getSettings() {
        return settings;
    }

    public void setSettings(CollectorSettings settings) {
        this.settings = settings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CollectorConfig that = (CollectorConfig) o;

        if (className != null ? !className.equals(that.className) : that.className != null) return false;
        if (settings != null ? !settings.equals(that.settings) : that.settings != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return className != null ? className.hashCode() : 0;
    }
}
