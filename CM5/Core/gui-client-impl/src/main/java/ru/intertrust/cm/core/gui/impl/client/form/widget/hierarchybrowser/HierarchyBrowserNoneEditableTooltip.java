package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.user.client.ui.PopupPanel;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;

import java.util.ArrayList;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 08.07.2014
 *         Time: 21:34
 */
public class HierarchyBrowserNoneEditableTooltip extends PopupPanel {
    private EventBus eventBus;
    private HierarchyBrowserNoneEditablePanel widgetItemsView;
    private boolean displayAsHyperlinks;
    private String hyperlinkPopupTitle;

    public HierarchyBrowserNoneEditableTooltip(SelectionStyleConfig selectionStyleConfig, EventBus eventBus,
                                               boolean displayAsHyperlinks, String hyperlinkPopupTitle) {
        super(true);
        this.eventBus = eventBus;
        this.displayAsHyperlinks = displayAsHyperlinks;
        this.hyperlinkPopupTitle = hyperlinkPopupTitle;
        init(selectionStyleConfig);

    }

    private void init(SelectionStyleConfig selectionStyleConfig) {
        widgetItemsView = new HierarchyBrowserNoneEditablePanel(selectionStyleConfig, eventBus, hyperlinkPopupTitle);
        widgetItemsView.setPopupPanel(this);
        this.add(widgetItemsView);
        this.setStyleName("tooltip-popup");

    }

    public void displayItems(ArrayList<HierarchyBrowserItem> items, boolean drawTooltipButton) {
        if (displayAsHyperlinks) {
            widgetItemsView.displayHyperlinks(items, drawTooltipButton);
        } else {
            widgetItemsView.displayHierarchyBrowserItems(items, drawTooltipButton);
        }

    }
}