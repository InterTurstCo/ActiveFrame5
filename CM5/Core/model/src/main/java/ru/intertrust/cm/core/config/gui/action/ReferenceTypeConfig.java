package ru.intertrust.cm.core.config.gui.action;

import org.simpleframework.xml.Attribute;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Sergey.Okolot
 *         Created on 23.09.2014 17:13.
 */
public class ReferenceTypeConfig implements Dto {

    @Attribute(name = "field-path")
    private String fieldPath;

    public String getFieldPath() {
        return fieldPath;
    }
}
