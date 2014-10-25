package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Label;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser.HierarchyBrowserItemClickEvent;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser.HierarchyBrowserShowTooltipEvent;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.NoneEditablePanel;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.01.14
 *         Time: 13:15
 */
public class HierarchyBrowserNoneEditablePanel extends NoneEditablePanel implements HierarchyBrowserDisplay {

    private boolean tooltipContent;
    private boolean displayAsHyperlinks;
    public HierarchyBrowserNoneEditablePanel(SelectionStyleConfig selectionStyleConfig, EventBus eventBus, boolean displayAsHyperlinks) {
        super(selectionStyleConfig, eventBus);
        this.displayAsHyperlinks = displayAsHyperlinks;

    }
    public void displayItems(List<HierarchyBrowserItem> items, boolean drawTooltipButton){
        mainBoxPanel.clear();
        for (HierarchyBrowserItem item : items) {
            boolean isHyperlink = item.isDisplayAsHyperlinks() == null ? displayAsHyperlinks : item.isDisplayAsHyperlinks();
            if(isHyperlink){
                displayHyperlink(item);
            }else {
                displayItem(item.getStringRepresentation());
            }
        }
        if(drawTooltipButton){
            addTooltipButton();
        }

    }

    public void setTooltipContent(boolean tooltipContent) {
        this.tooltipContent = tooltipContent;
    }

    private void displayHyperlink(final HierarchyBrowserItem item) {
        AbsolutePanel element = new AbsolutePanel();
        element.addStyleName("facebook-element");
        element.getElement().getStyle().clearOverflow();

        String itemRepresentation = item.getStringRepresentation();
        Label label = new Label(itemRepresentation);
        label.setStyleName("facebook-label");
        label.addStyleName("facebook-clickable-label");
        label.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                eventBus.fireEvent(new HierarchyBrowserItemClickEvent(item, HierarchyBrowserNoneEditablePanel.this, tooltipContent));

            }
        });
        element.getElement().getStyle().setDisplay(displayStyle);
        element.add(label);
        mainBoxPanel.add(element);
    }

    public void displayHierarchyBrowserItems(List<HierarchyBrowserItem> items, boolean drawTooltipButton) {
        mainBoxPanel.clear();
        for (HierarchyBrowserItem item : items) {
            displayItem(item.getStringRepresentation());
        }
        if(drawTooltipButton){
            addTooltipButton();
        }
    }

    public void display(List<HierarchyBrowserItem> items, boolean drawTooltipButton) {
       displayItems(items, drawTooltipButton);

    }

    @Override
    protected ClickHandler getTooltipClickHandler() {
        return new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                eventBus.fireEvent(new HierarchyBrowserShowTooltipEvent(null));
            }
        };
    }
}