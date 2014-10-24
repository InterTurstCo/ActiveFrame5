package ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.HasLinkedFormMappings;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.action.SaveAction;
import ru.intertrust.cm.core.gui.impl.client.event.ActionSuccessListener;
import ru.intertrust.cm.core.gui.impl.client.event.HyperlinkStateChangedEvent;
import ru.intertrust.cm.core.gui.impl.client.form.widget.tooltip.TooltipWidget;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.01.14
 *         Time: 13:15
 */
public class HyperlinkClickHandler extends LinkedFormOpeningHandler {
    private HyperlinkDisplay hyperlinkDisplay;
    private HasLinkedFormMappings widget;

    public HyperlinkClickHandler(Id id, HyperlinkDisplay hyperlinkDisplay, EventBus eventBus, boolean tooltipContent,
                                 String popupTitle, HasLinkedFormMappings widget) {
        super(id, eventBus, tooltipContent, popupTitle);
        this.hyperlinkDisplay = hyperlinkDisplay;
        this.widget = widget;
    }

    @Override
    public void onClick(ClickEvent event) {
        processClick();

    }

    public void processClick() {
        createNonEditableFormDialogBox(widget);
    }

    protected void noEditableOnCancelClick(FormPlugin formPlugin, FormDialogBox dialogBox) {
        dialogBox.hide();
    }

    protected void noEditableOnChangeButtonClick(FormPlugin formPlugin, FormDialogBox dialogBox) {
        dialogBox.hide();
        createEditableFormDialogBox(widget);
    }

    protected void editableOnCancelClick(FormPlugin formPlugin, FormDialogBox dialogBox) {
        dialogBox.hide();
    }

    protected void editableOnChangeClick(FormPlugin formPlugin, final FormDialogBox dialogBox) {
        final SaveAction action = getSaveAction(formPlugin, id);
        action.addActionSuccessListener(new ActionSuccessListener() {
            @Override
            public void onSuccess() {
                dialogBox.hide();
                eventBus.fireEvent(new HyperlinkStateChangedEvent(id, hyperlinkDisplay, tooltipContent));
            }
        });
        action.perform();
    }

}