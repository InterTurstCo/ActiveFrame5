package ru.intertrust.cm.core.gui.impl.client.form.widget.tablebrowser;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.impl.client.event.tooltip.WidgetItemRemoveEvent;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.HyperlinkClickHandler;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.HyperlinkDisplay;
import ru.intertrust.cm.core.gui.impl.client.form.widget.tooltip.TooltipButtonClickHandler;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.impl.client.util.DisplayStyleBuilder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 25.08.2014
 *         Time: 17:44
 */
public class TableBrowserItemsView extends Composite implements HyperlinkDisplay {
    private static final int INPUT_MARGIN = 40;
    private TextBox filter;
    private AbsolutePanel mainBoxPanel;
    private Style.Display displayStyle;
    private Button openTooltip;
    private EventBus eventBus;
    private String hyperlinkPopupTitle;

    public TableBrowserItemsView(SelectionStyleConfig selectionStyleConfig, EventBus eventBus, String hyperlinkPopupTitle) {
        this.eventBus = eventBus;
        this.hyperlinkPopupTitle = hyperlinkPopupTitle;
        mainBoxPanel = new AbsolutePanel();
        mainBoxPanel.setStyleName("facebook-main-box linkedWidgetsBorderStyle");
        displayStyle = DisplayStyleBuilder.getDisplayStyle(selectionStyleConfig);
        initWidget(mainBoxPanel);
    }

    public void addFocusedFilter() {
        filter = new TextBox();
        filter.setStyleName("tableBrowserFilterInput");
        mainBoxPanel.add(filter);
        changeInputFilterWidth();
        filter.setFocus(true);
    }

    public String getFilterValue() {
        return filter.getValue();
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    private void displayChosenRowItem(Map.Entry<Id, String> entry) {
        final AbsolutePanel element = new AbsolutePanel();

        element.setStyleName("facebook-element");
        Label label = new Label(entry.getValue());
        label.setStyleName("facebook-label");
        label.addStyleName("facebook-clickable-label");
        FocusPanel delBtn = new FocusPanel();
        delBtn.addStyleName("facebook-btn facebookElementDel");
        final Id id = entry.getKey();
        delBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                element.removeFromParent();
                eventBus.fireEvent(new WidgetItemRemoveEvent(id, false));
            }
        });
        element.add(label);
        element.add(delBtn);
        if (displayStyle.equals(Style.Display.INLINE_BLOCK)) {
            element.getElement().getStyle().setFloat(Style.Float.LEFT);
            label.getElement().getStyle().setFloat(Style.Float.LEFT);
            delBtn.getElement().getStyle().setFloat(Style.Float.LEFT);
        }
        mainBoxPanel.add(element);
    }

    private void displayChosenRowItemAsHyperlink(Map.Entry<Id, String> entry) {
        final AbsolutePanel element = new AbsolutePanel();

        element.setStyleName("facebook-element");
        Label label = new Label(entry.getValue());
        label.setStyleName("facebook-label");
        label.addStyleName("facebook-clickable-label");
        final Id id = entry.getKey();
        label.addClickHandler(new HyperlinkClickHandler(id, this, eventBus, false, hyperlinkPopupTitle));
        FocusPanel delBtn = new FocusPanel();
        delBtn.addStyleName("facebook-btn facebookElementDel");
        delBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                element.removeFromParent();
                eventBus.fireEvent(new WidgetItemRemoveEvent(id, false));
            }
        });
        element.add(label);
        element.add(delBtn);
        if (displayStyle.equals(Style.Display.INLINE_BLOCK)) {
            element.getElement().getStyle().setFloat(Style.Float.LEFT);
            label.getElement().getStyle().setFloat(Style.Float.LEFT);
            delBtn.getElement().getStyle().setFloat(Style.Float.LEFT);
        }
        mainBoxPanel.add(element);
    }

    public void displayItems(LinkedHashMap<Id, String> items, boolean displayTooltipButton) {
        mainBoxPanel.clear();
        Set<Map.Entry<Id, String>> entries = items.entrySet();
        for (Map.Entry<Id, String> entry : entries) {
            displayChosenRowItem(entry);
        }
        if (displayTooltipButton) {
            addTooltipButton();
        }
        addFocusedFilter();

    }

    public void displayHyperlinks(LinkedHashMap<Id, String> items, boolean displayTooltipButton) {
        mainBoxPanel.clear();
        Set<Map.Entry<Id, String>> entries = items.entrySet();
        for (Map.Entry<Id, String> entry : entries) {
            displayChosenRowItemAsHyperlink(entry);
        }
        if (displayTooltipButton) {
            addTooltipButton();
        }
        addFocusedFilter();

    }

    public void clearFilterInput() {
        filter.setValue(BusinessUniverseConstants.EMPTY_VALUE);
    }

    private void addTooltipButton() {
        openTooltip = new Button("...");
        openTooltip.setStyleName("tooltipButton");
        mainBoxPanel.add(openTooltip);
        openTooltip.addClickHandler(new TooltipButtonClickHandler(eventBus));

    }

    public void removeTooltipButton() {
        openTooltip.removeFromParent();
    }

    private void changeInputFilterWidth() {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                Widget lastWidget = getWidgetBeforeInput();
                if (lastWidget == null || lastWidget.getElement() == null) {
                    return;
                }
                int width = mainBoxPanel.getElement().getAbsoluteRight()
                        - lastWidget.getElement().getAbsoluteRight() - INPUT_MARGIN;
                if (width > 0) {
                    filter.setWidth(width + Style.Unit.PX.name());
                }

            }
        });
    }

    private Widget getWidgetBeforeInput() {
        int count = mainBoxPanel.getWidgetCount();
        Widget result = null;
        if (count >= 2) {
            result = mainBoxPanel.getWidget(count - 2);
        }
        return result;
    }

    public void clearItems(){
        mainBoxPanel.clear();
        mainBoxPanel.add(filter);
    }
}
