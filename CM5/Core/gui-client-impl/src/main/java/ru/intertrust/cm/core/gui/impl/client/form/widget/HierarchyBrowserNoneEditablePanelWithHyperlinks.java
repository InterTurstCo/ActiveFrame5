package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.user.client.ui.Label;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.01.14
 *         Time: 13:15
 */
public class HierarchyBrowserNoneEditablePanelWithHyperlinks extends NoneEditablePanel {
    protected EventBus eventBus;

    public HierarchyBrowserNoneEditablePanelWithHyperlinks(SelectionStyleConfig selectionStyleConfig, EventBus eventBus) {
        super(selectionStyleConfig);
        this.eventBus = eventBus;
    }

    public void displayHyperlink(HierarchyBrowserItem item) {
        Id id = item.getId();
        String collectionName = item.getNodeCollectionName();
        String itemRepresentation = item.getStringRepresentation();
        Label label = new Label(itemRepresentation);
        label.setStyleName("facebook-label-none-editable");
        label.addClickHandler(new HierarchyBrowserHyperlinkClickHandler("Collection item", id, collectionName, eventBus));
        label.getElement().getStyle().setDisplay(displayStyle);
        mainBoxPanel.add(label);
    }
}