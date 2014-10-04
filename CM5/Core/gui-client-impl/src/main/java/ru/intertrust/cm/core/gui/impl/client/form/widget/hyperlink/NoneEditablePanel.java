package ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.impl.client.form.widget.AbstractNoneEditablePanel;
import ru.intertrust.cm.core.gui.impl.client.form.widget.tooltip.TooltipButtonClickHandler;

import java.util.Collection;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 29.01.14
 *         Time: 13:15
 */
public class NoneEditablePanel extends AbstractNoneEditablePanel {
    protected EventBus eventBus;
    public NoneEditablePanel(SelectionStyleConfig selectionStyleConfig, EventBus eventBus) {
        super(selectionStyleConfig);
        this.eventBus = eventBus;
    }

    protected void displayItem(final String itemRepresentation) {
        AbsolutePanel element = new AbsolutePanel();
        element.addStyleName("facebook-element");
        element.getElement().getStyle().setDisplay(displayStyle);
        Label label = new Label(itemRepresentation);
        label.setStyleName("facebook-label");
        label.addStyleName("facebook-none-clickable-label");
        element.add(label);
        if(displayStyle.equals(Style.Display.INLINE_BLOCK)) {
            element.getElement().getStyle().setFloat(Style.Float.LEFT);
            label.getElement().getStyle().setFloat(Style.Float.LEFT);

        }
        mainBoxPanel.add(element);
    }

    public void displayItems(Collection<String> items, boolean drawTooltipButton) {
        for (String item : items) {
            displayItem(item);
        }
        if (drawTooltipButton){
            addTooltipButton();
        }
    }

    protected void addTooltipButton() {
        Button openTooltip = new Button("...");
        openTooltip.setStyleName("tooltipButton");
        mainBoxPanel.add(openTooltip);
        openTooltip.addClickHandler(new TooltipButtonClickHandler(eventBus));

    }

}
