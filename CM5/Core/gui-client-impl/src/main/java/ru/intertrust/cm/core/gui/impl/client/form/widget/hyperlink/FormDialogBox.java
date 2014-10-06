package ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.config.gui.form.widget.LinkedFormConfig;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEventListener;
import ru.intertrust.cm.core.gui.impl.client.util.GuiUtil;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 14.01.14
 *         Time: 10:25
 */
public class FormDialogBox extends DialogBox {
    private PluginPanel formPluginPanel;
    private AbsolutePanel buttonsPanel;
    private String headerTitle;
    private LinkedFormConfig linkedFormConfig;

    public FormDialogBox(String headerTitle) {
        this.headerTitle = headerTitle;
        init();
    }

    public FormDialogBox() {
        init();
    }

    private void init() {
        // Enable animation.
        this.setAnimationEnabled(true);
        this.setModal(true);
        this.addStyleName("dialog-box-body");
        this.removeStyleName("gwt-DialogBox");
        VerticalPanel panel = new VerticalPanel();
        panel.addStyleName("form-dialog-box-content");
        formPluginPanel = new PluginPanel();
        formPluginPanel.asWidget().addStyleName("frm-pnl-top");
        panel.add(formPluginPanel);
        buttonsPanel = new AbsolutePanel();
        buttonsPanel.addStyleName("buttons-panel");
        buttonsPanel.getElement().getStyle().clearPosition();
        panel.add(buttonsPanel);

        this.add(panel);
    }


    private void showDialogBox() {
        this.center();
    }

    public void initButton(String text, ClickHandler clickHandler) {
        Button button = new Button(text);
        button.addStyleName("dark-button");
        button.removeStyleName("gwt-Button");
        button.addClickHandler(clickHandler);
        buttonsPanel.add(button);

    }

    public FormPlugin createFormPlugin(final FormPluginConfig config, final EventBus eventBus) {
        final FormPlugin formPlugin = ComponentRegistry.instance.get("form.plugin");
        formPlugin.setLocalEventBus(eventBus);
        formPlugin.setConfig(config);
        formPlugin.addViewCreatedListener(new PluginViewCreatedEventListener() {
            @Override
            public void onViewCreation(PluginViewCreatedEvent source) {
                String resultTitle = headerTitle == null
                        ? GuiUtil.getConfiguredTitle(formPlugin, config.getDomainObjectId() == null):headerTitle;
                FormDialogBox.this.getCaption().setText(resultTitle);
                setTitle(resultTitle);
                showDialogBox();

            }
        });

        formPluginPanel.open(formPlugin);
        return formPlugin;

    }

}
