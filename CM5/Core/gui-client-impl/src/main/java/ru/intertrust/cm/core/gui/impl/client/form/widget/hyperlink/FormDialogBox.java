package ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEventListener;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 14.01.14
 *         Time: 10:25
 */
public class FormDialogBox extends DialogBox {
   private PluginPanel formPluginPanel;
   private  AbsolutePanel buttonsPanel;

    public FormDialogBox(String headerTitle){
        init(headerTitle);
    }

    private void init(String headerTitle){
        // Enable animation.
        this.setAnimationEnabled(true);
        this.setModal(true);
        this.addStyleName("dialog-box-body");

        this.removeStyleName("gwt-DialogBox");
        Label label = new Label(headerTitle);
        label.addStyleName("form-header-message");
        label.removeStyleName("gwt-Label");
        AbsolutePanel panel = new AbsolutePanel();
        panel.addStyleName("form-dialog-box-content");
        SimplePanel header = new SimplePanel();
        header.addStyleName("dialog-box-header");
        header.add(label);
        panel.add(header);
        formPluginPanel = new PluginPanel();
        panel.add(formPluginPanel);
        buttonsPanel = new AbsolutePanel();
        buttonsPanel.addStyleName("buttons-panel");
        panel.add(buttonsPanel);
        this.add(panel);
    }

    private void showDialogBox() {
        this.center();
    }

    public void initButton(String text, ClickHandler clickHandler){
        Button button = new Button(text);
        button.addStyleName("dialog-box-button");
        button.removeStyleName("gwt-Button");
        button.addClickHandler(clickHandler);
        buttonsPanel.add(button);
    }

    public FormPlugin createFormPlugin(FormPluginConfig config){
        final FormPlugin formPlugin = ComponentRegistry.instance.get("form.plugin");
        formPlugin.setConfig(config);
        formPlugin.addViewCreatedListener(new PluginViewCreatedEventListener() {
            @Override
            public void onViewCreation(PluginViewCreatedEvent source) {
                showDialogBox();

            }
        });
        formPluginPanel.open(formPlugin);
        return formPlugin;

    }

}
