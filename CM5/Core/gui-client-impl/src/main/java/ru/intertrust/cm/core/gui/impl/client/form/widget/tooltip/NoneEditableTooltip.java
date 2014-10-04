package ru.intertrust.cm.core.gui.impl.client.form.widget.tooltip;

import com.google.gwt.user.client.ui.PopupPanel;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
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

    public NoneEditableTooltip(SelectionStyleConfig selectionStyleConfig, EventBus eventBus,
                               boolean displayAsHyperlinks) {

        super(true);
        this.eventBus = eventBus;
        this.displayAsHyperlinks = displayAsHyperlinks;
        init(selectionStyleConfig);
    }

    private void init(SelectionStyleConfig selectionStyleConfig) {
        hyperlinkNoneEditablePanel = new HyperlinkNoneEditablePanel(selectionStyleConfig, eventBus, true);

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