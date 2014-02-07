package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Label;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.01.14
 *         Time: 13:15
 */
public class SimpleNoneEditablePanelWithHyperlinks extends NoneEditablePanel {
    protected EventBus eventBus;

    public SimpleNoneEditablePanelWithHyperlinks(SelectionStyleConfig selectionStyleConfig, EventBus eventBus) {
        super(selectionStyleConfig);
        this.eventBus = eventBus;
    }

    public void displayHyperlink(Id id, String itemRepresentation) {
        Label label = new Label(itemRepresentation);
        label.setStyleName("facebook-label-none-editable");
        label.getElement().getStyle().setCursor(Style.Cursor.POINTER);
        label.addClickHandler(new HyperlinkClickHandler("Collection item", id, eventBus));
        label.getElement().getStyle().setDisplay(displayStyle);
        mainBoxPanel.add(label);
    }

}