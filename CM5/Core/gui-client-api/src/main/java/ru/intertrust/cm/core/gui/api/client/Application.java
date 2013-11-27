package ru.intertrust.cm.core.gui.api.client;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.SimpleEventBus;

/**
 * Утилитный класс, который обеспечивает доступ к "глобальным" объектам приложения
 * User: IPetrov
 * Date: 25.11.13
 * Time: 16:54
 */
public class Application {

    private static Application ourInstance = null;

    /*
     * Шина событий приложения
     */
    private SimpleEventBus APP_EVENT_BUS = null;

    /*
     * Метод получения экземпляра класса
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
    public SimpleEventBus getAppEventBus( ) {
        return APP_EVENT_BUS;
    }

    private Application() {
        // создаем шину сообщений
        APP_EVENT_BUS = GWT.create(SimpleEventBus.class);
    }
}
