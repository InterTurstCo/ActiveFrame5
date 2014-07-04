package ru.intertrust.cm.core.gui.impl.client.plugins.navigation;

import java.util.List;
import com.google.gwt.event.shared.GwtEvent;
import com.google.web.bindery.event.shared.EventBus;

import ru.intertrust.cm.core.config.gui.navigation.ChildLinksConfig;
import ru.intertrust.cm.core.config.gui.navigation.LinkConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.history.HistoryManager;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.plugins.RestoreHistorySupport;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.NavigationTreePluginData;

@ComponentName("navigation.tree")
public class NavigationTreePlugin extends Plugin implements RootNodeSelectedEventHandler, RestoreHistorySupport {

    protected EventBus eventBus;

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
        pluginView.repaintNavigationTrees(event.getSelectedRootLinkName(), null);
    }

    @Override
    public boolean restoreHistory() {
        final HistoryManager historyManager = Application.getInstance().getHistoryManager();
        final NavigationTreePluginView view = (NavigationTreePluginView) getView();
        final String selectedLinkName = view.getSelectedLinkName();
        boolean linkChanged = historyManager.getLink() != null
                && !historyManager.getLink().isEmpty()
                && !HistoryManager.UNKNOWN_LINK.equals(historyManager.getLink())
                && !historyManager.getLink().equals(selectedLinkName);
        if (linkChanged) {
            final NavigationTreePluginData data = getInitialData();
            final List<LinkConfig> linkConfigs = data.getNavigationConfig().getLinkConfigList();
            String rootName = null;
            boolean containsChild = false;
            for (LinkConfig linkConfig : linkConfigs) {
                rootName = linkConfig.getName();
                if (historyManager.getLink().equals(rootName)) {
                    break;
                } else {
                    containsChild = containsChildLink(linkConfig.getChildLinksConfigList(), historyManager.getLink());
                    if (containsChild) {
                        break;
                    }
                }
            }
            if (rootName != null) {
                view.showAsSelectedRootLink(rootName);
                view.repaintNavigationTrees(rootName, containsChild ? historyManager.getLink() : null);
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean containsChildLink(final List<ChildLinksConfig> childLinksConfigs, final String childName) {
        for (ChildLinksConfig childLinksConfig : childLinksConfigs) {
            final List<LinkConfig> linkConfigs = childLinksConfig.getLinkConfigList();
            for (LinkConfig linkConfig : linkConfigs) {
                if (childName.equals(linkConfig.getName())
                        || containsChildLink(linkConfig.getChildLinksConfigList(), childName)) {
                    return true;
                }
            }
        }
        return false;
    }
}
