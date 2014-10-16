package ru.intertrust.cm.core.gui.model.plugin.calendar;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * FIXME temporary class.
 *
 * @author Sergey.Okolot
 *         Created on 13.10.2014 15:44.
 */
public class CalendarData implements Dto {

    private String task;
    private String description;

    public CalendarData() {
    }

    public CalendarData(String task, String description) {
        this.task = task;
        this.description = description;
    }

    public String getTask() {
        return task;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return new StringBuilder(CalendarData.class.getSimpleName())
                .append(": task='").append(task)
                .append("', description='").append(description)
                .append("'").toString();
    }
}
