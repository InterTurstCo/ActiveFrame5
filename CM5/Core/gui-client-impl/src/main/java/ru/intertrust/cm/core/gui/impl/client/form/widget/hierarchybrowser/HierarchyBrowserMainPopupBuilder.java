package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.config.gui.form.widget.RootNodeLinkConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;

import java.util.ArrayList;

public class HierarchyBrowserMainPopupBuilder {
    private EventBus eventBus;
    private ArrayList<HierarchyBrowserItem> chosenItems;
    private int popupWidth;
    private int popupHeight;
    private SelectionStyleConfig selectionStyleConfig;
    private boolean displayAsHyperlinks;
    private RootNodeLinkConfig rootNodeLinkConfig;
    private boolean shouldDisplayTooltipButton;
    public HierarchyBrowserMainPopupBuilder setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
        return this;
    }

    public HierarchyBrowserMainPopupBuilder setChosenItems(ArrayList<HierarchyBrowserItem> chosenItems) {
        this.chosenItems = chosenItems;
        return this;
    }

    public HierarchyBrowserMainPopupBuilder setPopupWidth(int popupWidth) {
        this.popupWidth = popupWidth;
        return this;
    }

    public HierarchyBrowserMainPopupBuilder setPopupHeight(int popupHeight) {
        this.popupHeight = popupHeight;
        return this;
    }

    public HierarchyBrowserMainPopupBuilder setSelectionStyleConfig(SelectionStyleConfig selectionStyleConfig) {
        this.selectionStyleConfig = selectionStyleConfig;
        return this;
    }

    public HierarchyBrowserMainPopupBuilder setDisplayAsHyperlinks(boolean displayAsHyperlinks) {
        this.displayAsHyperlinks = displayAsHyperlinks;
        return this;
    }

    public HierarchyBrowserMainPopupBuilder setRootNodeLinkConfig(RootNodeLinkConfig rootNodeLinkConfig) {
        this.rootNodeLinkConfig = rootNodeLinkConfig;
        return this;
    }

    public HierarchyBrowserMainPopupBuilder setShouldDisplayTooltipButton(boolean shouldDisplayTooltipButton) {
        this.shouldDisplayTooltipButton = shouldDisplayTooltipButton;
        return this;
    }

    public HierarchyBrowserMainPopup createHierarchyBrowserMainPopup() {
        return new HierarchyBrowserMainPopup(eventBus, chosenItems, popupWidth, popupHeight, selectionStyleConfig,
                displayAsHyperlinks, rootNodeLinkConfig, shouldDisplayTooltipButton);
    }
}