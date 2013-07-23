package ru.intertrust.cm.core.gui.impl.client.temp.vaadin;

import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;

/**
 * @author Denis Mitavskiy
 *         Date: 15.07.13
 *         Time: 18:50
 */
public class MainMenu extends HorizontalLayout {
    private Component frame0;
    private Component frame1;
    private Component frame2;
    private Component frame3;

    public MainMenu() {
    }

    public void setComponents(Component... frame) {
        this.frame0 = frame[0];
        this.frame1 = frame[1];
        this.frame2 = frame[2];
        this.frame3 = frame[3];
        setHeight("30px");
        addComponent(createMenu());
    }

    public MenuBar createMenu() {

        MenuBar menuBar = new MenuBar();

        MenuBar.Command command = new MenuBar.Command() {
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                switch (selectedItem.getDescription()) {
                    case "TOGGLE_FRAME_3":
                        if (frame3.getWidth() == 0.0f) {
                            frame3.setWidth("100px");
                        } else {
                            frame3.setWidth("0px");
                        }
                        break;
                    case "TOGGLE_FRAME_0":
                        if (frame0.getHeight() == 0.0f) {
                            frame0.setHeight("100px");
                        } else {
                            frame0.setHeight("0px");
                        }
                        break;
                    case "TOGGLE_FRAME_1":
                        if (frame1.getHeight() == 0.0f) {
                            frame1.setHeight("100px");
                        } else {
                            frame1.setHeight("0px");
                        }
                        break;
                    case "TOGGLE_FRAME_2":
                        if (frame2.getWidth() == 0.0f) {
                            frame2.setWidth("100px");
                        } else {
                            frame2.setWidth("0px");
                        }
                        break;
                    case "FULL_SCREEN":
                        frame0.setHeight("0px");
                        frame1.setHeight("0px");
                        frame2.setWidth("0px");
                        frame3.setWidth("0px");

                    default:
                        break;

                }
            }
        };

        MenuBar.MenuItem actions = menuBar.addItem("Действие", null, null);
        MenuBar.MenuItem hideFrameZero = actions.addItem("Скрыть фрейм 0", null, command);
        hideFrameZero.setDescription("TOGGLE_FRAME_0");
        MenuBar.MenuItem hideFrameOne = actions.addItem("Скрыть фрейм 1", null, command);
        hideFrameOne.setDescription("TOGGLE_FRAME_1");
        MenuBar.MenuItem hideFrameTwo = actions.addItem("Скрыть фрейм 2", null, command);
        hideFrameTwo.setDescription("TOGGLE_FRAME_2");
        MenuBar.MenuItem hideFrameThree = actions.addItem("Скрыть фрейм 3", null, command);
        hideFrameThree.setDescription("TOGGLE_FRAME_3");
        MenuBar.MenuItem fullScreen = actions.addItem("Во весь экран", null, command);
        fullScreen.setDescription("FULL_SCREEN");

        menuBar.setSizeFull();
        return menuBar;
    }
}
