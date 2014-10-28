package ru.intertrust.cm.core.config.gui.navigation.calendar;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import ru.intertrust.cm.core.config.gui.action.ToolBarConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionRefConfig;
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

    public String getStartMode() {
        return startMode;
    }

    public void setStartMode(String startMode) {
        this.startMode = startMode;
    }

    public boolean isShowDetailPanel() {
        return showDetailPanel;
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
}
