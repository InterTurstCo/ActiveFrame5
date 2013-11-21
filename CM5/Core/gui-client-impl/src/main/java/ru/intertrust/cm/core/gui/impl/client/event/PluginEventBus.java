package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * <p>
 * Класс шины событий для плагинов системы. При создании плагина объект этого класса будет "обслуживать" коммуникации
 * между "дочерними" компонентами плагина, например: вложенными плагинами, панелями и т.д.
 * </p>
 * Created with IntelliJ IDEA.
 *
 * User: IPetrov
 * Date: 21.11.13
 * Time: 12:57
 *
 */
public class PluginEventBus extends SimpleEventBus {

    public PluginEventBus () {
        super();
    }
    /**
     * Регистрация обработчика на шине событий
     * в случае использования GwtEvent
     *
     * @return ссылка на обработчик события
     */
    public <H extends EventHandler> com.google.gwt.event.shared.HandlerRegistration addHandler(
            GwtEvent.Type<H> type, final H handler) {
        return wrap(addHandler((Event.Type<H>) type, handler));
    }

    /**
     * Регистрация кастомного обработчика на шине событий.
     * Можно использовать для "собственных" обработчиков
     * плагинов, панелей
     *
     * @return ссылка на обработчик события
     */

    public <H> HandlerRegistration addHandler(Event.Type<H> type, H handler) {
        return super.addHandler(type, handler);
    }

    /**
     * Генерация определенного события на шине событий
     * плагинов, панелей
     */

    public void fireEvent(Event<?> event) {
        super.fireEvent(event);
    }

    /**
     * Генерация события из определенного источника на шине событий
     * плагинов, панелей
     */

    public void fireEventFromSource(Event<?> event, Object source) {
        super.fireEventFromSource(event, source);
    }
}

