package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.api.client.BaseComponent;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEvent;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.logging.Logger;

/**
 * <p>
 * Базовый класс плагинов системы. Плагин - это элемент управления, который может быть внедрён в соответствующую панель
 * - {@link PluginPanel}. Плагин может быть наделён логикой, выполняемой на сервере. За обработку сообщений и команд
 * плагинов на стороне сервера отвечают наследники класса {@link PluginHandler}.
 * </p>
 * <p>
 * Плагин является компонентом GUI и должен быть именован {@link ru.intertrust.cm.core.gui.model.ComponentName}.
 * </p>
 *
 * @author Denis Mitavskiy
 *         Date: 14.08.13
 *         Time: 13:01
 */
public abstract class Plugin extends BaseComponent {
    private PluginPanel owner;
    private EventBus eventBus;
    private PluginData initialData;
    private PluginView view;

    static Logger logger = Logger.getLogger("plugin logger");

    /**
     * Создаёт представление данного плагина
     *
     * @return представление данного плагина
     */
    public abstract PluginView createView();

    public void onDataLoadFailure() {
        Window.alert("Ошибка инициализации плагина " + this.getName());
    }

    /**
     * Возвращает текущее состояние приложения
     * @return текущее состояние приложения
     */
    public PluginData getCurrentState() {
        return null;
    }

    /**
     * Производит первичную инициализацию плагина, загружая необходимые для него данные. Серверная инициализация
     * производится соответствующим обработчиком плагина в методе {@link PluginHandler#initialize(Dto)}
     */
    protected void setUp() {
        if (!isInitializable()) {
            postSetUp();
            return;
        }

        AsyncCallback<Dto> callback = new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                Plugin.this.setInitialData((PluginData) result);
                postSetUp();
                //logger.info("success " + getName());
            }

            @Override
            public void onFailure(Throwable caught) {
                Plugin.this.onDataLoadFailure();
                logger.info("failed " + getName());
            }
        };
        Command command = new Command("initialize", this.getName(), null);
        BusinessUniverseServiceAsync.Impl.getInstance().executeCommand(command, callback);
    }

    /**
     * Возвращает представление плагина.
     *
     * @return представление плагина
     */
    PluginView getView() {
        return view;
    }

    /**
     * Устанавливает "владельца" (панель плагинов) данного плагина.
     *
     * @param owner панель плагинов, в которой данный плагин отображается
     */
    void setOwner(PluginPanel owner) {
        this.owner = owner;
    }

    /**
     * Устанавливает шину сообщений
     *
     * @param eventBus шина сообщений
     */
    void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * Возвращает первичные данные плагина.
     *
     * @return первичные данные плагина
     */
    <T> T getInitialData() {
        return (T) initialData;
    }

    /**
     * Устанавливает первичные данные плагина. Эти данные используются при инициализация плагина.
     *
     * @param initialData первичные данные данного плагина
     */
    void setInitialData(PluginData initialData) {
        this.initialData = initialData;
    }

    /**
     * Определяет, требует ли плагин инициализации. Если нет, то при открытии плагина не будет совершаться вызов
     * серверных методов, ускоряя его открытие и снижая нагрузку на сервер.
     *
     * @return true, если плагин требует инициализации и false - в противном случае
     */
    protected boolean isInitializable() {
        return true;
    }

    private void postSetUp() {
        view = createView();
        PluginViewCreatedEvent viewCreatedEvent = new PluginViewCreatedEvent(this);
        eventBus.fireEventFromSource(viewCreatedEvent, this);
    }

    public PluginPanel getOwner() {
        return owner;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public void registerEventHandlingFromExternalSource(GwtEvent.Type eventType, Object externalEventSource, Object handler) {
        getEventBus().addHandlerToSource(eventType, externalEventSource, handler);
    }


}
