package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

public class LinkedTablePatternConfig implements Dto {

    @Attribute(name = "value")
    private String value;

    @Element(name = "domain-object-types", required = false)
    private PatternDomainObjectTypesConfig patternDomainObjectTypesConfig;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public PatternDomainObjectTypesConfig getPatternDomainObjectTypesConfig() {
        return patternDomainObjectTypesConfig;
    }

    public void setPatternDomainObjectTypesConfig(PatternDomainObjectTypesConfig patternDomainObjectTypesConfig) {
        this.patternDomainObjectTypesConfig = patternDomainObjectTypesConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LinkedTablePatternConfig that = (LinkedTablePatternConfig) o;

        if (value != null ? !value.equals(that.value) : that.value != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}

