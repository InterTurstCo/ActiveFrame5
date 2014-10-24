package ru.intertrust.cm.core.gui.impl.client.plugins.headernotification;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.event.CentralPluginChildOpeningRequestedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.HeaderNotificationRemoveItemEvent;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.plugin.*;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.ArrayList;

/**
 * Created by lvov on 25.03.14.
 */
public class HeaderNotificationPluginView extends PluginView{
    private FlowPanel container;
    private Plugin plugin;
    private HeaderNotificationPluginData headerNotificationPluginData;
    private ArrayList<HeaderNotificationItem> listNotificationItem;
    private FormPlugin oldFormPlugin;

    protected HeaderNotificationPluginView(Plugin plugin) {
        super(plugin);
        this.plugin = plugin;
        headerNotificationPluginData = plugin.getInitialData();
        listNotificationItem = headerNotificationPluginData.getCollection();
        if (Application.getInstance().getHeaderNotificationPeriod() > 0) {
            updateNotificationTimer(Application.getInstance().getHeaderNotificationPeriod());
        }
    }

    private void updateNotificationTimer(final int headerNotificationPeriod){
        Timer timer = new Timer() {
            @Override
            public void run() {
                cancelHeaderNotificationItem(new CancelHeaderNotificationItem());
            }
        };
        timer.scheduleRepeating(headerNotificationPeriod);
    }

    @Override
    public IsWidget getViewWidget() {
        container = new FlowPanel();
        containerSetStyle();
        buildPlugin();

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

    private void buildPlugin(){
        if (listNotificationItem.size() == 0){
            AbsolutePanel absolutePanel = new AbsolutePanel();
            absolutePanel.add( new Label("Новых уведомлений нет"));
            container.add(absolutePanel);


        }
        for (int i =0; i < listNotificationItem.size(); i++){
            AbsolutePanel absolutePanel = new AbsolutePanel();
            final FocusPanel crossContainer = new FocusPanel();
            if (i == 0){
                crossContainer.addStyleName("cross-button-first");
            }   else {
            crossContainer.addStyleName("cross-button");
            }

            absolutePanel.addStyleName("section-suggest-item");


            absolutePanel.setSize("100%", "35px");
            Label label = new Label(listNotificationItem.get(i).getSubject() +" "+listNotificationItem.get(i).getBody());
            final int finalI = i;

            label.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    FormPluginConfig config = new FormPluginConfig(listNotificationItem.get(finalI).getId()) ;
                    FormPlugin formPlugin = new FormPlugin();
                    FormPluginState formPluginState = new FormPluginState();
                    formPluginState.setToggleEdit(true);
                    formPluginState.setEditable(false);
                    formPluginState.setInCentralPanel(true);
                    config.setPluginState(formPluginState);
                    //commented out: don't close the old plugin, to be able to return back to it when notification plugin is closed
//                    if (oldFormPlugin != null){
//                        oldFormPlugin.getOwner().closeCurrentPlugin();
//                    }
                    formPlugin = createFormPlugin(config);
                    oldFormPlugin = formPlugin;


                    Application.getInstance().getEventBus().fireEvent(new CentralPluginChildOpeningRequestedEvent(formPlugin));
                    cancelHeaderNotificationItem(new CancelHeaderNotificationItem(listNotificationItem.get(finalI)
                            .getId()));

                }
            });

            absolutePanel.add(label);
            absolutePanel.add(crossContainer);
            container.add(absolutePanel);

            crossContainer.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    crossContainer.getParent().removeFromParent();
                    cancelHeaderNotificationItem(new CancelHeaderNotificationItem(listNotificationItem.get(finalI)
                            .getId()));
                }
            });
        }
        Application.getInstance().getEventBus().fireEvent(new HeaderNotificationRemoveItemEvent(listNotificationItem.size()));
    }

    private void cancelHeaderNotificationItem(CancelHeaderNotificationItem cancelHeaderNotificationItem){
        Command command = new Command("deleteNotification", "header.notifications.plugin", cancelHeaderNotificationItem);
        BusinessUniverseServiceAsync.Impl.getInstance().executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining canselHeaderNotificationItemForPluginInitialization for ''");
                caught.printStackTrace();

            }

            @Override
            public void onSuccess(Dto result) {
                CancelHeaderNotificationItem cancelHeaderNotificationItem1 = (CancelHeaderNotificationItem) result;

                listNotificationItem.clear();
                listNotificationItem = ((CancelHeaderNotificationItem) result).getItems();
                container.clear();
                buildPlugin();

            }
        });
    }
}
