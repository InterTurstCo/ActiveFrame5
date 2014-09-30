package ru.intertrust.cm.core.config.gui.action;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.form.FormMappingConfig;

/**
 * @author Sergey.Okolot
 *         Created on 23.09.2014 17:12.
 */
public class DomainObjectToCreateConfig implements Dto {

    @Attribute(name = "title")
    private String title;

    @Element(name = "form-mapping")
    private FormMappingConfig formMappingConfig;

    @Element(name = "reference")
    private ReferenceTypeConfig referenceTypeConfig;

    public String getTitle() {
        return title;
    }

    public FormMappingConfig getFormMappingConfig() {
        return formMappingConfig;
    }

    public String getReferenceType() {
        return referenceTypeConfig.getType();
    }

    public String getReferenceFieldName() {
        return referenceTypeConfig.getFieldName();
    }
}
