package ru.intertrust.cm.core.gui.model.plugin.calendar;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Sergey.Okolot
 *         Created on 06.11.2014 15:52.
 */
public class CalendarItemData implements Dto {

    private boolean link;
    private String presentation;

    public CalendarItemData() {
    }

    public CalendarItemData(boolean link, String presentation) {
        this.link = link;
        this.presentation = presentation;
    }

    public boolean isLink() {
        return link;
    }

    public String getPresentation() {
        return presentation;
    }

    @Override
    public String toString() {
        return new StringBuilder(CalendarItemData.class.getSimpleName())
                .append(": isLink=").append(link)
                .append(", presentation=").append(presentation)
                .toString();
    }
}
