package ru.intertrust.cm.core.gui.impl.client.form.widget.breadcrumb;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.navigation.CollectionViewerConfig;
import ru.intertrust.cm.core.config.gui.navigation.DomainObjectSurferConfig;
import ru.intertrust.cm.core.config.gui.navigation.LinkConfig;
import ru.intertrust.cm.core.config.gui.navigation.NavigationConfig;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.ApplicationWindow;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.event.BreadCrumbNavigationEvent;
import ru.intertrust.cm.core.gui.impl.client.event.HierarchicalCollectionEvent;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEventListener;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionPlugin;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionPluginView;
import ru.intertrust.cm.core.gui.impl.client.util.LinkUtil;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.form.widget.BreadCrumbItem;
import ru.intertrust.cm.core.gui.model.plugin.ExpandHierarchicalCollectionData;
import ru.intertrust.cm.core.gui.model.plugin.HierarchicalCollectionData;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 14.12.2014
 *         Time: 21:43
 */
public class CollectionWidgetHelper {

    private List<BreadCrumbItem> breadCrumbItems ;
    private EventBus eventBus;

    public CollectionWidgetHelper(EventBus eventBus) {

        this.eventBus = eventBus;
        breadCrumbItems = new ArrayList<BreadCrumbItem>();
    }


    public void handleHierarchyEvent(HierarchicalCollectionEvent event, final CollectionViewerConfig initialCollectionViewerConfig, final PluginPanel pluginPanel) {
        String currentCollectionName = initialCollectionViewerConfig.getCollectionRefConfig().getName();
        ExpandHierarchicalCollectionData data = new ExpandHierarchicalCollectionData(
                event.getChildCollectionViewerConfigs(), event.getSelectedId(), currentCollectionName);

        final Command command = new Command("prepareHierarchicalCollectionData", "hierarchical.collection.builder", data);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onFailure(Throwable caught) {
                ApplicationWindow.errorAlert("Ошибка получения данных иерархической коллекции: " + caught.getMessage());
            }

            @Override
            public void onSuccess(Dto result) {
                HierarchicalCollectionData data = (HierarchicalCollectionData) result;
                DomainObjectSurferConfig pluginConfig = data.getDomainObjectSurferConfig();
                CollectionViewerConfig collectionViewerConfig = pluginConfig.getCollectionViewerConfig();
                collectionViewerConfig.setEmbedded(true);
                LinkConfig link = data.getHierarchicalLink();
                NavigationConfig navigationConfig = new NavigationConfig();
                LinkUtil.addHierarchicalLinkToNavigationConfig(navigationConfig, link);

                if (breadCrumbItems.isEmpty()) {
                    breadCrumbItems.add(new BreadCrumbItem("root", "Исходная коллекция", //we haven't display text for the root
                            initialCollectionViewerConfig));
                }
                breadCrumbItems.add(new BreadCrumbItem(link.getName(), link.getDisplayText(), collectionViewerConfig));

                openCollectionPlugin(collectionViewerConfig, navigationConfig, pluginPanel);
            }
        });
    }


    public void openCollectionPlugin(CollectionViewerConfig collectionViewerConfig, NavigationConfig navigationConfig,
                                      final PluginPanel pluginPanel) {
        final CollectionPlugin collectionPlugin = ComponentRegistry.instance.get("collection.plugin");
        collectionPlugin.setConfig(collectionViewerConfig);
        collectionPlugin.setLocalEventBus(eventBus);
        collectionPlugin.setNavigationConfig(navigationConfig);
        collectionPlugin.addViewCreatedListener(new PluginViewCreatedEventListener() {
            @Override
            public void onViewCreation(PluginViewCreatedEvent source) {
                CollectionPluginView view = (CollectionPluginView) collectionPlugin.getView();
                view.setBreadcrumbWidgets(breadCrumbItemsToWidgets(pluginPanel));

            }
        });
        pluginPanel.open(collectionPlugin);

    }
    private List<IsWidget> breadCrumbItemsToWidgets(final PluginPanel pluginPanel) {
        List<IsWidget> breadCrumbWidgets = new ArrayList<>();
        for (final BreadCrumbItem item : breadCrumbItems) {
            Anchor breadCrumb = new Anchor(item.getDisplayText());
            breadCrumb.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    navigateByBreadCrumb(item.getName(), pluginPanel);
                }
            });
            breadCrumbWidgets.add(breadCrumb);
        }
        return breadCrumbWidgets;
    }

    private void navigateByBreadCrumb(String linkName,  PluginPanel pluginPanel) {
        CollectionViewerConfig config = null;
        int removeFrom = breadCrumbItems.size();
        for (int i = 0; i < breadCrumbItems.size() - 1; i++) { // skip last item
            BreadCrumbItem breadCrumbItem = breadCrumbItems.get(i);
            if (breadCrumbItem.getName().equals(linkName)) {
                config = breadCrumbItem.getConfig();
                removeFrom = i;
            }
        }
        breadCrumbItems.subList(removeFrom, breadCrumbItems.size()).clear();
        if (config != null) {
            openCollectionPlugin(config, new NavigationConfig(), pluginPanel);

        }
        eventBus.fireEvent(new BreadCrumbNavigationEvent());
    }

}
