package ru.intertrust.cm.core.gui.api.client;

import java.util.List;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Утилитный класс, который обеспечивает доступ к "глобальным" объектам приложения
 * User: IPetrov
 * Date: 25.11.13
 * Time: 16:54
 */
public class Application {

    private static Application ourInstance = null;
    private PopupPanel glassPopupPanel;
    private Timer timer = new Timer() {
        @Override
        public void run() {
            glassPopupPanel.show();

        }
    };



    /*
     * Шина событий приложения
     */
    private EventBus eventBus = null;
    private CompactModeState compactModeState;
    private List<String> timeZoneIds;

    /*
     * Метод получения экземпляра класса                                        ;
     */
    public static Application getInstance() {
        if(ourInstance == null) {
            ourInstance = new Application();
        }
        return ourInstance;
    }

    /*
     * Метод получения "глобальной" шины событий
     */
    public EventBus getEventBus( ) {
        return eventBus;
    }

    public CompactModeState getCompactModeState() {
        return compactModeState;
    }

    public List<String> getTimeZoneIds() {
        return timeZoneIds;
    }

    public void setTimeZoneIds(List<String> timeZoneIds) {
        this.timeZoneIds = timeZoneIds;
    }

    public void enableTimer(){
        timer.schedule(100);
    }

    public void disableTimer(){
          timer.cancel();
          glassPopupPanel.hide();

    }



    private Application() {
        // создаем шину сообщений
        eventBus = GWT.create(SimpleEventBus.class);
        compactModeState = new CompactModeState();
        glassPopupPanel = new PopupPanel();
        createGlassPopup();
    }

    private void createGlassPopup(){
        glassPopupPanel.setGlassEnabled(true);
        glassPopupPanel.setGlassStyleName("glass");
        Image image = new Image();
        image.setUrl("progressbar.gif");
        image.addStyleName("loading");
        glassPopupPanel.getElement().getStyle().setZIndex(1000);
        glassPopupPanel.getElement().getStyle().setBorderWidth(0, Style.Unit.PX);
        glassPopupPanel.add(image);
        glassPopupPanel.center();

    }
}
