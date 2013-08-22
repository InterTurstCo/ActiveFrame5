package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.api.client.BaseComponent;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEvent;

/**
 * @author Denis Mitavskiy
 *         Date: 14.08.13
 *         Time: 13:01
 */
public abstract class Plugin extends BaseComponent {
    private PluginPanel owner;

    private AsyncCallback<Dto> callback;
    private EventBus eventBus;
    private PluginView view;

    void setUp() {
        callback = new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                view = Plugin.this.createView(result);
                PluginViewCreatedEvent viewCreatedEvent = new PluginViewCreatedEvent(Plugin.this);
                eventBus.fireEventFromSource(viewCreatedEvent, Plugin.this);
            }

            @Override
            public void onFailure(Throwable caught) {
                Plugin.this.onDataLoadFailure();
            }
        };
        init(callback);
    }

    PluginView getView() {
        return view;
    }

    public abstract void init(AsyncCallback<Dto> callback);

    public abstract PluginView createView(Dto data);

    public void onDataLoadFailure() {
        Window.alert("Ошибка инициализации плагина");
    }

    void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }
}
