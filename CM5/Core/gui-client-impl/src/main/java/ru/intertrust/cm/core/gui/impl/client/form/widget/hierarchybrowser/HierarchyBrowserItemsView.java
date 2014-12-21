package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser.HierarchyBrowserCheckBoxUpdateEvent;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser.HierarchyBrowserItemClickEvent;
import ru.intertrust.cm.core.gui.impl.client.util.DisplayStyleBuilder;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 20.12.13
 *         Time: 13:15
 */
public class HierarchyBrowserItemsView extends Composite implements HierarchyBrowserDisplay {
    private AbsolutePanel container;
    private AbsolutePanel mainBoxPanel;
    private EventBus eventBus;
    private Style.Display displayStyle;
    private boolean displayAsHyperlinks;
    private ClickHandler tooltipClickHandler;

    private boolean tooltipContent;
    public HierarchyBrowserItemsView(SelectionStyleConfig selectionStyleConfig, EventBus eventBus,
                                     boolean displayAsHyperlink) {
        this.eventBus = eventBus;
        this.displayAsHyperlinks = displayAsHyperlink;
        mainBoxPanel = new AbsolutePanel();
        mainBoxPanel.setStyleName("hierarchyBrowserMainBox");
        displayStyle = DisplayStyleBuilder.getDisplayStyle(selectionStyleConfig);
        mainBoxPanel.getElement().getStyle().clearOverflow();

        container = new AbsolutePanel();
        container.getElement().getStyle().clearOverflow();
        container.add(mainBoxPanel);
        initWidget(container);
    }

    public void setTooltipContent(boolean tooltipContent) {
        this.tooltipContent = tooltipContent;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    private void displayChosenItem(final HierarchyBrowserItem item) {

        Panel element = createChosenItem(item);
        mainBoxPanel.add(element);
    }

    private Panel createChosenItem(final HierarchyBrowserItem item) {
        final AbsolutePanel element = new AbsolutePanel();
        element.getElement().getStyle().setMarginLeft(5, Style.Unit.PX);
        element.getElement().getStyle().setDisplay(displayStyle);
        element.setStyleName("hierarchyBrowserElement");
        Label label = new Label(item.getStringRepresentation());

        boolean isHyperlink = item.isDisplayAsHyperlinks() == null ? displayAsHyperlinks : item.isDisplayAsHyperlinks();
        label.setStyleName("hierarchyBrowserLabel");
        if (isHyperlink) {

            label.addStyleName("facebook-clickable-label");
            label.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    eventBus.fireEvent(new HierarchyBrowserItemClickEvent(item, HierarchyBrowserItemsView.this, tooltipContent));

                }
            });

        }else{
            label.addStyleName("clearHyperlinkAttributes");

        }
        FocusPanel delBtn = new FocusPanel();
        delBtn.addStyleName("facebook-btn");
        delBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                element.removeFromParent();
                item.setChosen(false);
                eventBus.fireEvent(new HierarchyBrowserCheckBoxUpdateEvent(item));
            }
        });
        element.add(label);
        element.add(delBtn);
        return element;
    }

    public void displayChosenItems(List<HierarchyBrowserItem> chosenItems, boolean shouldDisplayTooltipButton) {
        mainBoxPanel.clear();
        for (HierarchyBrowserItem item : chosenItems) {
            displayChosenItem(item);
        }
        if (shouldDisplayTooltipButton && !tooltipContent) {
            createTooltipLabel();
        }

    }

    public void setTooltipClickHandler(ClickHandler tooltipClickHandler) {
        this.tooltipClickHandler = tooltipClickHandler;
    }

    public void createTooltipLabel() {
        Button openTooltip = new Button("...");
        openTooltip.setStyleName("tooltipButton");
        AbsolutePanel wrapper = new AbsolutePanel();
        wrapper.setStyleName("hierarchyBrowserElement");
        wrapper.add(openTooltip);
        mainBoxPanel.add(wrapper);
        openTooltip.addClickHandler(tooltipClickHandler);
    }

    public void setHeight(String height) {
        container.setHeight(height);
    }

    public boolean isEmpty() {
        return mainBoxPanel.getWidgetCount() == 0;
    }

    @Override
    public void display(List<HierarchyBrowserItem> items, boolean shouldDrawTooltipButton) {
        displayChosenItems(items, shouldDrawTooltipButton);
    }
}


