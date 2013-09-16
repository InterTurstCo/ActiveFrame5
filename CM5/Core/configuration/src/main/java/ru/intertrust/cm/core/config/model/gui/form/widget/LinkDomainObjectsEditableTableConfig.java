package ru.intertrust.cm.core.config.model.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@Root(name = "linked-domain-objects-editable-table")
public class LinkDomainObjectsEditableTableConfig implements Dto {
    @Attribute(name = "id", required = false)
    private String id;

    @Element(name = "field-path", required = false)
    private FieldPathConfig fieldPathConfig;

    @Element(name = "linked-form")
    private LinkedFormConfig linkedFormConfig;

    @Element(name = "form-table")
    private FormTableConfig formTableConfig;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public FieldPathConfig getFieldPathConfig() {
        return fieldPathConfig;
    }

    public void setFieldPathConfig(FieldPathConfig fieldPathConfig) {
        this.fieldPathConfig = fieldPathConfig;
    }

    public LinkedFormConfig getLinkedFormConfig() {
        return linkedFormConfig;
    }

    public void setLinkedFormConfig(LinkedFormConfig linkedFormConfig) {
        this.linkedFormConfig = linkedFormConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LinkDomainObjectsEditableTableConfig that = (LinkDomainObjectsEditableTableConfig) o;

        if (fieldPathConfig != null ? !fieldPathConfig.equals(that.fieldPathConfig) : that.fieldPathConfig != null) {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (linkedFormConfig != null ? !linkedFormConfig.equals(that.linkedFormConfig) : that.linkedFormConfig != null) {
            return false;
        }
        if (formTableConfig != null ? !formTableConfig.equals(that.formTableConfig) : that.
                formTableConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (fieldPathConfig != null ? fieldPathConfig.hashCode() : 0);
        result = 31 * result + (linkedFormConfig != null ? linkedFormConfig.hashCode() : 0);
        result = 31 * result + (formTableConfig != null ? formTableConfig.hashCode() : 0);
        return result;
    }
}



