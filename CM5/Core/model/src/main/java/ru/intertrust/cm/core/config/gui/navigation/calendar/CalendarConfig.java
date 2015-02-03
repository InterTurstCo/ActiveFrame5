package ru.intertrust.cm.core.config.gui.navigation.calendar;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.gui.action.ToolBarConfig;
import ru.intertrust.cm.core.config.gui.navigation.PluginConfig;

/**
 * @author Sergey.Okolot
 *         Created on 27.10.2014 13:07.
 */
@Root(name = "calendar")
public class CalendarConfig extends PluginConfig {
    public static final String COMPONENT_NAME = "calendar.plugin";
    public static final String MONTH_MODE = "month";
    public static final String WEEK_MODE = "week";

    @Attribute(name = "show-weekend", required = false)
    private boolean showWeekend = true;

    @Attribute(name = "start-mode", required = false)
    private String startMode = "month";

    @Attribute(name = "show-detail-panel", required = false)
    private boolean showDetailPanel = true;

    @Element(name = "tool-bar", required = false)
    private ToolBarConfig toolBarConfig;

    @Element(name = "calendar-view")
    private CalendarViewConfig calendarViewConfig;

    public boolean isShowWeekend() {
        return showWeekend;
    }

    public void setShowWeekend(boolean showWeekend) {
        this.showWeekend = showWeekend;
    }

    public String getStartMode() {
        return startMode;
    }

    public void setStartMode(String startMode) {
        this.startMode = startMode;
    }

    public boolean isShowDetailPanel() {
        return showDetailPanel;
    }

    public void setShowDetailPanel(boolean showDetailPanel) {
        this.showDetailPanel = showDetailPanel;
    }

    public ToolBarConfig getToolBarConfig() {
        return toolBarConfig;
    }

    public CalendarViewConfig getCalendarViewConfig() {
        return calendarViewConfig;
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CalendarConfig that = (CalendarConfig) o;
        if (showDetailPanel != that.showDetailPanel) {
            return false;
        }
        if (showWeekend != that.showWeekend) {
            return false;
        }
        if (calendarViewConfig != null ? !calendarViewConfig.equals(that.calendarViewConfig) : that
                .calendarViewConfig != null) {
            return false;
        }
        if (startMode != null ? !startMode.equals(that.startMode) : that.startMode != null) {
            return false;
        }
        if (toolBarConfig != null ? !toolBarConfig.equals(that.toolBarConfig) : that.toolBarConfig != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = (showWeekend ? 1 : 0);
        result = 31 * result + (startMode != null ? startMode.hashCode() : 0);
        result = 31 * result + (showDetailPanel ? 1 : 0);
        result = 31 * result + (toolBarConfig != null ? toolBarConfig.hashCode() : 0);
        result = 31 * result + (calendarViewConfig != null ? calendarViewConfig.hashCode() : 0);
        return result;
    }
}
