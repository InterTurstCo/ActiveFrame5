package ru.intertrust.cm.core.config.migration;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;

import java.util.ArrayList;
import java.util.List;

/**
 * Java модель конфигурации создания уникального ключа при миграции
 */
public class CreateUniqueKeyConfig {

    @Attribute
    private String type;

    @ElementList(entry="field", inline=true)
    private List<CreateUniqueKeyFieldConfig> fields = new ArrayList<>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<CreateUniqueKeyFieldConfig> getFields() {
        return fields;
    }

    public void setFields(List<CreateUniqueKeyFieldConfig> fields) {
        if (fields == null) {
            this.fields.clear();
        } else {
            this.fields = fields;
        }
    }
}
