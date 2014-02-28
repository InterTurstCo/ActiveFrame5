package ru.intertrust.cm.core.gui.model.form.widget;

import java.util.Date;

import ru.intertrust.cm.core.business.api.dto.FieldType;

/**
 * @author Denis Mitavskiy
 *         Date: 26.09.13
 *         Time: 12:29
 */
public class DateBoxState extends ValueEditingWidgetState {
    // @default UID
    private static final long serialVersionUID = 1L;

    private Date date;
    private long timeZoneOffset;
    /**
     * Used ordinal value of {@link FieldType} to optimize traffic. Is readonly always.
     */
    private int ordinalFieldType;

    /**
     * Default constructor for serialization.
     */
    protected DateBoxState() {
    }

    public DateBoxState(final int ordinalFieldType) {
        this.ordinalFieldType = ordinalFieldType;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getTimeZoneOffset() {
        return timeZoneOffset;
    }

    public void setTimeZoneOffset(long timeZoneOffset) {
        this.timeZoneOffset = timeZoneOffset;
    }

    public int getOrdinalFieldType() {
        return ordinalFieldType;
    }
// FIXME will be updated
    @Override
    public int hashCode() {
        return date == null ? 17 : date.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Date other = (Date) obj;
        final boolean result = date == null ? other == null : date.equals(other);
        return result;
    }
}
