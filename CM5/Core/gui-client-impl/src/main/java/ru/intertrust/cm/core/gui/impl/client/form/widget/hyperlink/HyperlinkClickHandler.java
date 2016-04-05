package ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.form.PopupTitlesHolder;
import ru.intertrust.cm.core.config.gui.form.widget.HasLinkedFormMappings;
import ru.intertrust.cm.core.config.gui.form.widget.LinkedFormConfig;
import ru.intertrust.cm.core.gui.api.client.ActionManager;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.ConfirmCallback;
import ru.intertrust.cm.core.gui.api.client.event.OpenHyperlinkInSurferEvent;
import ru.intertrust.cm.core.gui.api.client.event.PluginCloseListener;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.action.ActionManagerImpl;
import ru.intertrust.cm.core.gui.impl.client.action.SaveAction;
import ru.intertrust.cm.core.gui.impl.client.event.ActionSuccessListener;
import ru.intertrust.cm.core.gui.impl.client.event.HyperlinkStateChangedEvent;
import ru.intertrust.cm.core.gui.impl.client.util.GuiUtil;

import java.util.List;
import java.util.Map;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.01.14
 *         Time: 13:15
 */
public class HyperlinkClickHandler extends LinkedFormOpeningHandler {
    private HyperlinkDisplay hyperlinkDisplay;
    private HasLinkedFormMappings widget;
    private Boolean readOnly = false;

    public HyperlinkClickHandler(Id id, HyperlinkDisplay hyperlinkDisplay, EventBus eventBus, boolean tooltipContent,
                                 Map<String, PopupTitlesHolder> typeTitleMap, HasLinkedFormMappings widget) {
        super(id, eventBus, tooltipContent, typeTitleMap);
        this.hyperlinkDisplay = hyperlinkDisplay;
        this.widget = widget;
    }

    public HyperlinkClickHandler(Id id, HyperlinkDisplay hyperlinkDisplay, EventBus eventBus, boolean tooltipContent,
                                 Map<String, PopupTitlesHolder> typeTitleMap, HasLinkedFormMappings widget, Boolean readOnly) {
        this(id, hyperlinkDisplay, eventBus, tooltipContent, typeTitleMap, widget);
        this.readOnly = readOnly;
    }

    @Override
    public void onClick(ClickEvent event) {
        processClick();
    }

    public HyperlinkClickHandler withModalWindow(boolean modalWindow) {
        this.modalWindow = modalWindow;
        return this;
    }

    public void processClick() {
        if (modalWindow) {
            init(widget);
        } else {
            openInFullWindow(true);

        }
    }

    protected void noEditableOnCancelClick(FormPlugin formPlugin, FormDialogBox dialogBox) {
        dialogBox.hide();
    }

    protected void noEditableOnChangeButtonClick(FormPlugin formPlugin, FormDialogBox dialogBox) {
        if (!readOnly) {
            createEditableFormDialogBox(dialogBox, widget);
        }

    }

    protected void editableOnCancelClick(FormPlugin formPlugin, final FormDialogBox dialogBox) {
        ActionManager actionManager = new ActionManagerImpl(formPlugin.getOwner());
        actionManager.checkChangesBeforeExecution(new ConfirmCallback() {
            @Override
            public void onAffirmative() {
                dialogBox.hide();
            }

            @Override
            public void onCancel() {
                //nothing to do
            }
        });


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

    private void openInFullWindow(boolean editable) {
        openInFullWindow(null, editable);
    }

    protected void openInFullWindow(FormDialogBox dialogBox, boolean editable) {
        PluginCloseListener pluginCloseListener = new PluginCloseListener() {
            @Override
            public void onPluginClose() {
                eventBus.fireEvent(new HyperlinkStateChangedEvent(id, hyperlinkDisplay, tooltipContent));
            }
        };
        List<LinkedFormConfig> linkedFormConfigs = GuiUtil.getLinkedFormConfigs(widget.getLinkedFormConfig(), widget.getLinkedFormMappingConfig());
        Application.getInstance().getEventBus().fireEvent(new OpenHyperlinkInSurferEvent(id, linkedFormConfigs,
                pluginCloseListener, editable));
        if (dialogBox != null) {
            dialogBox.hide();
        }
    }

}