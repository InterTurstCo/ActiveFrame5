package ru.intertrust.cm.core.gui.impl.client.form;

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
import ru.intertrust.cm.core.gui.impl.client.util.DisplayStyleBuilder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 01.12.13
 *         Time: 16:15
 */
public class WidgetItemsView extends Composite implements HyperlinkDisplay {
    private AbsolutePanel mainBoxPanel;
    protected AbsolutePanel container;
    private Style.Display displayStyle;
    private EventBus eventBus;
    private boolean tooltipContent;
    private String hyperlinkPopupTitle;

    public WidgetItemsView(SelectionStyleConfig selectionStyleConfig, String hyperlinkPopupTitle) {
        mainBoxPanel = new AbsolutePanel();
        mainBoxPanel.setStyleName("facebook-main-box linkedWidgetsBorderStyle");
        displayStyle = DisplayStyleBuilder.getDisplayStyle(selectionStyleConfig);
        container = new AbsolutePanel();
        container.add(mainBoxPanel);
        this.hyperlinkPopupTitle = hyperlinkPopupTitle;
        initWidget(container);
    }

    public void setTooltipContent(boolean tooltipContent) {
        this.tooltipContent = tooltipContent;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void displayChosenRowItem(Map.Entry<Id, String> entry) {
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
                eventBus.fireEvent(new WidgetItemRemoveEvent(id, tooltipContent));

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
        label.addClickHandler(new HyperlinkClickHandler(id, this, eventBus, tooltipContent, hyperlinkPopupTitle));
        FocusPanel delBtn = new FocusPanel();
        delBtn.addStyleName("facebook-btn facebookElementDel");
        delBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                element.removeFromParent();
                eventBus.fireEvent(new WidgetItemRemoveEvent(id, tooltipContent));

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

    public void displayItems(LinkedHashMap<Id, String> listValues, boolean drawTooltipButton) {
        mainBoxPanel.clear();
        Set<Map.Entry<Id, String>> entries = listValues.entrySet();
        for (Map.Entry<Id, String> entry : entries) {
            displayChosenRowItem(entry);
        }
        if(drawTooltipButton){
            addTooltipButton();
        }

    }

    public void displayHyperlinks(LinkedHashMap<Id, String> listValues, boolean drawTooltipButton) {
        mainBoxPanel.clear();
        Set<Map.Entry<Id, String>> entries = listValues.entrySet();
        for (Map.Entry<Id, String> entry : entries) {
            displayChosenRowItemAsHyperlink(entry);
        }
        if(drawTooltipButton){
            addTooltipButton();
        }

    }
    protected void addTooltipButton() {
        Button openTooltip = new Button("...");
        openTooltip.setStyleName("tooltipButton");
        mainBoxPanel.add(openTooltip);
        openTooltip.addClickHandler(new TooltipButtonClickHandler(eventBus));

    }

    public boolean isEmpty() {
        return mainBoxPanel.getWidgetCount() == 0;
    }

}


