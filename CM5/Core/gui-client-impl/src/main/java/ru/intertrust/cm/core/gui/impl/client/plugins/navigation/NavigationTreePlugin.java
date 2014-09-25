package ru.intertrust.cm.core.gui.impl.client.plugins.navigation;

import com.google.gwt.event.shared.GwtEvent;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Pair;
import ru.intertrust.cm.core.config.gui.navigation.ChildLinksConfig;
import ru.intertrust.cm.core.config.gui.navigation.LinkConfig;
import ru.intertrust.cm.core.config.gui.navigation.NavigationConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.history.HistoryManager;
import ru.intertrust.cm.core.gui.impl.client.ApplicationWindow;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.event.NavigationTreeItemSelectedEvent;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.NavigationTreePluginData;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;

import java.util.List;

@ComponentName("navigation.tree")
public class NavigationTreePlugin extends Plugin implements RootNodeSelectedEventHandler {

    protected EventBus eventBus;

    // установка шины событий плагину™
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
    public void setInitialData(PluginData initialData) {
        final NavigationTreePluginData data = (NavigationTreePluginData) initialData;
        final HistoryManager historyManager = Application.getInstance().getHistoryManager();
        if (historyManager.hasLink()) {
            final Pair<String, String> historyNavigationItems = getHistoryNavigationItems(data);
            if (historyNavigationItems != null) {
                data.setRootLinkSelectedName(historyNavigationItems.getFirst());
                data.setChildToOpen(historyNavigationItems.getSecond());
            }
        } else if (!historyManager.getSelectedIds().isEmpty()) {
            data.setChildToOpen(null);
        }
        super.setInitialData(initialData);
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

    public void clearCurrentSelectedItemValue() {
        ((NavigationTreePluginView) getView()).clearCurrentSelectedItemValue();
    }

    @Override
    public boolean restoreHistory() {
        final HistoryManager historyManager = Application.getInstance().getHistoryManager();
        final NavigationTreePluginView view = (NavigationTreePluginView) getView();
        final String selectedLinkName = view.getSelectedLinkName() == null ? "" : view.getSelectedLinkName();
        //TODO: [CMFIVE-451] commented out to be able to move back from hierarchical link to normal. Need to find a better way.
        //if (!selectedLinkName.equals(historyManager.getLink())) {
            final NavigationTreePluginData data = getInitialData();
            final Pair<String, String> selectedNavigationItems = getHistoryNavigationItems(data);
            if (selectedNavigationItems != null) {
                view.showAsSelectedRootLink(selectedNavigationItems.getFirst());
                view.repaintNavigationTrees(selectedNavigationItems.getFirst(), selectedNavigationItems.getSecond());
                return true;
            } else {
                //try to find requested link among hierarchical links and emulate tree item selection
                NavigationConfig navigationConfig = getNavigationConfig();
                for (LinkConfig hierarchicalLink : navigationConfig.getHierarchicalLinkList()) {
                    if (hierarchicalLink.getName().equals(historyManager.getLink())) {
                        Application.getInstance().getEventBus().fireEvent(new NavigationTreeItemSelectedEvent(
                                hierarchicalLink.getPluginDefinition().getPluginConfig(), hierarchicalLink.getName(),
                                navigationConfig));
                        return true;
                    }
                }
                ApplicationWindow.errorAlert("Пункт меню '" + historyManager.getLink() + "' не найден");
            }
       // }
        return false;
    }

    public Pair<String, String> getHistoryNavigationItems(final NavigationTreePluginData data) {
        final HistoryManager historyManager = Application.getInstance().getHistoryManager();
        final List<LinkConfig> linkConfigs = data.getNavigationConfig().getLinkConfigList();
        final Pair<String, String> result = new Pair<>();
        boolean notExists = true;
        for (LinkConfig linkConfig : linkConfigs) {
            final String rootName = linkConfig.getName();
            result.setFirst(rootName);
            if (rootName.equals(historyManager.getLink())) {
                notExists = false;
                break;
            } else {
                final String childLink = findChildLink(linkConfig.getChildLinksConfigList(), historyManager);
                if (childLink != null) {
                    notExists = false;
                    result.setSecond(childLink);
                    break;
                }
            }
        }
        if (notExists) {
            return null;
        } else {
            return result;
        }
    }

    private String findChildLink(final List<ChildLinksConfig> childLinksConfigs, final HistoryManager manager) {
        for (ChildLinksConfig childLinksConfig : childLinksConfigs) {
            final List<LinkConfig> linkConfigs = childLinksConfig.getLinkConfigList();
            for (LinkConfig linkConfig : linkConfigs) {
                if (linkConfig.getName().equals(manager.getLink())) {
                    return linkConfig.getName();
                }
                final String childLink = findChildLink(linkConfig.getChildLinksConfigList(), manager);
                if (childLink != null) {
                    return childLink;
                }
            }
        }
        return null;
    }
}
