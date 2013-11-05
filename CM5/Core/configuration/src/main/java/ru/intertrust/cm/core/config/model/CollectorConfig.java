package ru.intertrust.cm.core.config.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.convert.Convert;

import ru.intertrust.cm.core.config.CollectorSettingsConverter;

/**
 * Подключение алгоритма с интерфейсом ContextRoleCollector.
 * @author atsvetkov
 */
@Root(name = "collector")
public class CollectorConfig {

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
}
