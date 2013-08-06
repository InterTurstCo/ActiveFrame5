package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentName;
import ru.intertrust.cm.core.gui.model.BusinessUniverseInitialization;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

/**
 * @author Denis Mitavskiy
 *         Date: 19.07.13
 *         Time: 16:22
 */
@ComponentName("business.universe")
public class BusinessUniverse implements EntryPoint, Component {
    public void onModuleLoad() {
        AsyncCallback<BusinessUniverseInitialization> callback = new AsyncCallback<BusinessUniverseInitialization>() {
            @Override
            public void onSuccess(BusinessUniverseInitialization result) {
                Window.alert("Welcome!");

            }

            @Override
            public void onFailure(Throwable caught) {
                Window.Location.assign("/cm-sochi/Login.html");
            }
        };
        BusinessUniverseServiceAsync.Impl.getInstance().getBusinessUniverseInitialization(callback);
    }

    @Override
    public Component createNew() {
        return new BusinessUniverse();
    }
}
