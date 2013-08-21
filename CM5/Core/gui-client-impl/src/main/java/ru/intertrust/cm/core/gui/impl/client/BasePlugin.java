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
public abstract class BasePlugin extends BaseComponent {
    private AsyncCallback<Dto> callback;
    private EventBus eventBus;
    private BasePluginView view;

    void setUp() {
        callback = new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                view = BasePlugin.this.createView(result);
                PluginViewCreatedEvent viewCreatedEvent = new PluginViewCreatedEvent(BasePlugin.this);
                eventBus.fireEventFromSource(viewCreatedEvent, BasePlugin.this);
            }

            @Override
            public void onFailure(Throwable caught) {
                BasePlugin.this.onDataLoadFailure();
            }
        };
        init(callback);
    }

    BasePluginView getView() {
        return view;
    }

    public abstract void init(AsyncCallback<Dto> callback);

    public abstract BasePluginView createView(Dto data);

    public void onDataLoadFailure() {
        Window.alert("Ошибка инициализации плагина");
    }

    void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }
}
