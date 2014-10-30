package ru.intertrust.cm.core.gui.impl.client.plugins.calendar;

import java.util.Date;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import ru.intertrust.cm.core.config.gui.navigation.calendar.CalendarConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.model.CalendarTableModel;
import ru.intertrust.cm.core.gui.model.plugin.calendar.CalendarPluginData;

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
        final CalendarPluginData initialData = plugin.getInitialData();
        this.tableModel = new CalendarTableModel((CalendarPlugin) plugin);
        final Date selectedDate = initialData.getSelectedDate();
        this.tableModel.setSelectedDate(selectedDate);
    }

    @Override
    public IsWidget getViewWidget() {
        final Container container = new Container();
        container.add(createBreadCrumbsPanel());
        final CalendarConfig calendarConfig = (CalendarConfig) plugin.getConfig();
        final CalendarPanel calendarPanel;
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
            calendarPanel = new DayPanel(localEventBus, tableModel, calendarConfig);
        }
        container.add(calendarPanel);
        Application.getInstance().hideLoadingIndicator();
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






