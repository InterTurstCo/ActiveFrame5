package ru.intertrust.cm.core.config.gui.action;

import org.simpleframework.xml.Attribute;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Sergey.Okolot
 *         Created on 23.09.2014 17:07.
 */
public class BooleanValueConfig implements Dto {

    @Attribute(name = "value")
    private boolean value;

    public boolean getValue() {
        return value;
    }
}
