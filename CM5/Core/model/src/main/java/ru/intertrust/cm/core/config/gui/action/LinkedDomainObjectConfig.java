package ru.intertrust.cm.core.config.gui.action;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.base.Localizable;
import ru.intertrust.cm.core.config.gui.form.FormMappingConfig;

/**
 * @author Sergey.Okolot
 *         Created on 23.09.2014 17:12.
 */
public class LinkedDomainObjectConfig implements Dto {

    @Attribute(name = "title")
    @Localizable
    private String title;

    @Element(name = "form-mapping")
    private FormMappingConfig formMappingConfig;

    @Element(name = "reference")
    private ReferenceTypeConfig referenceTypeConfig;

    @Element(name = "perform-validation", required = false)
    private BooleanValueConfig performValidation;

    public String getTitle() {
        return title;
    }

    public FormMappingConfig getFormMappingConfig() {
        return formMappingConfig;
    }

    public String getReferenceFieldPath() {
        return referenceTypeConfig.getFieldPath();
    }

    public boolean isPerformValidation() {
        return performValidation == null ? true : performValidation.getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LinkedDomainObjectConfig that = (LinkedDomainObjectConfig) o;

        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        if (formMappingConfig != null ? !formMappingConfig.equals(that.formMappingConfig) : that.formMappingConfig != null)
            return false;
        if (referenceTypeConfig != null ? !referenceTypeConfig.equals(that.referenceTypeConfig) : that.referenceTypeConfig != null)
            return false;
        if (performValidation != null ? !performValidation.equals(that.performValidation) : that.performValidation != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return title != null ? title.hashCode() : 0;
    }
}
