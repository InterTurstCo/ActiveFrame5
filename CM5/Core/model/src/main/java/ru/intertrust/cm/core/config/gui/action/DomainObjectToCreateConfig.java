package ru.intertrust.cm.core.config.gui.action;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Sergey.Okolot
 *         Created on 23.09.2014 17:12.
 */
public class DomainObjectToCreateConfig implements Dto {

    @Attribute(name = "type")
    private String type;

    @Attribute(name = "form-name", required = false)
    private String formName;

    @Element(name = "reference")
    private ReferenceTypeConfig referenceTypeConfig;

    public String getType() {
        return type;
    }

    public String getFormName() {
        return formName;
    }

    public String getReferenceType() {
        return referenceTypeConfig.getType();
    }

    public String getReferenceFieldName() {
        return referenceTypeConfig.getFieldName();
    }
}
