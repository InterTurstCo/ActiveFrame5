package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Created by tbilyi on 18.06.2014.
 */

@Element(name = "field")
public class ActiveFieldConfig implements Dto{

    @Attribute(name = "name", required = false)
    private String name;

    @Attribute(name = "value", required = false)
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ActiveFieldConfig that = (ActiveFieldConfig) o;
        if (name == null ? that.name != null : !name.equals(that.name)){
            return false;
        }
        return value == null ? that.value == null : value.equals(that.value);
    }

    @Override
    public int hashCode() {
        int result = (name == null ? 31 : name.hashCode());
        result = 31 * result + (value == null ? 31 : value.hashCode());
        return result ;
    }

}
