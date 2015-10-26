/*
 * Copyright 2011-2012 InterTrust LTD. All rights reserved. Visit our web-site: www.intertrust.ru.
 */
package ru.intertrust.cm.core.gui.impl.markup.cmj41.navigation.client.module.root.view.sidebar;

import java.util.HashMap;
import java.util.Iterator;

import ru.intertrust.cm.core.gui.impl.markup.cmj41.navigation.client.iview.SystemSizes;
import ru.intertrust.cm.core.gui.impl.markup.cmj41.navigation.client.system.AppConfig;
import ru.intertrust.cm.core.gui.impl.markup.cmj41.navigation.client.system.NameTokens;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import ru.intertrust.cm.core.gui.api.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author laputski
 */
public class SidebarView extends Composite {

    public final static String DIV_CONTENT_ID = "content";
    public final static String DIV_SIDEBAR_ID = "sidebar";
    public final static String DIV_TASKSWRAP_ID = "tasks-wrap";

    /**
   * 
   */
    HashMap<String, String> hasImg = new HashMap<String, String>();

    private static SidebarMenuUiBinder uiBinder = GWT.create(SidebarMenuUiBinder.class);

    @UiTemplate("SidebarView.ui.xml")
    interface SidebarMenuUiBinder extends UiBinder<Widget, SidebarView> {
    }

    @UiField
    LayoutPanel sidebarPanel;

    @UiField
    ScrollPanel scrollPanel;

    @UiField
    HTMLPanel tasksWrap;

    @UiField
    HTMLPanel arrsPanel;

    @UiField
    Element arrTop, arrBottom;

    @UiField
    VerticalPanel menuItems;

    // @Override
    public VerticalPanel getMenuItems() {
        return menuItems;
    }

    HashMap<String, HTML> navigationMap = new HashMap<String, HTML>();

    // @Override
    public void putNavigationMap(String name, HTML h) {
        navigationMap.put(name, h);
    }

    public SidebarView() {
        initWidget(uiBinder.createAndBindUi(this));

        hasImg.put(NameTokens.notificationsPage, "images/inbox.png");
        hasImg.put(NameTokens.tasksPage, "images/tasks.png");
        hasImg.put(NameTokens.calendarPage, "images/calendar.png");
        hasImg.put(NameTokens.docsPage, "images/docs.png");
        hasImg.put(NameTokens.discussionsPage, "images/discussions.png");
        hasImg.put(NameTokens.helpersPage, "images/helpers.png");
        hasImg.put(NameTokens.casesPage, "images/cases.png");
        hasImg.put(NameTokens.reportsPage, "images/analitika.png");

        init();

        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                correctContentStyles();
            }
        });
    }

    // @Override
    public HashMap<String, String> getHasImg() {
        return hasImg;
    }

    public class ChangeVerticalScrollPositionTop extends Animation {

        private int scrollPosition;

        @Override
        protected void onUpdate(double progress) {
            int position = (int) changePositionTop(progress);
            scrollPanel.setVerticalScrollPosition(scrollPosition - position);
        }

        private double changePositionTop(double progress) {
            double outPosition = SB_ELEM_HEIGHT * progress;
            return outPosition;
        }

        /**
         * @param duration
         * @param Position
         */
        public void run(int duration, int position) {
            this.scrollPosition = position;
            super.run(duration);
        }

    }

    public class ChangeVerticalScrollPositionBottom extends Animation {

        private int scrollPosition;

        @Override
        protected void onUpdate(double progress) {
            int position = (int) changePositionBottom(progress);
            scrollPanel.setVerticalScrollPosition(scrollPosition + position);
        }

        private double changePositionBottom(double progress) {
            double outPosition = SB_ELEM_HEIGHT * progress;
            return outPosition;
        }

        /**
         * @param duration
         * @param Position
         */
        public void run(int duration, int position) {
            this.scrollPosition = position;
            super.run(duration);
        }

    }

    final ChangeVerticalScrollPositionTop changePositionTop = new ChangeVerticalScrollPositionTop();
    final ChangeVerticalScrollPositionBottom changePositionBottom = new ChangeVerticalScrollPositionBottom();

    private void init() {

        sidebarPanel.getElement().setId(DIV_SIDEBAR_ID);
        sidebarPanel.getElement().getStyle().clearPosition();

        tasksWrap.getElement().setId(DIV_TASKSWRAP_ID);

        scrollPanel.setAlwaysShowScrollBars(false);
        scrollPanel.getElement().getStyle().setOverflow(Overflow.HIDDEN);

        arrsPanel.setStyleName("arrs");
        initArrowsImages();

        Event.sinkEvents(arrTop, Event.ONCLICK);
        Event.sinkEvents(arrBottom, Event.ONCLICK);

        DOM.setEventListener((com.google.gwt.user.client.Element) arrTop, new EventListener() {

            @Override
            public void onBrowserEvent(Event event) {

                switch (DOM.eventGetType(event)) {
                    case Event.ONCLICK:
                        changePositionTop.run(AppConfig.SIDEBAR_ANIMATION, scrollPanel.getVerticalScrollPosition());

                        if (scrollPanel.getVerticalScrollPosition() == scrollPanel.getMinimumVerticalScrollPosition()) {
                            disableUpArrow();
                        }
                        else {
                            enableUpArrow();
                        }
                        enableDownArrow();

                        break;
                }
            }
        });

        DOM.setEventListener((com.google.gwt.user.client.Element) arrBottom, new EventListener() {

            @Override
            public void onBrowserEvent(Event event) {

                switch (DOM.eventGetType(event)) {
                    case Event.ONCLICK:
                        changePositionBottom.run(AppConfig.SIDEBAR_ANIMATION, scrollPanel.getVerticalScrollPosition());

                        if (scrollPanel.getVerticalScrollPosition() == scrollPanel.getMaximumVerticalScrollPosition()) {
                            disableDownArrow();
                        }
                        else {
                            enableDownArrow();
                        }
                        enableUpArrow();

                        break;
                }
            }
        });
    }

    private void initArrowsImages() {
        enableUpArrow();
        enableDownArrow();

        if (scrollPanel.getVerticalScrollPosition() == scrollPanel.getMinimumVerticalScrollPosition()) {
            disableUpArrow();
        }
        else if (scrollPanel.getVerticalScrollPosition() == scrollPanel.getMaximumVerticalScrollPosition()) {
            disableDownArrow();
        }
    }

    private void setArrowImage(Element arrow, String path) {
        arrow.getStyle().setBackgroundImage("url(" + path + ")");
    }

    private void enableUpArrow() {
        setArrowImage(arrTop, "css/icons/icon-sidebar-button-up-active.png");
    }

    private void disableUpArrow() {
        setArrowImage(arrTop, "css/icons/icon-sidebar-button-up-unactive.png");
    }

    private void enableDownArrow() {
        setArrowImage(arrBottom, "css/icons/icon-sidebar-button-down-active.png");
    }

    private void disableDownArrow() {
        setArrowImage(arrBottom, "css/icons/icon-sidebar-button-down-unactive.png");
    }


    public HTML getSidebarItem() {
        HTML h = new HTML();
        return h;
    }


    public void sidebarItem(String path, String title, String name, Long value, HTML h) {
        if (value > 0) {
            h.setHTML("<li><a><img width=\"60\" height=\"50\" border=\"0\" alt=\"\" src=\"" + path + "\"><span>"
                    + title + "</span><small>" + value + "</small></a></li>");
        }
        else {
            h.setHTML("<li><a><img width=\"60\" height=\"50\" border=\"0\" alt=\"\" src=\"" + path + "\"><span>"
                    + title + "</span></a></li>");
        }
        if (name != null) {
            h.setTitle(name);
        }

        h.getElement().getStyle().setCursor(Cursor.POINTER);
    }

    /**
     * Делает выделенным элемент с названием name
     */
    // @Override
    public void correctStyle(String name) {
        for (Iterator<?> iterator = navigationMap.values().iterator(); iterator.hasNext();) {
            HTML type = (HTML) iterator.next();
            type.removeStyleName("selected");
        }
        HTML selected = navigationMap.get(name);
        if (selected != null) {
            selected.setStyleName("selected");
        }
    }

    // @Override
    public HashMap<String, HTML> getNavigationMap() {
        return navigationMap;
    }

    /**
     * Get actual history token (without parameters)
     * @return
     */
    @SuppressWarnings("unused")
    private String loadActualHistoryToken() {

        String token = History.getToken();

        if (token.indexOf(";") != -1) {
            token = token.substring(0, token.indexOf(";"));
        }

        return token;
    }

    public final static int SB_ELEM_HEIGHT = 84;
    public final static int BUT_RESERV_HEIGHT = 30;


    private final int currentItemsCount = 8;

    /**
     * Sidebar content correct function. See main.js for details
     * @author labotski, alex oreshkevich
     */
    private strictfp void sidebarContentCorrect() {

        int clientHeight = Window.getClientHeight();


        if (clientHeight < SystemSizes.NORTH_HEIGHT) {
            return;
        }

        // client height without north (header, topMenu ...)
        double totalHeight = clientHeight - SystemSizes.NORTH_HEIGHT;

        int visibleItemCount = (int) Math.floor((totalHeight - BUT_RESERV_HEIGHT) / SB_ELEM_HEIGHT);
        int visibleHeight = SB_ELEM_HEIGHT * visibleItemCount;

        // manage scrollPanel height
        if (visibleHeight > 0 && visibleItemCount < currentItemsCount) {
            scrollPanel.setHeight(Integer.toString(visibleHeight) + "px");
            showArrs();
        }
        else {
            scrollPanel.setHeight("auto");
            hideArrs();
        }
    }

    private void showArrs() {
        initArrowsImages();
        arrsPanel.getElement().getStyle().setDisplay(Display.BLOCK);
    }

    private void hideArrs() {
        arrsPanel.getElement().getStyle().setDisplay(Display.NONE);
    }

    // @Override
    public void correctContentStyles() {
        sidebarContentCorrect();
    }

    // @Override
    public void addToSlot(Object slot, Widget content) {
    }

    // @Override
    public void removeFromSlot(Object slot, Widget content) {
    }

    // @Override
    public void setInSlot(Object slot, Widget content) {
    }

}
