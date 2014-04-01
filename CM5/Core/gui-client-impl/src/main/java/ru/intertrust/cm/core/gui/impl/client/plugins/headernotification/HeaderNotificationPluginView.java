package ru.intertrust.cm.core.gui.impl.client.plugins.headernotification;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.event.CentralPluginChildOpeningRequestedEvent;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginState;
import ru.intertrust.cm.core.gui.model.plugin.HeaderNotificationItem;
import ru.intertrust.cm.core.gui.model.plugin.HeaderNotificationPluginData;

import java.util.ArrayList;

/**
 * Created by lvov on 25.03.14.
 */
public class HeaderNotificationPluginView extends PluginView{
    private FlowPanel container;
    private Plugin plugin;
    private HeaderNotificationPluginData headerNotificationPluginData;
    private ArrayList<HeaderNotificationItem> listNotificationItem;
    private FormPlugin formPlugin;


    protected HeaderNotificationPluginView(Plugin plugin) {
        super(plugin);
        this.plugin = plugin;
        headerNotificationPluginData = plugin.getInitialData();
        listNotificationItem = headerNotificationPluginData.getCollection();


    }

    @Override
    public IsWidget getViewWidget() {
        container = new FlowPanel();
        containerSetStyle();

        for (int i =0; i < listNotificationItem.size(); i++){
            AbsolutePanel absolutePanel = new AbsolutePanel();
            final FocusPanel crossContainer = new FocusPanel();
            crossContainer.addStyleName("cross-button");


            absolutePanel.addStyleName("section-suggest-item");


            absolutePanel.setSize("100%", "35px");
            Label label = new Label(listNotificationItem.get(i).getSubject() +" "+listNotificationItem.get(i).getBody());
            final int finalI = i;

            label.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    FormPluginConfig config = new FormPluginConfig(listNotificationItem.get(finalI).getId()) ;
                    FormPluginState formPluginState = new FormPluginState();
                    formPluginState.setToggleEdit(true);
                    formPluginState.setEditable(false);
                    formPluginState.setInCentralPanel(true);
                    config.setPluginState(formPluginState);
                    if (formPlugin != null){
                        formPlugin.getOwner().closeCurrentPlugin();
                    }
                    formPlugin = createFormPlugin(config);

                    Application.getInstance().getEventBus().fireEvent(new CentralPluginChildOpeningRequestedEvent(formPlugin));


                }
            });

            absolutePanel.add(label);
            absolutePanel.add(crossContainer);
            container.add(absolutePanel);

            crossContainer.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    crossContainer.getParent().removeFromParent();
                }
            });
        }

        return container;
    }

    private void containerSetStyle(){
        container.addStyleName("header-notification-style");
    }

    private FormPlugin createFormPlugin(final FormPluginConfig config) {
        final FormPlugin formPlugin = ComponentRegistry.instance.get("form.plugin");
        formPlugin.setConfig(config);
        formPlugin.setDisplayActionToolBar(true);
        return formPlugin;
    }


}
