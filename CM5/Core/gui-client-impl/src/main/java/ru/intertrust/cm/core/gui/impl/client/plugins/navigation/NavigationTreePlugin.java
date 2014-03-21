package ru.intertrust.cm.core.gui.impl.client.plugins.navigation;

import com.google.gwt.event.shared.GwtEvent;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.model.BusinessUniverseInitialization;
import ru.intertrust.cm.core.gui.model.ComponentName;

@ComponentName("navigation.tree")
public class NavigationTreePlugin extends Plugin implements RootNodeSelectedEventHandler {

    protected EventBus eventBus;
    private BusinessUniverseInitialization businessUniverseInitialization;

    // установка шины событий плагину
    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public PluginView createView() {
        return new NavigationTreePluginView(this);

    }

    @Override
    public Component createNew() {
        return new NavigationTreePlugin();
    }

    @Override
    protected GwtEvent.Type[] getEventTypesToHandle() {
        return new GwtEvent.Type[]{RootLinkSelectedEvent.TYPE};
    }

    @Override
    public void onRootNodeSelected(RootLinkSelectedEvent event) {
        NavigationTreePluginView pluginView = (NavigationTreePluginView) getView();
        pluginView.repaintNavigationTrees(event.getSelectedRootLinkName());
    }

    public void setBusinessUniverseInitialization(BusinessUniverseInitialization businessUniverseInitialization) {
        this.businessUniverseInitialization = businessUniverseInitialization;
    }

    public BusinessUniverseInitialization getBusinessUniverseInitialization() {
        return businessUniverseInitialization;
    }
}
