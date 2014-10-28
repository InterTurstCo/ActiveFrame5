package ru.intertrust.cm.core.config.gui.navigation.calendar;

import org.simpleframework.xml.Attribute;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Sergey.Okolot
 *         Created on 28.10.2014 16:50.
 */
public class DateFieldFilterConfig implements Dto {

    @Attribute(name = "name")
    private String name;

    public String getName() {
        return name;
    }
}
