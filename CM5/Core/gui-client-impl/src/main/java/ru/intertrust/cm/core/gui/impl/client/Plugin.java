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
    private PluginView view;

    void setUp() {
        AsyncCallback<PluginData> callback = new AsyncCallback<PluginData>() {
            @Override
            public void onSuccess(PluginData result) {
                view = Plugin.this.createView(result);
                PluginViewCreatedEvent viewCreatedEvent = new PluginViewCreatedEvent(Plugin.this);
                eventBus.fireEventFromSource(viewCreatedEvent, Plugin.this);
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

    public abstract PluginView createView(PluginData initialData);

    public void onDataLoadFailure() {
        Window.alert("Ошибка инициализации плагина");
    }

    void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }
}
