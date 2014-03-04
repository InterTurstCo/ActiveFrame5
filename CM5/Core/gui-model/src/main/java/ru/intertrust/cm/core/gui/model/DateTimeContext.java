package ru.intertrust.cm.core.gui.model;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Sergey.Okolot
 *         Created on 04.03.14 11:31.
 */
public class DateTimeContext implements Dto {
    /**
     * @defaultUID
     */
    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_PATTERN = "dd.MM.yyyy";
    private static final String DEFAULT_TIME_ZONE_ID = "default";

    private String dateTime;
    private String pattern;
    private String timeZoneId;
    /**
     * Used ordinal value of {@link ru.intertrust.cm.core.business.api.dto.FieldType} to optimize traffic.
     * Is readonly always.
     */
    private int ordinalFieldType;
    private boolean changed;

    public String getDateTime() {
        if (dateTime == null || dateTime.isEmpty()) {
            return null;
        } else {
            return dateTime;
        }
    }

    public void setDateTime(final String dateTime) {
        changed = dateTime == null ? this.dateTime != null : !dateTime.equals(this.dateTime);
        this.dateTime = dateTime;
    }

    public int getOrdinalFieldType() {
        return ordinalFieldType;
    }

    public void setOrdinalFieldType(int ordinalFieldType) {
        this.ordinalFieldType = ordinalFieldType;
    }

    public String getPattern() {
        return pattern == null ? DEFAULT_PATTERN : pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getTimeZoneId() {
        return timeZoneId == null ? DEFAULT_TIME_ZONE_ID : timeZoneId;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    public void setChanged(final boolean changed) {
        this.changed = changed;
    }

    public boolean isChanged() {
        return changed;
    }
}
