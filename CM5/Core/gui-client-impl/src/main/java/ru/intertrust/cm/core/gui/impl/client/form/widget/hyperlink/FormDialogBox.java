package ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.FormPluginView;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEventListener;
import ru.intertrust.cm.core.gui.impl.client.form.WidgetsContainer;
import ru.intertrust.cm.core.gui.impl.client.form.widget.messagedialog.DialogBoxUtils;
import ru.intertrust.cm.core.gui.impl.client.panel.ResizablePanel;
import ru.intertrust.cm.core.gui.impl.client.panel.RightSideResizablePanel;
import ru.intertrust.cm.core.gui.impl.client.util.GuiUtil;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 14.01.14
 *         Time: 10:25
 */
public class FormDialogBox extends DialogBox {
    private static final String DEFAULT_HEIGHT = "250px";
    private static final int MINIMAL_HEIGHT = 200;
    private static final int MINIMAL_WIDTH = 350;
    private PluginPanel formPluginPanel;
    private AbsolutePanel buttonsPanel;
    private String headerTitle;
    private String modalWidth;
    private String modalHeight;

    public FormDialogBox(String headerTitle) {
        this.headerTitle = headerTitle;
        init(false);
    }
    @Deprecated
    public FormDialogBox(String headerTitle, String modalWidth, String modalHeight) {
        this.headerTitle = headerTitle;
        this.modalWidth = modalWidth;
        this.modalHeight = modalHeight;
        init(false);
    }
    public FormDialogBox(String headerTitle, String modalWidth, String modalHeight, boolean resizable) {
        this.headerTitle = headerTitle;
        this.modalWidth = modalWidth;
        this.modalHeight = modalHeight;
        init(resizable);
    }

    public FormDialogBox() {
        init(false);
    }

    private void init(boolean resizable) {
        // Enable animation.
        this.setAnimationEnabled(true);
        this.setModal(true);
        this.addStyleName("dialogBoxBody");
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
        if (modalWidth != null) {
            panel.setWidth(modalWidth);
        }
        panel.setHeight(getHeight());

        ResizablePanel resizablePanel = new RightSideResizablePanel(MINIMAL_WIDTH, MINIMAL_HEIGHT,true, resizable);
        resizablePanel.addStyleName("dialogResizablePanel");
        resizablePanel.wrapWidget(panel);
        this.add(resizablePanel);
    }


    private void showDialogBox() {
        this.center();
    }

    public void initButton(String text, ClickHandler clickHandler) {
        Button button = new Button(text);
        button.addStyleName("darkButton");
        button.removeStyleName("gwt-Button");
        button.addClickHandler(clickHandler);
        buttonsPanel.add(button);

    }
    public FormPlugin createFormPlugin(FormPluginConfig config, EventBus eventBus){
        return createFormPlugin(config, eventBus, null);
    }

    public FormPlugin createFormPlugin(final FormPluginConfig config, final EventBus eventBus,
                                       final WidgetsContainer parentWidgetsContainer) {
        final FormPlugin formPlugin = ComponentRegistry.instance.get("form.plugin");
        formPlugin.setLocalEventBus(eventBus);
        formPlugin.setConfig(config);
        formPlugin.addViewCreatedListener(new PluginViewCreatedEventListener() {
            @Override
            public void onViewCreation(PluginViewCreatedEvent source) {
                String resultTitle = headerTitle == null
                        ? GuiUtil.getConfiguredTitle(formPlugin, config.getDomainObjectId() == null) : headerTitle;
                FormDialogBox.this.getCaption().setText(resultTitle);
                DialogBoxUtils.addCaptionCloseButton(FormDialogBox.this);
                setTitle(resultTitle);
                if(parentWidgetsContainer != null){
                FormPluginView formPluginView = (FormPluginView) formPlugin.getView();
                WidgetsContainer widgetContainer = (WidgetsContainer) formPluginView.getViewWidget();
                widgetContainer.setParentWidgetsContainer(parentWidgetsContainer);
                }
                showDialogBox();

            }
        });

        formPluginPanel.open(formPlugin);
        return formPlugin;

    }
    private String getHeight(){
        return modalHeight == null ? DEFAULT_HEIGHT : modalHeight;
    }

}
