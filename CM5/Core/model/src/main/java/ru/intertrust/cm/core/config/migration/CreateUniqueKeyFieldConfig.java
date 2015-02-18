package ru.intertrust.cm.core.config.migration;

import org.simpleframework.xml.Attribute;

/**
 * Java модель конфигурации поля создаваемого при миграции уникального ключа
 */
public class CreateUniqueKeyFieldConfig {

    @Attribute
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
