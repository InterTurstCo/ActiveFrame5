package ru.intertrust.cm.core.gui.impl.client.form.widget.tooltip;

import com.google.gwt.user.client.ui.PopupPanel;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.form.PopupTitlesHolder;
import ru.intertrust.cm.core.config.gui.form.widget.HasLinkedFormMappings;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.HyperlinkNoneEditablePanel;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 30.06.2014
 *         Time: 13:52
 */
public class NoneEditableTooltip extends PopupPanel {
    private EventBus eventBus;
    private HyperlinkNoneEditablePanel hyperlinkNoneEditablePanel;
    private boolean displayAsHyperlinks;
    private HasLinkedFormMappings hasLinkedFormMappings;

    public NoneEditableTooltip(SelectionStyleConfig selectionStyleConfig, EventBus eventBus, boolean displayAsHyperlinks,
                               Map<String, PopupTitlesHolder> typeTitleMap, HasLinkedFormMappings hasLinkedFormMappings) {

        super(true);
        this.eventBus = eventBus;
        this.displayAsHyperlinks = displayAsHyperlinks;
        this.hasLinkedFormMappings = hasLinkedFormMappings;
        init(selectionStyleConfig, typeTitleMap);
    }

    private void init(SelectionStyleConfig selectionStyleConfig, Map<String, PopupTitlesHolder> typeTitleMap) {
        hyperlinkNoneEditablePanel = new HyperlinkNoneEditablePanel(selectionStyleConfig, eventBus, true, typeTitleMap, hasLinkedFormMappings);
        this.add(hyperlinkNoneEditablePanel);
        this.setStyleName("tooltipPopup");

    }

    public void displayItems(LinkedHashMap<Id, String> listValues) {
        if (displayAsHyperlinks) {
            hyperlinkNoneEditablePanel.displayHyperlinks(listValues, false);
        } else {
            hyperlinkNoneEditablePanel.displayItems(listValues.values(), false);
        }

    }

}