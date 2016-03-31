package ru.intertrust.cm.core.gui.impl.client.form.widget.tableviewer;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.TableBrowserParams;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetDisplayConfig;
import ru.intertrust.cm.core.config.gui.form.widget.tableviewer.TableViewerConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionRefConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionViewRefConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionViewerConfig;
import ru.intertrust.cm.core.config.gui.navigation.DefaultSortCriteriaConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.event.HierarchicalCollectionEvent;
import ru.intertrust.cm.core.gui.impl.client.event.HierarchicalCollectionEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.form.ParentTabSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.form.ParentTabSelectedEventHandler;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.impl.client.form.widget.breadcrumb.CollectionWidgetHelper;
import ru.intertrust.cm.core.gui.impl.client.util.GuiUtil;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.filters.ComplexFiltersParams;
import ru.intertrust.cm.core.gui.model.form.widget.TableViewerState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.ArrayList;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.DEFAULT_EMBEDDED_COLLECTION_TABLE_HEIGHT;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.DEFAULT_EMBEDDED_COLLECTION_TABLE_WIDTH;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 13.12.2014
 *         Time: 20:34
 */
@ComponentName("table-viewer")
public class TableViewerWidget extends BaseWidget implements ParentTabSelectedEventHandler, HierarchicalCollectionEventHandler {
    private PluginPanel pluginPanel;
    private EventBus localEventBus;
    private CollectionWidgetHelper collectionWidgetHelper;

    @Override
    public void setCurrentState(WidgetState currentState) {
        TableViewerState state = (TableViewerState) currentState;
        CollectionViewerConfig config = initCollectionConfig(state);
        collectionWidgetHelper.openCollectionPlugin(config, null, pluginPanel);
    }

    @Override
    protected boolean isChanged() {
        return false;
    }

    @Override
    protected WidgetState createNewState() {
        return new TableViewerState();
    }

    @Override
    protected Widget asEditableWidget(WidgetState state) {
        return initView();

    }

    private Widget initView() {
        WidgetDisplayConfig displayConfig = getDisplayConfig();
        Panel pluginWrapper = new AbsolutePanel();
        pluginPanel = new PluginPanel();
        localEventBus = new SimpleEventBus();

        String height = displayConfig.getHeight() == null ? DEFAULT_EMBEDDED_COLLECTION_TABLE_HEIGHT : displayConfig.getHeight();
        pluginWrapper.setHeight(height);
        String width = displayConfig.getWidth() == null ? DEFAULT_EMBEDDED_COLLECTION_TABLE_WIDTH : displayConfig.getWidth();
        pluginWrapper.setWidth(width);
        int tableWidth = Integer.parseInt(width.replaceAll("\\D+", ""));
        pluginPanel.setVisibleWidth(tableWidth);
        pluginWrapper.add(pluginPanel);
        eventBus.addHandler(ParentTabSelectedEvent.TYPE, this);
        collectionWidgetHelper = new CollectionWidgetHelper(localEventBus);
        localEventBus.addHandler(HierarchicalCollectionEvent.TYPE, this);

        return pluginWrapper;
    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        return asEditableWidget(state);
    }

    @Override
    public Component createNew() {
        return new TableViewerWidget();
    }

    private CollectionViewerConfig initCollectionConfig(TableViewerState state) {
        TableViewerConfig config = state.getTableViewerConfig();
        TableBrowserParams tableBrowserParams = createTableBrowserParams(config);

        if (config.getCollectionViewerConfig() == null) {
            CollectionViewerConfig collectionViewerConfig = new CollectionViewerConfig();
            CollectionViewRefConfig collectionViewRefConfig = new CollectionViewRefConfig();
            collectionViewerConfig.setTableBrowserParams(tableBrowserParams);
            collectionViewRefConfig.setName(config.getCollectionViewRefConfig().getName());
            CollectionRefConfig collectionRefConfig = new CollectionRefConfig();
            collectionRefConfig.setName(config.getCollectionRefConfig().getName());
            DefaultSortCriteriaConfig defaultSortCriteriaConfig = config.getDefaultSortCriteriaConfig();
            collectionViewerConfig.setDefaultSortCriteriaConfig(defaultSortCriteriaConfig);
            collectionViewerConfig.setCollectionRefConfig(collectionRefConfig);
            collectionViewerConfig.setCollectionViewRefConfig(collectionViewRefConfig);
            collectionViewerConfig.setEmbedded(true);
            return collectionViewerConfig;
        } else {
            config.getCollectionViewerConfig().setTableBrowserParams(tableBrowserParams);
            config.getCollectionViewerConfig().setEmbedded(true);
            return  config.getCollectionViewerConfig();
        }
    }

    private TableBrowserParams createTableBrowserParams(TableViewerConfig config) {
        ComplexFiltersParams filtersParams = GuiUtil.createComplexFiltersParams(getContainer());


        TableBrowserParams tableBrowserParams = new TableBrowserParams()
                .setComplexFiltersParams(filtersParams)
                .setIds(new ArrayList<Id>())
                .setDisplayOnlySelectedIds(false)
                .setDisplayCheckBoxes(false)
                .setDisplayChosenValues(true)
                .setPageSize(config.getPageSize())
                .setCollectionExtraFiltersConfig((config.getCollectionViewerConfig() != null) ? config.getCollectionViewerConfig().getCollectionExtraFiltersConfig() :
                        config.getCollectionExtraFiltersConfig())
                .setHasColumnButtons(config.getCollectionTableButtonsConfig() == null ? true
                        : config.getCollectionTableButtonsConfig().isDisplayAllPossible());
        return tableBrowserParams;
    }

    @Override
    public void onParentTabSelectedEvent(ParentTabSelectedEvent event) {
        Element parentElement = event.getParent().getElement();
        Node widgetNode = pluginPanel.asWidget().getElement().getParentNode();
        boolean widgetIsChildOfSelectedTab = parentElement.isOrHasChild(widgetNode);
        boolean viewIsInitialized = pluginPanel.getCurrentPlugin() != null && pluginPanel.getCurrentPlugin().getView() != null;
        if (widgetIsChildOfSelectedTab && viewIsInitialized) {
            pluginPanel.getCurrentPlugin().getView().onPluginPanelResize();
        }
    }

    @Override
    public void onExpandHierarchyEvent(HierarchicalCollectionEvent event) {
        CollectionViewerConfig config = initCollectionConfig(this.<TableViewerState>getInitialData());
        collectionWidgetHelper.handleHierarchyEvent(event, config, pluginPanel);
    }
}
