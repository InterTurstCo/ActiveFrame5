package ru.intertrust.cm.core.gui.impl.client.form.widget.tableviewer;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.HasLinkedFormMappings;
import ru.intertrust.cm.core.config.gui.form.widget.LinkedFormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.TableBrowserParams;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetDisplayConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.LinkedFormMappingConfig;
import ru.intertrust.cm.core.config.gui.form.widget.tableviewer.TableViewerConfig;
import ru.intertrust.cm.core.config.gui.navigation.*;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.event.*;
import ru.intertrust.cm.core.gui.impl.client.event.collection.OpenDomainObjectFormEvent;
import ru.intertrust.cm.core.gui.impl.client.event.collection.OpenDomainObjectFormEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.form.ParentTabSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.form.ParentTabSelectedEventHandler;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.impl.client.form.widget.breadcrumb.CollectionWidgetHelper;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.HyperlinkClickHandler;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;
import ru.intertrust.cm.core.gui.impl.client.util.GuiUtil;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.filters.ComplexFiltersParams;
import ru.intertrust.cm.core.gui.model.form.widget.TableViewerState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.ArrayList;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.*;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 13.12.2014
 *         Time: 20:34
 */
@ComponentName("table-viewer")
public class TableViewerWidget extends BaseWidget implements ParentTabSelectedEventHandler, HierarchicalCollectionEventHandler,
        OpenDomainObjectFormEventHandler, HasLinkedFormMappings, CollectionRowSelectedEventHandler, BreadCrumbNavigationEventHandler {
    private PluginPanel pluginPanel;
    private EventBus localEventBus;
    private CollectionWidgetHelper collectionWidgetHelper;
    private TableViewerConfig config;
    private HorizontalPanel toolbarPanel;
    private Id selectedId;

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
        final Panel pluginWrapper = new AbsolutePanel();
        pluginWrapper.addStyleName("table-viewer-wrapper");
        pluginPanel = new PluginPanel();
        localEventBus = new SimpleEventBus();

        pluginWrapper.add(buildToolbarPanel());

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
        localEventBus.addHandler(OpenDomainObjectFormEvent.TYPE, this);
        localEventBus.addHandler(CollectionRowSelectedEvent.TYPE, this);
        localEventBus.addHandler(BreadCrumbNavigationEvent.TYPE, this);
        eventBus.addHandler(UpdateCollectionEvent.TYPE, new UpdateCollectionEventHandler() {
            @Override
            public void updateCollection(UpdateCollectionEvent event) {
                pluginPanel.getCurrentPlugin().refresh();
            }
        });
        return pluginWrapper;
    }

    private Widget buildToolbarPanel() {
        ToggleButton editButton;
        ToggleButton createButton;
        toolbarPanel = new HorizontalPanel();
        editButton = new ToggleButton();
        createButton = new ToggleButton();
        editButton.setStyleName(GlobalThemesManager.getCurrentTheme().commonCss().editButton());
        editButton.setTitle("Редактировать");
        createButton.setStyleName(GlobalThemesManager.getCurrentTheme().commonCss().addDoBtn());
        createButton.setTitle("Создать");

        editButton.addClickHandler(new ClickHandler() {
                                       @Override
                                       public void onClick(ClickEvent event) {
                                           if (selectedId != null) {
                                               localEventBus.fireEvent(new OpenDomainObjectFormEvent(selectedId));
                                           }
                                       }
                                   }
        );

        toolbarPanel.add(editButton);
        toolbarPanel.add(createButton);


        return toolbarPanel;
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
        selectedId = null;
        config = state.getTableViewerConfig();
        TableBrowserParams tableBrowserParams = createTableBrowserParams(config);

        if (config.getCollectionViewerConfig() != null &&
                config.getCollectionViewerConfig().getToolBarConfig() != null &&
                config.getCollectionViewerConfig().getToolBarConfig().isUseDefault()) {
            toolbarPanel.setVisible(true);
        } else {
            toolbarPanel.setVisible(false);
        }

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
            return config.getCollectionViewerConfig();
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

    @Override
    public void onOpenDomainObjectFormEvent(OpenDomainObjectFormEvent event) {

        if (getLinkedFormMappingConfig() != null) {
            HyperlinkClickHandler clickHandler = new HyperlinkClickHandler(event.getId(), null,
                    eventBus, false, null, this, false).withModalWindow(true);
            clickHandler.processClick();
        }

    }


    @Override
    public LinkedFormMappingConfig getLinkedFormMappingConfig() {
        return config.getLinkedFormMappingConfig();
    }

    @Override
    public LinkedFormConfig getLinkedFormConfig() {
        return null;
    }


    @Override
    public void onCollectionRowSelect(CollectionRowSelectedEvent event) {
        selectedId = event.getId();
    }

    @Override
    public void onNavigation(BreadCrumbNavigationEvent event) {
        selectedId = null;
    }
}
