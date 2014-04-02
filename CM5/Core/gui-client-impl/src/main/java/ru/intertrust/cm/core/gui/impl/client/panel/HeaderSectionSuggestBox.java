package ru.intertrust.cm.core.gui.impl.client.panel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.event.HeaderNotificationRemoveItemEvent;
import ru.intertrust.cm.core.gui.impl.client.event.HeaderNotificationRemoveItemEventHandler;
import ru.intertrust.cm.core.gui.impl.client.plugins.headernotification.HeaderNotificationPlugin;

/**
 * Created with IntelliJ IDEA.
 * User: Timofiy Bilyi
 * Date: 28.11.13
 * Time: 15:20
 * To change this template use File | Settings | File Templates.
 */
public class HeaderSectionSuggestBox implements IsWidget{
    private AbsolutePanel rootSuggestDiv;
    private FocusPanel firstImage;
    private FocusPanel popupClose;
    private FocusPanel secondImage;
    private AbsolutePanel decoratedListSuggestBox;
    private AbsolutePanel sectionSuggestBox;
    private HeaderNotificationPlugin headerNotificationPlugin;
    private PluginPanel headerNotificationPanel;
    private boolean pluginPopupShow;
    private PopupPanel pluginPopupPanel;
    private EventBus eventBus = Application.getInstance().getEventBus();
    private int notificationRowCount;

    public HeaderSectionSuggestBox() {
        rootSuggestDiv = new AbsolutePanel();
        pluginPopupPanel = new PopupPanel(true);
        pluginPopupPanel.addStyleName("header-notification-popup");
        // pluginPopupPanel.removeStyleName("gwt-PopupPanel");


        rootSuggestDiv.setStyleName("root-suggest-div");

        decoratedListSuggestBox = new AbsolutePanel();
//        decoratedListSuggestBox.getElement().getStyle().clearPosition();
//        decoratedListSuggestBox.getElement().getStyle().clearOverflow();
        decoratedListSuggestBox.setStyleName("decorated-list-suggest-box");


        firstImage = new FocusPanel();
        popupClose = new FocusPanel();
        popupClose.setStyleName("close-notification-popup ");
        firstImage.setStyleName("header-first-action-button");
        firstImage.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (firstImage.getStyleName().equals("header-first-action-button")) {
                    firstImage.addStyleName("header-first-action-button-mute");
                } else {
                    firstImage.removeStyleName("header-first-action-button-mute");
                }

            }
        });

        secondImage = new FocusPanel();
        secondImage.setStyleName("header-second-action-button");

        sectionSuggestBox = new AbsolutePanel();
        headerNotificationPlugin = ComponentRegistry.instance.get("header.notifications.plugin");
        headerNotificationPanel = new PluginPanel();
        headerNotificationPanel.open(headerNotificationPlugin);



        sectionSuggestBox.add(headerNotificationPanel);
        secondImage.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (notificationRowCount > 1){
                pluginPopupShow = true;
                pluginPopupPanel.clear();
                FlowPanel flowPanel = new FlowPanel();
                pluginPopupPanel.add(flowPanel);
                flowPanel.add(headerNotificationPanel);
                flowPanel.add(popupClose);
                pluginPopupPanel.show();
                }



            }
        });


        pluginPopupPanel.addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> event) {
                sectionSuggestBox.clear();
                sectionSuggestBox.add(headerNotificationPanel);
                pluginPopupShow = false;
            }
        });

        popupClose.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                 pluginPopupPanel.hide();
            }
        });

        eventBus.addHandler(HeaderNotificationRemoveItemEvent.TYPE, new HeaderNotificationRemoveItemEventHandler() {
            @Override
            public void headerNotificationPopupStatus(HeaderNotificationRemoveItemEvent event) {
                notificationRowCount = event.getPluginRowCount();
                pluginPopupPanel.hide();


            }
        });

//        sectionSuggestBox.getElement().getStyle().clearOverflow();
//        sectionSuggestBox.getElement().getStyle().clearPosition();
                sectionSuggestBox.setStyleName("section-suggest-box");
        sectionSuggestBox.getElement().getStyle().clearPosition();
         

//        AbsolutePanel decoratedSuggestBox = new AbsolutePanel();
//        decoratedSuggestBox.setStyleName("decorated-suggest-box");

//        TextBox suggestBox = new TextBox();
//        suggestBox.setWidth("100%");
//        suggestBox.setStyleName("upper-suggestbox");

//        sectionSuggestBox.add(decoratedSuggestBox);
        //sectionSuggestBox.add(secondImage);
//        decoratedSuggestBox.add(suggestBox);

        decoratedListSuggestBox.add(firstImage);
        decoratedListSuggestBox.add(sectionSuggestBox);
        decoratedListSuggestBox.add(secondImage);
        rootSuggestDiv.add(decoratedListSuggestBox);
    //    rootSuggestDiv.add(sectionSuggestBox);


    }

    @Override
    public Widget asWidget() {
        return rootSuggestDiv;
    }


}
