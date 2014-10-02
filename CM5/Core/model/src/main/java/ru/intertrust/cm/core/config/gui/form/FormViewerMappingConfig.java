package ru.intertrust.cm.core.config.gui.form;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.form.widget.FieldPathConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 11.08.14
 *         Time: 13:57
 */
@Root(name = "form-mapping")
public class FormViewerMappingConfig implements Dto {
    @Attribute(name = "form", required = true)
    private String form;

    @Attribute(name = "domain-object-type", required = true)
    private String domainObjectType;

    @ElementList(name = "field-path", inline = true, required = false)
    List<FieldPathConfig> fieldPathConfigs = new ArrayList<>();

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getDomainObjectType() {
        return domainObjectType;
    }

    public void setDomainObjectType(String domainObjectType) {
        this.domainObjectType = domainObjectType;
    }

    public List<FieldPathConfig> getFieldPathConfigs() {
        return fieldPathConfigs;
    }

    public void setFieldPathConfigs(List<FieldPathConfig> fieldPathConfigs) {
        this.fieldPathConfigs = fieldPathConfigs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FormViewerMappingConfig that = (FormViewerMappingConfig) o;
        if (!domainObjectType.equals(that.domainObjectType)) {
            return false;
        }
        if (!form.equals(that.form)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = form.hashCode();
        result = 31 * result + domainObjectType.hashCode();
        return result;
    }
}
