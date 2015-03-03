package ru.intertrust.cm.core.gui.impl.client.form.widget.tablebrowser;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.web.bindery.event.shared.HandlerRegistration;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.form.widget.buttons.CaptionCloseButton;

import static ru.intertrust.cm.core.config.localization.LocalizationKeys.CANCELLATION_BUTTON_KEY;
import static ru.intertrust.cm.core.config.localization.LocalizationKeys.OK_BUTTON_KEY;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.CANCELLATION_BUTTON;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.OK_BUTTON;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 23.11.2014
 *         Time: 11:06
 */
public class CollectionDialogBox extends DialogBox {
    private int dialogWidth;
    private int dialogHeight;
    private PluginPanel pluginPanel;
    private Button okButton;
    private Button cancelButton;

    public CollectionDialogBox withDialogWidth(int dialogWidth) {
        this.dialogWidth = dialogWidth;
        return this;
    }

    public CollectionDialogBox setDialogHeight(int dialogHeight) {
        this.dialogHeight = dialogHeight;
        return this;
    }

    public CollectionDialogBox setPluginPanel(PluginPanel pluginPanel) {
        this.pluginPanel = pluginPanel;
        return this;
    }

    public PluginPanel getPluginPanel() {
        return pluginPanel;
    }

    public HandlerRegistration addOkButtonHandler(ClickHandler handler) {
        return okButton.addClickHandler(handler);
    }

    public HandlerRegistration addCancelButtonHandler(ClickHandler handler) {
        return cancelButton.addClickHandler(handler);
    }

    public void init(){
        this.setStyleName("table-browser-dialog popup-z-index");
        this.getElement().getStyle().setZIndex(100);
        okButton = new Button(LocalizeUtil.get(OK_BUTTON_KEY, OK_BUTTON));
        okButton.setStyleName("darkButton buttons-fixed");
        cancelButton = new Button(LocalizeUtil.get(CANCELLATION_BUTTON_KEY, CANCELLATION_BUTTON));
        cancelButton.setStyleName("lightButton buttons-fixed position-margin-left");
        AbsolutePanel buttonsContainer = new AbsolutePanel();
        buttonsContainer.addStyleName("table-browser-dialog-box-button-panel");
        buttonsContainer.add(okButton);
        buttonsContainer.add(cancelButton);
        FlowPanel dialogBoxContent = new FlowPanel();

        dialogBoxContent.setWidth(dialogWidth + "px");
        dialogBoxContent.addStyleName("table-browser-dialog-box-content");

        dialogBoxContent.add(buttonsContainer);
        this.add(dialogBoxContent);
        this.setWidth(dialogWidth + "px");
        this.setHeight(dialogHeight + "px");
        dialogBoxContent.add(pluginPanel);
        initCaption();

    }

    private void initCaption(){
        HTML caption = (HTML) this.getCaption();
        CaptionCloseButton captionCloseButton = new CaptionCloseButton();
        captionCloseButton.addClickListener(new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                   cancelButton.click();
            }
        });

        caption.getElement().appendChild(captionCloseButton.getElement());

    }
}
