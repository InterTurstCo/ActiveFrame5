package ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Label;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.form.PopupTitlesHolder;
import ru.intertrust.cm.core.config.gui.form.widget.HasLinkedFormMappings;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.01.14
 *         Time: 13:15
 */
public class HyperlinkNoneEditablePanel extends NoneEditablePanel implements HyperlinkDisplay {
    private boolean tooltipContent;
    private Map<String, PopupTitlesHolder> typeTitleMap;
    private HasLinkedFormMappings widget;

    public HyperlinkNoneEditablePanel(SelectionStyleConfig selectionStyleConfig, EventBus eventBus, boolean tooltipContent,
                                      Map<String, PopupTitlesHolder> typeTitleMap, HasLinkedFormMappings widget) {
        super(selectionStyleConfig, eventBus);
        this.tooltipContent = tooltipContent;
        this.typeTitleMap = typeTitleMap;
        this.widget = widget;
    }

    private void displayHyperlink(Id id, String itemRepresentation) {
        AbsolutePanel element = new AbsolutePanel();
        element.addStyleName("facebook-element");
        Label label = new Label(itemRepresentation);
        label.setStyleName("facebook-label");
        label.addStyleName("facebook-clickable-label");
        label.addClickHandler(new HyperlinkClickHandler(id, this, eventBus, tooltipContent, typeTitleMap, widget));
        element.getElement().getStyle().setDisplay(displayStyle);
        element.add(label);
        if (displayStyle.equals(Style.Display.INLINE_BLOCK)) {
            element.getElement().getStyle().setFloat(Style.Float.LEFT);
            label.getElement().getStyle().setFloat(Style.Float.LEFT);

        }
        mainBoxPanel.add(element);
    }

    public void displayHyperlinks(LinkedHashMap<Id, String> listValues, boolean drawTooltipButton) {
        mainBoxPanel.clear();
        Set<Map.Entry<Id, String>> entries = listValues.entrySet();
        for (Map.Entry<Id, String> entry : entries) {
            Id id = entry.getKey();
            String representation = entry.getValue();
            displayHyperlink(id, representation);
        }
        if (drawTooltipButton) {
            addTooltipButton();
        }
    }
    public void display(LinkedHashMap<Id, String> listValues, boolean drawTooltipButton, boolean displayAsHyperlinks){
        if (displayAsHyperlinks) {
           displayHyperlinks(listValues, drawTooltipButton);
        } else {
            displayItems(listValues.values(), drawTooltipButton);
        }
    }


}