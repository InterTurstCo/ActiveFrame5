package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.model.gui.navigation.PluginConfig;
import ru.intertrust.cm.core.gui.api.client.BaseComponent;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEvent;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.ArrayList;
import java.util.List;
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
    private PluginConfig config;
    private PluginData initialData;
    private PluginView view;
    private List<HandlerRegistration> handlerRegistrations = new ArrayList<HandlerRegistration>();

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
     *
     * @return текущее состояние приложения
     */
    public Dto getCurrentState() {
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
            }

            @Override
            public void onFailure(Throwable caught) {
                Plugin.this.onDataLoadFailure();
            }
        };
        Command command = new Command("initialize", this.getName(), getConfig());
        BusinessUniverseServiceAsync.Impl.getInstance().executeCommand(command, callback);
    }

    public void reinit(PluginData initData) {
        getOwner().closeCurrentPlugin();
        Plugin.this.setInitialData(initData);
        postSetUp();
    }

    protected <H extends EventHandler> void addHandler(GwtEvent.Type<H> type, H handler) {
        handlerRegistrations.add(eventBus.addHandler(type, handler));
    }

    void clearHandlers() {
        for (HandlerRegistration registration : handlerRegistrations) {
            registration.removeHandler();
        }
    }

    /**
     * Возвращает представление плагина.
     *
     * @return представление плагина
     */
    protected PluginView getView() {
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
     * Возвращает конфигурацию плагина
     *
     * @return конфигурацию плагина
     */
    public PluginConfig getConfig() {
        return config;
    }

    /**
     * Устанавливает конфигурацию плагина
     *
     * @param config конфигурация плагина
     */
    public void setConfig(PluginConfig config) {
        this.config = config;
    }

    /**
     * Возвращает первичные данные плагина.
     *
     * @return первичные данные плагина
     */
    public <T extends PluginData> T getInitialData() {
        return (T) initialData;
    }

    /**
     * Устанавливает первичные данные плагина. Эти данные используются при инициализация плагина.
     *
     * @param initialData первичные данные данного плагина
     */
    public void setInitialData(PluginData initialData) {
        PluginData original = this.initialData;
        boolean initialDataChanged = original != null && initialData != original;
        this.initialData = initialData;
        if (initialDataChanged) {
            getView().updateActionToolBar();
            afterInitialDataChange(original, initialData);
        }
    }

    protected void afterInitialDataChange(PluginData oldData, PluginData newData) {
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
