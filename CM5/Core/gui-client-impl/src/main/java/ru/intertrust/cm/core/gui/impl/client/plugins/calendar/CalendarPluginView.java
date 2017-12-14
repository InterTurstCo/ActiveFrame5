package ru.intertrust.cm.core.gui.impl.client.plugins.calendar;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.config.gui.navigation.calendar.CalendarConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.history.HistoryManager;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.model.CalendarTableModel;

/**
 * Created by lvov on 03.04.14.
 */
public class CalendarPluginView extends PluginView {

    private EventBus localEventBus;
    private CalendarTableModel tableModel;

    /**
     * Основной конструктор
     *
     * @param plugin плагин, являющийся по сути, контроллером (или представителем) в паттерне MVC
     */
    protected CalendarPluginView(final Plugin plugin, final EventBus localEventBus) {
        super(plugin);
        this.localEventBus = localEventBus;
        this.tableModel = new CalendarTableModel((CalendarPlugin) plugin);
        Application.getInstance().getHistoryManager().setMode(HistoryManager.Mode.APPLY, CalendarPlugin.class.getSimpleName());
    }

    @Override
    public IsWidget getViewWidget() {
        final Container container = new Container();
        container.add(createBreadCrumbsPanel());
        final CalendarConfig calendarConfig = (CalendarConfig) plugin.getConfig();
        final AbstractCalendarPanel calendarPanel;
        if (CalendarConfig.MONTH_MODE.equals(calendarConfig.getStartMode())) {
            final SimplePanel cursor = new SimplePanel();
            cursor.setStyleName("current-month-block");
            cursor.getElement().getStyle().setWidth(MonthScrollPanel.MONTH_SCROLL_ITEM_WIDTH, Style.Unit.PX);
            container.add(cursor);
            final MonthScrollPanel monthScrollPanel =
                    new MonthScrollPanel(localEventBus, tableModel.getSelectedDate());
            container.add(monthScrollPanel);
            calendarPanel = new MonthPanel(localEventBus, tableModel, calendarConfig);
        } else {
            calendarPanel = new WeekPanel(localEventBus, tableModel, calendarConfig);
        }
        container.add(calendarPanel);
        Application.getInstance().unlockScreen();
        return container;
    }

    private static class Container extends FlowPanel implements RequiresResize {

        private HandlerRegistration resizeHandler;

        @Override
        protected void onLoad() {
            super.onLoad();
            resizeHandler = Window.addResizeHandler(new ResizeHandler() {
                @Override
                public void onResize(ResizeEvent event) {
                    Container.this.onResize();
                }
            });
        }

        @Override
        protected void onDetach() {
            if (resizeHandler != null) {
                resizeHandler.removeHandler();
                resizeHandler = null;
            }
            super.onDetach();
        }

        @Override
        public void onResize() {
            for (Widget widget : getChildren()) {
                if (widget instanceof RequiresResize) {
                    final RequiresResize requiresResize = (RequiresResize) widget;
                    requiresResize.onResize();
                }
            }
        }
    }
}






