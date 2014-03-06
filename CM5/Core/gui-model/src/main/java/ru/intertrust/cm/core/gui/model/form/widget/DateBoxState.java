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
    private static final String DEFAULT_PATTERN = "dd.MM.yyyy";

    private DateTimeContext dateTimeContext;
    private String pattern;
    private boolean displayTimeZoneChoice;

    public DateTimeContext getDateTimeContext() {
        return dateTimeContext;
    }

    public void setDateTimeContext(DateTimeContext dateTimeContext) {
        this.dateTimeContext = dateTimeContext;
    }

    public String getPattern() {
        return pattern == null ? DEFAULT_PATTERN : pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public boolean isDisplayTimeZoneChoice() {
        return displayTimeZoneChoice;
    }

    public void setDisplayTimeZoneChoice(boolean displayTimeZoneChoice) {
        this.displayTimeZoneChoice = displayTimeZoneChoice;
    }

    @Override
    public int hashCode() {
        int result = (dateTimeContext == null ? 17 : dateTimeContext.hashCode());
        result = result * 17 + getPattern().hashCode();
        result = result * 17 + (displayTimeZoneChoice ? 0 : 1);
        return result;
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
        if (dateTimeContext == null ? other.dateTimeContext != null : dateTimeContext.equals(other.dateTimeContext)) {
            return false;
        }
        if (!getPattern().equals(other.getPattern())) {
            return false;
        }
        if (displayTimeZoneChoice != other.displayTimeZoneChoice) {
            return false;
        }
        return true;
    }
}
