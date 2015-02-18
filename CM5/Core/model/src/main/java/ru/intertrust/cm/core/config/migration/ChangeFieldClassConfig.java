package ru.intertrust.cm.core.config.migration;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;

import java.util.ArrayList;
import java.util.List;

/**
 * Java модель конфигурации изменения типа поля при миграции
 */
public class ChangeFieldClassConfig {

    @Attribute
    private String type;

    @ElementList(entry="field", inline=true)
    private List<ChangeFieldClassFieldConfig> fields = new ArrayList<>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<ChangeFieldClassFieldConfig> getFields() {
        return fields;
    }

    public void setFields(List<ChangeFieldClassFieldConfig> fields) {
        if (fields == null) {
            this.fields.clear();
        } else {
            this.fields = fields;
        }
    }
}
