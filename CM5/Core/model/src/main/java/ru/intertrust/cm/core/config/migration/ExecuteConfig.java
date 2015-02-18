package ru.intertrust.cm.core.config.migration;

import org.simpleframework.xml.Attribute;

/**
 * Java модель конфигурации комонента миграции
 */
public class ExecuteConfig {

    @Attribute(name="component-name")
    private String componentName;

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }
}
