package ru.intertrust.cm.core.config.gui.navigation.calendar;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.form.widget.PatternConfig;

/**
 * @author Sergey.Okolot
 *         Created on 06.11.2014 14:08.
 */
public class CalendarItemConfig implements Dto {

    @Attribute(name = "type")
    private String type;

    @Element(name = "pattern")
    private PatternConfig pattern;

    public boolean isLink() {
        return "link".equals(type);
    }

    public PatternConfig getPattern() {
        return pattern;
    }
}
