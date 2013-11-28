package ru.intertrust.cm.core.gui.api.client;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.web.bindery.event.shared.EventBus;

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
    private EventBus eventBus = null;

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
    public EventBus getEventBus( ) {
        return eventBus;
    }

    private Application() {
        // создаем шину сообщений
        eventBus = GWT.create(SimpleEventBus.class);
    }
}
