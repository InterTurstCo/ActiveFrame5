package ru.intertrust.cm.core.gui.impl.client.plugins.navigation;

import com.google.gwt.user.client.rpc.AsyncCallback;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.NavigationTreePluginData;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

@ComponentName("navigation.tree")
public class NavigationTreePlugin extends Plugin implements RootNodeSelectedEventHandler {
    @Override
    public PluginView createView() {
        return new NavigationTreePluginView(this);

    }

    @Override
    public Component createNew() {
        return new NavigationTreePlugin();
    }

    @Override
    public void onRootNodeSelected(RootLinkSelectedEvent event) {
        AsyncCallback<Dto> callback = new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                NavigationTreePlugin.this.reinit((NavigationTreePluginData) result);
            }
            @Override
            public void onFailure(Throwable caught) {
            }
        };
        NavigationTreePluginData navigationTreePluginData = new NavigationTreePluginData();
        navigationTreePluginData.setRootLinkSelectedName(event.getSelectedRootLinkName());
        Command command = new Command("rootNodeSelected", this.getName(), navigationTreePluginData);
        BusinessUniverseServiceAsync.Impl.getInstance().executeCommand(command, callback);


    }
}
