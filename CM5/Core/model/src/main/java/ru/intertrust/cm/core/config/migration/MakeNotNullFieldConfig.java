package ru.intertrust.cm.core.config.migration;

import org.simpleframework.xml.Attribute;

/**
 * Java модель конфигурации поля, которое объявляется not-null при миграции
 */
public class MakeNotNullFieldConfig {

    @Attribute
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
