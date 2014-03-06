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

    public static final String DTO_PATTERN = "dd.MM.yyyy HH:mm:ss:SSS";
    private static final String DEFAULT_TIME_ZONE_ID = "default";

    private String dateTime;
    private String timeZoneId;
    /**
     * Used ordinal value of {@link ru.intertrust.cm.core.business.api.dto.FieldType} to optimize traffic.
     * Is readonly always.
     */
    private int ordinalFieldType;

    public String getDateTime() {
        if (dateTime == null || dateTime.isEmpty()) {
            return null;
        } else {
            return dateTime;
        }
    }

    public void setDateTime(final String dateTime) {
        this.dateTime = dateTime;
    }

    public int getOrdinalFieldType() {
        return ordinalFieldType;
    }

    public void setOrdinalFieldType(int ordinalFieldType) {
        this.ordinalFieldType = ordinalFieldType;
    }

    public String getTimeZoneId() {
        return timeZoneId == null ? DEFAULT_TIME_ZONE_ID : timeZoneId;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }
}
