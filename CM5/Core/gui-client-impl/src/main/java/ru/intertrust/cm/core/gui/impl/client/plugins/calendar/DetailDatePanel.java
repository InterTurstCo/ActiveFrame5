package ru.intertrust.cm.core.gui.impl.client.plugins.calendar;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.impl.client.event.calendar.CalendarSelectDateListener;
import ru.intertrust.cm.core.gui.impl.client.model.CalendarTableModel;
import ru.intertrust.cm.core.gui.impl.client.model.CalendarTableModelCallback;
import ru.intertrust.cm.core.gui.impl.client.util.GuiUtil;
import ru.intertrust.cm.core.gui.model.plugin.calendar.CalendarItemData;
import ru.intertrust.cm.core.gui.model.plugin.calendar.CalendarItemsData;

import java.util.Date;
import java.util.List;

import static ru.intertrust.cm.core.config.localization.LocalizationKeys.MONTHS;
import static ru.intertrust.cm.core.config.localization.LocalizationKeys.WEEK_DAYS;

/**
 * @author Sergey.Okolot
 *         Created on 05.11.2014 16:51.
 */
public class DetailDatePanel extends FlowPanel implements CalendarSelectDateListener {

    private final CalendarTableModel tableModel;
    private final Panel taskWrapper;
    private HTML header;

    public DetailDatePanel(final CalendarTableModel tableModel, int width, int height) {
        this.tableModel = tableModel;
        header = new HTML();
        header.setWidth("100%");
        add(header);
        taskWrapper = new AbsolutePanel();  // todo add wrapper class;
        taskWrapper.addStyleName("calendarDetailListWrapper");
        taskWrapper.getElement().getStyle().clearPosition();
        taskWrapper.getElement().getStyle().clearOverflow();
        add(taskWrapper);
        getElement().getStyle().setWidth(width - 12, Style.Unit.PX);
        getElement().getStyle().setHeight(height - 11, Style.Unit.PX);
        tableModel.addSelectListener(this);
        updateHeader();
        updateTaskPanels();
    }

    @Override
    public void onSelect(Date date) {
        updateHeader();
        updateTaskPanels();
    }

    @Override
    protected void onDetach() {
        tableModel.removeListener(this);
        super.onDetach();
    }

    private void updateHeader() {
        header.setHTML(tableModel.getSelectedDate().getDate() + " "
                + LocalizeUtil.get(MONTHS[tableModel.getSelectedDate().getMonth()]) + ",<br/>"
                + LocalizeUtil.get(WEEK_DAYS[tableModel.getSelectedDate().getDay()]));
    }

    private void updateTaskPanels() {
        taskWrapper.clear();
        final FlowPanel taskPanel = new FlowPanel();
        final CalendarTableModelCallback callback = new CalendarTableModelCallbackImpl(taskPanel);
        tableModel.fillByDateValues(tableModel.getSelectedDate(), callback);
        taskWrapper.add(taskPanel);
    }

    private static class CalendarTableModelCallbackImpl implements CalendarTableModelCallback {
        private final Panel container;

        private CalendarTableModelCallbackImpl(final Panel container) {
            this.container = container;
        }

        @Override
        public void fillValues(List<CalendarItemsData> values) {
            if (values != null) {
                for (CalendarItemsData calendarItemsData : values) {
                    if (calendarItemsData.getImage() != null) {
                        final Panel wrapper = new AbsolutePanel();
                        wrapper.setStyleName("calendarTaskWrapper");
                        final Image image = new Image(calendarItemsData.getImage());
                        image.setWidth(calendarItemsData.getImageWidth());
                        image.setHeight(calendarItemsData.getImageHeight());
                        wrapper.add(image);
                        addPresentations(calendarItemsData, wrapper);
                        container.add(new HTML("<hr/>"));
                        container.add(wrapper);
                    } else {
                         container.add(new HTML("<hr/>"));
                         addPresentations(calendarItemsData, container);
                    }
                }
            }
        }

        private void addPresentations(CalendarItemsData itemsData, Panel panel) {
            if (itemsData.getDayItems() == null) {
                panel.add(GuiUtil.getCalendarItemPresentation(itemsData.getMonthItem(), itemsData.getRootObjectId()));
            } else {
                for (CalendarItemData itemData: itemsData.getDayItems()) {
                    panel.add(GuiUtil.getCalendarItemPresentation(itemData, itemsData.getRootObjectId()));
                }
            }
        }
    }
}
