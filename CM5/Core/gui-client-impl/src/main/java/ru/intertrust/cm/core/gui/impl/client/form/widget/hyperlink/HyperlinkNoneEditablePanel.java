package ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.01.14
 *         Time: 13:15
 */
public class HyperlinkNoneEditablePanel extends NoneEditablePanel {
    protected EventBus eventBus;
    private PopupPanel popupPanel;

    public HyperlinkNoneEditablePanel(SelectionStyleConfig selectionStyleConfig, EventBus eventBus) {
        super(selectionStyleConfig);
        this.eventBus = eventBus;
    }

    public void setPopupPanel(PopupPanel popupPanel) {
        this.popupPanel = popupPanel;
    }

    public void displayHyperlink(Id id, String itemRepresentation) {
        AbsolutePanel element = new AbsolutePanel();
        element.addStyleName("facebook-element");
        Label label = new Label(itemRepresentation);
        label.setStyleName("facebook-label");
        label.addStyleName("facebook-clickable-label");
        label.addClickHandler(new HyperlinkClickHandler(id, popupPanel, eventBus));
        element.getElement().getStyle().setDisplay(displayStyle);
        element.add(label);
        if (displayStyle.equals(Style.Display.INLINE_BLOCK)) {
            element.getElement().getStyle().setFloat(Style.Float.LEFT);
            label.getElement().getStyle().setFloat(Style.Float.LEFT);

        }
        mainBoxPanel.add(element);
    }

    public void displayHyperlinks(LinkedHashMap<Id, String> listValues) {
        Set<Map.Entry<Id, String>> entries = listValues.entrySet();
        for (Map.Entry<Id, String> entry : entries) {
            Id id = entry.getKey();
            String representation = entry.getValue();
            displayHyperlink(id, representation);
        }
    }

    public void addShowTooltipLabel(ClickHandler handler) {
        Button openTooltip = new Button("...");
        openTooltip.setStyleName("light-button");
        container.add(openTooltip);
        openTooltip.addClickHandler(handler);

    }
}