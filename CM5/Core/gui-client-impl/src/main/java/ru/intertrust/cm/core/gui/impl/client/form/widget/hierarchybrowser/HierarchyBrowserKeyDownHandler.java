package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.api.client.WidgetNavigator;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser.HierarchyBrowserCheckBoxUpdateEvent;
import ru.intertrust.cm.core.gui.impl.client.form.widget.linkediting.AbstractWidgetDelegatedKeyDownHandler;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 09.01.2015
 *         Time: 8:57
 */
public class HierarchyBrowserKeyDownHandler extends AbstractWidgetDelegatedKeyDownHandler<HierarchyBrowserItemPanel> {
    private static final String HIGHLIGHTED_STYLE_CLASS_NAME = "highlightedHierarchyBrowserElement";
    public HierarchyBrowserKeyDownHandler(WidgetNavigator<HierarchyBrowserItemPanel> widgetNavigator, EventBus eventBus) {
        super(widgetNavigator, eventBus);
    }

    @Override
    public void handleBackspaceOrDeleteDown() {
        if (widgetNavigator.getCurrent() != null) {
            HierarchyBrowserItemPanel lastSelectionItem = widgetNavigator.getCurrent();
            lastSelectionItem.removeFromParent();
            HierarchyBrowserItem item = lastSelectionItem.getItem();
            item.setChosen(false);
            eventBus.fireEvent(new HierarchyBrowserCheckBoxUpdateEvent(item));
        }
        widgetNavigator.back();
        changeHighlighting(true);
    }

    protected String getOrdinalStyleClassName() {
        return HierarchyBrowserItemsView.ITEM_STYLE_CLASS_NAME;
    }
    protected String getHighlightedStyleClassName() {
        return HIGHLIGHTED_STYLE_CLASS_NAME;
    }
}
