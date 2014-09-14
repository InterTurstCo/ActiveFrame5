package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.NoneEditablePanel;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.01.14
 *         Time: 13:15
 */
public class HierarchyBrowserNoneEditablePanel extends NoneEditablePanel {
    protected EventBus eventBus;
    private PopupPanel popupPanel;

    public HierarchyBrowserNoneEditablePanel(SelectionStyleConfig selectionStyleConfig, EventBus eventBus) {
        super(selectionStyleConfig);
        this.eventBus = eventBus;
    }

    public void setPopupPanel(PopupPanel popupPanel) {
        this.popupPanel = popupPanel;
    }

    private void displayHyperlink(HierarchyBrowserItem item) {
        AbsolutePanel element = new AbsolutePanel();
        element.addStyleName("facebook-element");
        element.getElement().getStyle().clearOverflow();
        Id id = item.getId();
        String collectionName = item.getNodeCollectionName();
        String itemRepresentation = item.getStringRepresentation();
        Label label = new Label(itemRepresentation);
        label.setStyleName("facebook-label");
        label.addStyleName("facebook-clickable-label");
        label.addClickHandler(new HierarchyBrowserHyperlinkClickHandler("Collection item", id,
                collectionName, eventBus, popupPanel));
        element.getElement().getStyle().setDisplay(displayStyle);
        element.add(label);
        mainBoxPanel.add(element);
    }

    public void displayHierarchyBrowserItems(List<HierarchyBrowserItem> items) {
        for (HierarchyBrowserItem item : items) {
            displayItem(item.getStringRepresentation());
        }
    }

    public void displayHyperlinks(List<HierarchyBrowserItem> items) {
        for (HierarchyBrowserItem item : items) {
            displayHyperlink(item);
        }
    }

    public void addShowTooltipLabel(ClickHandler handler) {
        Button openTooltip = new Button("...");
        openTooltip.setStyleName("tooltipButton");
        mainBoxPanel.add(openTooltip);
        openTooltip.addClickHandler(handler);


    }
}