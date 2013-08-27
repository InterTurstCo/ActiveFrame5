package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import ru.intertrust.cm.core.gui.api.client.BaseComponent;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEvent;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

/**
 * <p>
 * Базовый класс плагинов системы. Плагин - это элемент управления, который может быть внедрён в соответствующую панель
 * - {@link PluginPanel}. Плагин может быть наделён логикой, выполняемой на сервере. За обработку сообщений и команд
 * плагинов на стороне сервера отвечают наследники класса PluginHandler.
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

    void setUp() {
        if (!isInitializable()) {
            postSetUp();
            return;
        }

        AsyncCallback<PluginData> callback = new AsyncCallback<PluginData>() {
            @Override
            public void onSuccess(PluginData result) {
                postSetUp();
                Plugin.this.setInitialData(result); // view will get init data and build tool bar, for instance
            }

            @Override
            public void onFailure(Throwable caught) {
                Plugin.this.onDataLoadFailure();
            }
        };
        Command command = new Command("initialize", this.getName(), null);
        BusinessUniverseServiceAsync.Impl.getInstance().executeCommand(command, callback);
    }

    PluginView getView() {
        return view;
    }

    void setOwner(PluginPanel owner) {
        this.owner = owner;
    }

    public abstract PluginView createView();

    public void onDataLoadFailure() {
        Window.alert("Ошибка инициализации плагина");
    }

    void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    PluginData getInitialData() {
        return initialData;
    }

    void setInitialData(PluginData initialData) {
        this.initialData = initialData;
    }

    /**
     * Определяет, требует ли плагин инициализации. Если нет, то при открытии плагина не будет совершаться вызов
     * серверных методов, ускоряя его открытие и снижая нагрузку на сервер.
     * @return true, если плагин требует инициализации и false - в противном случае
     */
    protected boolean isInitializable() {
        return true;
    }

    private void postSetUp() {
        view = Plugin.this.createView();
        PluginViewCreatedEvent viewCreatedEvent = new PluginViewCreatedEvent(Plugin.this);
        eventBus.fireEventFromSource(viewCreatedEvent, Plugin.this);
    }
}
