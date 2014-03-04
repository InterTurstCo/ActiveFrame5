package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.gui.model.DateTimeContext;

/**
 * @author Denis Mitavskiy
 *         Date: 26.09.13
 *         Time: 12:29
 */
public class DateBoxState extends ValueEditingWidgetState {
    // @default UID
    private static final long serialVersionUID = 1L;

    private DateTimeContext dateTimeContext;

    public DateTimeContext getDateTimeContext() {
        return dateTimeContext;
    }

    public void setDateTimeContext(DateTimeContext dateTimeContext) {
        this.dateTimeContext = dateTimeContext;
    }

    @Override
    public int hashCode() {
        return dateTimeContext == null ? 17 : dateTimeContext.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final DateBoxState other = (DateBoxState) obj;
        final boolean result = dateTimeContext == null
                ? other.dateTimeContext == null
                : dateTimeContext.equals(other.dateTimeContext);
        return result;
    }
}
