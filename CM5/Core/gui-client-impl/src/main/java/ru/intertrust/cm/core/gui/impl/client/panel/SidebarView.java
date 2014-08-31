
package ru.intertrust.cm.core.gui.impl.client.panel;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 30.06.2014
 *         Time: 13:38
 */
public class SidebarView extends Composite {

    public final static String DIV_SIDEBAR_ID = "sidebar";
    public final static String DIV_TASKSWRAP_ID = "tasks-wrap";

    private LayoutPanel sidebarPanel = new LayoutPanel();
    private ScrollPanel scrollPanel = new ScrollPanel();
    private HTMLPanel tasksWrap = new HTMLPanel("");
    private HTMLPanel arrsPanel = new HTMLPanel("");
    private Element arrTop;
    private Element arrBottom;
    private VerticalPanel menuItems = new VerticalPanel();
    private ChangeVerticalScrollPositionTop changePositionTop = new ChangeVerticalScrollPositionTop();
    private ChangeVerticalScrollPositionBottom changePositionBottom = new ChangeVerticalScrollPositionBottom();

    public final static int SB_ELEM_HEIGHT = 84;
    public final static int BUT_RESERV_HEIGHT = 30;
    public static int ANIMATION_DURATION = 500;
    public SidebarView() {
        createSideBar();
        initWidget(sidebarPanel);
        init();
        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                correctContentStyles();
            }
        });
    }

    public Widget getWidgetMenuItems(int index) {
        return menuItems.getWidget(index);
    }

    public VerticalPanel getMenuItems() {
        return menuItems;
    }

    public void createSideBar() {

        HTMLPanel support = new HTMLPanel("");
        sidebarPanel.add(support);
        support.addStyleName("gradient");
        support.add(scrollPanel);
        scrollPanel.setStyleName("navigation-scroll");
        scrollPanel.add(tasksWrap);
        tasksWrap.add(menuItems);
        support.add(arrsPanel);
        arrTop = DOM.createSpan();
        arrTop.addClassName("arr top disabled");
        arrBottom = DOM.createSpan();
        arrBottom.addClassName("arr bottom");
        Element arrsPanelElement = arrsPanel.getElement();
        DOM.appendChild(arrsPanelElement, arrTop);
        DOM.appendChild(arrsPanelElement, arrBottom);

    }

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

        DOM.setEventListener(arrTop, new EventListener() {

            @Override
            public void onBrowserEvent(Event event) {

                switch (DOM.eventGetType(event)) {
                    case Event.ONCLICK:
                        changePositionTop.run(ANIMATION_DURATION, scrollPanel.getVerticalScrollPosition());
                        break;
                }
            }
        });

        DOM.setEventListener(arrBottom, new EventListener() {

            @Override
            public void onBrowserEvent(Event event) {

                switch (DOM.eventGetType(event)) {
                    case Event.ONCLICK:
                        changePositionBottom.run(ANIMATION_DURATION, scrollPanel.getVerticalScrollPosition());
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
        } else if (scrollPanel.getVerticalScrollPosition() == scrollPanel.getMaximumVerticalScrollPosition()) {
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

    private strictfp void sidebarContentCorrect() {
        int clientHeight = Window.getClientHeight();
        if (clientHeight < 50) {
            return;
        }

        // client height without north (header, topMenu ...)
        double totalHeight = clientHeight - 50;

        int visibleItemCount = (int) Math.floor((totalHeight - BUT_RESERV_HEIGHT) / SB_ELEM_HEIGHT);
        int visibleHeight = SB_ELEM_HEIGHT * visibleItemCount;

        // manage scrollPanel height
        if (visibleHeight > 0 && visibleItemCount < menuItems.getWidgetCount()) {
            scrollPanel.setHeight(Integer.toString(visibleHeight) + "px");
            showArrs();
        } else {
            scrollPanel.setHeight("auto");
            hideArrs();
        }
    }

    private void showArrs() {
        initArrowsImages();
        Style style = arrsPanel.getElement().getStyle();
        style.setDisplay(Display.BLOCK);
    }

    private void hideArrs() {
        Style style = arrsPanel.getElement().getStyle();
        style.setDisplay(Display.NONE);
    }

    public void correctContentStyles() {
        sidebarContentCorrect();
    }

    private class ChangeVerticalScrollPositionBottom extends Animation {

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

        public void run(int duration, int position) {
            this.scrollPosition = position;
            super.run(duration);
        }

        @Override
        protected void onComplete() {
            super.onComplete();
            if (scrollPanel.getVerticalScrollPosition() == scrollPanel.getMaximumVerticalScrollPosition()) {
                disableDownArrow();
            } else {
                enableDownArrow();
            }
            enableUpArrow();
        }
    }

    private class ChangeVerticalScrollPositionTop extends Animation {

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

        public void run(int duration, int position) {
            this.scrollPosition = position;
            super.run(duration);
        }

        @Override
        protected void onComplete() {
            super.onComplete();
            if (scrollPanel.getVerticalScrollPosition() == scrollPanel.getMinimumVerticalScrollPosition()) {
                disableUpArrow();
            } else {
                enableUpArrow();
            }
            enableDownArrow();
        }
    }
}
