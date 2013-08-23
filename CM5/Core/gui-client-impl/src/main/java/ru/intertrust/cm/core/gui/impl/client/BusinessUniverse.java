package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import ru.intertrust.cm.core.gui.api.client.BaseComponent;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.model.BusinessUniverseInitialization;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

/**
 * @author Denis Mitavskiy
 *         Date: 19.07.13
 *         Time: 16:22
 */
@ComponentName("business.universe")
public class BusinessUniverse extends BaseComponent implements EntryPoint {
    public void onModuleLoad() {
        AsyncCallback<BusinessUniverseInitialization> callback = new AsyncCallback<BusinessUniverseInitialization>() {
            @Override
            public void onSuccess(BusinessUniverseInitialization result) {
                PluginPanel pluginPanel = new PluginPanel();
                Plugin myPlugin = ComponentRegistry.instance.get("some.plugin");

                PluginPanel anotherPluginPanel = new PluginPanel();
                Plugin anotherPlugin = ComponentRegistry.instance.get("some.active.plugin");

                VerticalPanel verticalPanel = new VerticalPanel();

                verticalPanel.add(pluginPanel);
                verticalPanel.add(anotherPluginPanel);

                RootLayoutPanel.get().add(verticalPanel);
                pluginPanel.open(myPlugin);
                anotherPluginPanel.open(anotherPlugin);


            }

            @Override
            public void onFailure(Throwable caught) {
                Window.Location.assign("/cm-sochi/Login.html" + Window.Location.getQueryString());
            }
        };
        BusinessUniverseServiceAsync.Impl.getInstance().getBusinessUniverseInitialization(callback);
    }

    @Override
    public Component createNew() {
        return new BusinessUniverse();
    }
}
