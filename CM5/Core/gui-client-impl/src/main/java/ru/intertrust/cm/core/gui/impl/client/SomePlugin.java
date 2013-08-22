package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentName;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

/**
 * @author Denis Mitavskiy
 *         Date: 13.08.13
 *         Time: 18:43
 */
@ComponentName("some.plugin")
public class SomePlugin extends Plugin {

    @Override
    public void init(AsyncCallback<Dto> callback) {
        BusinessUniverseServiceAsync.Impl.getInstance().getBusinessUniverseInitialization(callback);
    }

    @Override
    public PluginView createView(Dto data) {
        return new PluginView();
    }

    @Override
    public Component createNew() {
        return new SomePlugin();
    }
}
