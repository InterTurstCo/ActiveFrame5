package ru.intertrust.cm.core.config.migration;

import org.simpleframework.xml.Attribute;

/**
 * Java модель конфигурации поля, в котором меняется тип при миграции
 */
public class ChangeFieldClassFieldConfig {

    @Attribute
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
