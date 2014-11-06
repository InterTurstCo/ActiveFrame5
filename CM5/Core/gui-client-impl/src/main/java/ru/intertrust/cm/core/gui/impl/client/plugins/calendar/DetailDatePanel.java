package ru.intertrust.cm.core.gui.impl.client.plugins.calendar;

import java.util.Date;
import java.util.List;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import ru.intertrust.cm.core.gui.impl.client.event.calendar.CalendarSelectDateListener;
import ru.intertrust.cm.core.gui.impl.client.model.CalendarTableModel;
import ru.intertrust.cm.core.gui.impl.client.model.CalendarTableModelCallback;
import ru.intertrust.cm.core.gui.impl.client.util.GuiUtil;
import ru.intertrust.cm.core.gui.model.plugin.calendar.CalendarItemData;
import ru.intertrust.cm.core.gui.model.plugin.calendar.CalendarItemsData;

/**
 * @author Sergey.Okolot
 *         Created on 05.11.2014 16:51.
 */
public class DetailDatePanel extends FlowPanel implements CalendarSelectDateListener {

    private final CalendarTableModel tableModel;
    private final VerticalPanel taskWrapper;
    private HTML header;

    public DetailDatePanel(final CalendarTableModel tableModel, int width, int height) {
        this.tableModel = tableModel;
        header = new HTML();
        header.setWidth("100%");
        add(header);
        taskWrapper = new VerticalPanel();  // todo add wrapper class;
        taskWrapper.setWidth("100%");
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
                + GuiUtil.MONTHS[tableModel.getSelectedDate().getMonth()] + ",<br/>"
                + GuiUtil.WEEK_DAYS[tableModel.getSelectedDate().getDay()]);
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
                    final Widget taskItem;
                    if (calendarItemsData.getImage() != null) {
                        final HorizontalPanel wrapper = new HorizontalPanel();
                        final Image image = new Image(calendarItemsData.getImage());
                        image.setWidth(calendarItemsData.getImageWidth());
                        image.setHeight(calendarItemsData.getImageHeight());
                        wrapper.add(image);
                        wrapper.add(getPresentations(calendarItemsData));
                        taskItem = wrapper;
                    } else {
                        taskItem = getPresentations(calendarItemsData);
                    }
                    container.add(new HTML("<hr/>"));
                    container.add(taskItem);
                }
            }
        }

        private Widget getPresentations(CalendarItemsData itemsData) {
            final VerticalPanel result = new VerticalPanel();
            if (itemsData.getDayItems() == null) {
                result.add(GuiUtil.getCalendarItemPresentation(itemsData.getMonthItem(), itemsData.getRootObjectId()));
            } else {
                for (CalendarItemData itemData: itemsData.getDayItems()) {
                    result.add(GuiUtil.getCalendarItemPresentation(itemData, itemsData.getRootObjectId()));
                }
            }
            return result;
        }
    }
}
