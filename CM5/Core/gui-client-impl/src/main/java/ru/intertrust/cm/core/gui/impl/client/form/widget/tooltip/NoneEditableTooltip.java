package ru.intertrust.cm.core.gui.impl.client.form.widget.tooltip;

import com.google.gwt.user.client.ui.PopupPanel;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.HasLinkedFormMappings;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.HyperlinkNoneEditablePanel;

import java.util.LinkedHashMap;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 30.06.2014
 *         Time: 13:52
 */
public class NoneEditableTooltip extends PopupPanel {
    private EventBus eventBus;
    private HyperlinkNoneEditablePanel hyperlinkNoneEditablePanel;
    private boolean displayAsHyperlinks;
    private HasLinkedFormMappings widget;

    public NoneEditableTooltip(SelectionStyleConfig selectionStyleConfig, EventBus eventBus,
                               boolean displayAsHyperlinks, String hyperlinkPopupTitle, HasLinkedFormMappings widget) {

        super(true);
        this.eventBus = eventBus;
        this.displayAsHyperlinks = displayAsHyperlinks;
        this.widget = widget;
        init(selectionStyleConfig, hyperlinkPopupTitle);
    }

    private void init(SelectionStyleConfig selectionStyleConfig, String hyperlinkPopupTitle) {
        hyperlinkNoneEditablePanel = new HyperlinkNoneEditablePanel(selectionStyleConfig, eventBus, true, hyperlinkPopupTitle, widget);
        this.add(hyperlinkNoneEditablePanel);
        this.setStyleName("tooltip-popup");

    }

    public void displayItems(LinkedHashMap<Id, String> listValues) {
        if (displayAsHyperlinks) {
            hyperlinkNoneEditablePanel.displayHyperlinks(listValues, false);
        } else {
            hyperlinkNoneEditablePanel.displayItems(listValues.values(), false);
        }

    }

}