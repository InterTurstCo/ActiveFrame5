package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.util.ModelUtil;
import ru.intertrust.cm.core.config.gui.form.widget.datebox.DateBoxConfig;
import ru.intertrust.cm.core.gui.model.DateTimeContext;

/**
 * @author Denis Mitavskiy
 *         Date: 26.09.13
 *         Time: 12:29
 */
public class DateBoxState extends WidgetState {
    // @default UID
    private static final long serialVersionUID = 1L;

    private DateTimeContext dateTimeContext;
    private String pattern;
    private boolean displayTimeZoneChoice;
    private boolean displayTime;
    private DateBoxConfig dateBoxConfig;

    public DateTimeContext getDateTimeContext() {
        return dateTimeContext;
    }

    public void setDateTimeContext(DateTimeContext dateTimeContext) {
        this.dateTimeContext = dateTimeContext;
    }

    public String getPattern() {
        return pattern == null ? ModelUtil.DEFAULT_DATE_PATTERN : pattern;
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

    public boolean isDisplayTime() {
        return displayTime;
    }

    public void setDisplayTime(boolean displayTime) {
        this.displayTime = displayTime;
    }

    public DateBoxConfig getDateBoxConfig() {
        return dateBoxConfig;
    }

    public void setDateBoxConfig(DateBoxConfig dateBoxConfig) {
        this.dateBoxConfig = dateBoxConfig;
    }

    @Override
    public int hashCode() {
        int result = (dateTimeContext == null ? 17 : dateTimeContext.hashCode());
        result = result * 17 + getPattern().hashCode();
        result = result * 17 + (displayTimeZoneChoice ? 0 : 1);
        result = result * 17 + (displayTime ? 0 : 1);
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
        if (displayTime != other.displayTime) {
            return false;
        }
        if (dateBoxConfig == null ? other.dateBoxConfig != null : dateBoxConfig.equals(other.dateBoxConfig)) {
            return false;
        }
        return true;
    }
}
